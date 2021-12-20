package pokecube.adventures.network;

import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates.AIState;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.StructureEvent.ReadTag;
import pokecube.core.handlers.events.SpawnEventsHandler.GuardInfo;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.Tools;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.api.maths.Vector3;
import thut.api.util.JsonUtil;
import thut.core.common.network.EntityUpdate;
import thut.core.common.network.NBTPacket;
import thut.core.common.network.PacketAssembly;

public class PacketTrainer extends NBTPacket
{
    public static final PacketAssembly<PacketTrainer> ASSEMBLER = PacketAssembly.registerAssembler(PacketTrainer.class,
            PacketTrainer::new, PokecubeAdv.packets);

    public static final String EDITSELF = "pokecube_adventures.traineredit.self";
    public static final String EDITOTHER = "pokecube_adventures.traineredit.other";
    public static final String EDITMOB = "pokecube_adventures.traineredit.mob";
    public static final String EDITTRAINER = "pokecube_adventures.traineredit.trainer";
    public static final String SPAWNTRAINER = "pokecube_adventures.traineredit.spawn";

    public static final byte REQUESTEDIT = -1;

    public static final byte UPDATETRAINER = 0;
    public static final byte NOTIFYDEFEAT = 1;
    public static final byte KILLTRAINER = 2;
    public static final byte UPDATEMOB = 3;
    public static final byte SPAWN = 4;

    public static void register()
    {
        PermissionAPI.registerNode(PacketTrainer.EDITSELF, DefaultPermissionLevel.OP,
                "Allowed to edit self with trainer editor");
        PermissionAPI.registerNode(PacketTrainer.EDITOTHER, DefaultPermissionLevel.OP,
                "Allowed to edit other player with trainer editor");
        PermissionAPI.registerNode(PacketTrainer.EDITMOB, DefaultPermissionLevel.OP,
                "Allowed to edit pokemobs with trainer editor");
        PermissionAPI.registerNode(PacketTrainer.EDITTRAINER, DefaultPermissionLevel.OP,
                "Allowed to edit trainer with trainer editor");
        PermissionAPI.registerNode(PacketTrainer.SPAWNTRAINER, DefaultPermissionLevel.OP,
                "Allowed to spawn trainer with trainer editor");
    }

    public static void requestEdit(final Entity target)
    {
        final PacketTrainer packet = new PacketTrainer(PacketTrainer.REQUESTEDIT);
        packet.getTag().putInt("I", target == null ? -1 : target.getId());
        PacketTrainer.ASSEMBLER.sendToServer(packet);
    }

    public static void sendEditOpenPacket(final Entity target, final ServerPlayer editor)
    {
        final String node = target == editor || target == null
                ? editor.isCrouching() ? PacketTrainer.EDITSELF : PacketTrainer.SPAWNTRAINER
                : target instanceof ServerPlayer ? PacketTrainer.EDITOTHER
                        : TrainerCaps.getHasPokemobs(target) != null ? PacketTrainer.EDITTRAINER
                                : PacketTrainer.EDITMOB;
        final boolean canEdit = !editor.getServer().isDedicatedServer() || PermissionAPI.hasPermission(editor, node);

        if (!canEdit)
        {
            editor.sendMessage(new TextComponent(ChatFormatting.RED + "You are not allowed to do that."),
                    Util.NIL_UUID);
            return;
        }
        final PacketTrainer packet = new PacketTrainer(PacketTrainer.UPDATETRAINER);
        packet.getTag().putBoolean("O", true);
        packet.getTag().putInt("I", target == null ? -1 : target.getId());

        if (target != null)
        {
            final CompoundTag tag = new CompoundTag();
            final IHasNPCAIStates ai = TrainerCaps.getNPCAIStates(target);
            final IGuardAICapability guard = target.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);
            final IHasPokemobs pokemobs = TrainerCaps.getHasPokemobs(target);
            final IHasRewards rewards = TrainerCaps.getHasRewards(target);
            final IHasMessages messages = TrainerCaps.getMessages(target);
            if (ai != null) tag.put("A", ai.serializeNBT());
            if (guard != null) tag.put("G", guard.serializeNBT());
            if (pokemobs != null) tag.put("P", pokemobs.serializeNBT());
            if (rewards != null) tag.put("R", rewards.serializeNBT());
            if (messages != null) tag.put("M", messages.serializeNBT());
            packet.getTag().put("C", tag);
        }
        PacketTrainer.ASSEMBLER.sendTo(packet, editor);
    }

    byte message;

    public PacketTrainer()
    {}

    public PacketTrainer(final byte message)
    {
        this.getTag().putByte("__message__", message);
    }

    public PacketTrainer(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient()
    {
        final Player player = PokecubeCore.proxy.getPlayer();
        this.message = this.getTag().getByte("__message__");
        switch (this.message)
        {
        case UPDATETRAINER:
            // O for Open Gui Packet.
            if (this.getTag().getBoolean("O"))
            {
                final int id = this.getTag().getInt("I");
                final Entity mob = player.getCommandSenderWorld().getEntity(id);
                if (mob != null && this.getTag().contains("C"))
                {
                    final CompoundTag nbt = this.getTag().getCompound("C");
                    final IHasNPCAIStates ai = TrainerCaps.getNPCAIStates(mob);
                    final IGuardAICapability guard = mob.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
                    final IHasPokemobs pokemobs = TrainerCaps.getHasPokemobs(mob);
                    final IHasRewards rewards = TrainerCaps.getHasRewards(mob);
                    final IHasMessages messages = TrainerCaps.getMessages(mob);
                    if (nbt.contains("A")) if (ai != null) ai.deserializeNBT(nbt.getCompound("A"));
                    if (nbt.contains("G")) if (guard != null) guard.deserializeNBT(nbt.getCompound("G"));
                    if (nbt.contains("G")) if (pokemobs != null) pokemobs.deserializeNBT(nbt.getCompound("P"));
                    if (nbt.contains("G"))
                        if (rewards != null) rewards.deserializeNBT(nbt.getList("R", Tag.TAG_COMPOUND));
                    if (nbt.contains("G")) if (messages != null) messages.deserializeNBT(nbt.getCompound("M"));
                }
                net.minecraft.client.Minecraft.getInstance().setScreen(new EditorGui(mob));
                return;
            }

            break;
        }
    }

    @Override
    protected void onCompleteServer(final ServerPlayer player)
    {
        this.message = this.getTag().getByte("__message__");
        final Level world = player.getCommandSenderWorld();
        String type;
        String name;
        boolean male;
        final int id = this.getTag().getInt("I");
        type = this.getTag().getString("__type__");
        name = this.getTag().getString("N");
        male = this.getTag().getBoolean("G");
        Entity mob;
        mob = world.getEntity(id);
        IHasPokemobs mobHolder;
        switch (this.message)
        {
        case REQUESTEDIT:
            mob = id == -1 ? null : world.getEntity(id);
            PacketTrainer.sendEditOpenPacket(mob, player);
            break;
        case SPAWN:
            if (!PermissionAPI.hasPermission(player, PacketTrainer.SPAWNTRAINER))
            {
                player.sendMessage(new TextComponent(ChatFormatting.RED + "You are not allowed to do that."),
                        Util.NIL_UUID);
                return;
            }

            PokecubeCore.LOGGER.debug("Recieved Trainer Spawn Packet");

            final int level = this.getTag().getInt("L");
            final Vector3 vec = Vector3.getNewVector().set(player);
            String args = "pokecube:mob:npc";
            final JsonObject thing = new JsonObject();

            final String npckey = this.getTag().getString("_npc_");
            if (npckey.equals("trainer")) args = "pokecube_adventures:trainer";
            if (npckey.equals("leader")) args = "pokecube_adventures:leader";

            thing.addProperty("level", level);

            // Type deals with the npc mob itself, trainerType also does extra
            // processing if this is a trainer npc
            thing.addProperty("type", type);
            thing.addProperty("trainerType", type);

            if (this.getTag().contains("_g_")) thing.addProperty("gender", this.getTag().getString("_g_"));
            if (this.getTag().getBoolean("S"))
            {
                final GuardInfo info = new GuardInfo();
                info.time = "allday";
                info.roam = 0;
                thing.add("guard", JsonUtil.gson.toJsonTree(info));
                final IHasNPCAIStates aiStates = TrainerCaps.getNPCAIStates(mob);
                if (aiStates != null) aiStates.setAIState(AIState.STATIONARY, true);
            }
            final String var = JsonUtil.gson.toJson(thing);
            args = args + var;
            final StructureEvent.ReadTag event = new ReadTag(args, vec.getPos(), player.getCommandSenderWorld(),
                    (ServerLevel) player.getCommandSenderWorld(), player.getRandom(), BoundingBox.infinite());
            MinecraftForge.EVENT_BUS.post(event);
            break;
        case UPDATETRAINER:
            if (!PermissionAPI.hasPermission(player, PacketTrainer.EDITTRAINER))
            {
                player.sendMessage(new TextComponent(ChatFormatting.RED + "You are not allowed to do that."),
                        Util.NIL_UUID);
                return;
            }

            // Here we are editing a rewards list for a trainer
            if (this.getTag().contains("__rewards__"))
            {
                final IHasRewards rewards = TrainerCaps.getHasRewards(mob);
                try
                {
                    rewards.deserializeNBT((ListTag) this.getTag().get("__rewards__"));
                    player.displayClientMessage(new TextComponent("Updated rewards list"), true);
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }

            // Here we edit the AI holder
            if (this.getTag().contains("__ai__"))
            {
                final IHasNPCAIStates aiStates = TrainerCaps.getNPCAIStates(mob);
                try
                {
                    boolean trades = aiStates.getAIState(AIState.TRADES_ITEMS);
                    aiStates.deserializeNBT((CompoundTag) this.getTag().get("__ai__"));
                    if (trades != aiStates.getAIState(AIState.TRADES_ITEMS) && mob instanceof NpcMob npc)
                    {
                        npc.updateTrades();
                    }
                    mob.setInvulnerable(aiStates.getAIState(AIState.INVULNERABLE));
                    player.displayClientMessage(new TextComponent("Updated AI Setting"), true);
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }

            // Here we edit the Messages holder
            if (this.getTag().contains("__messages__"))
            {
                final IHasMessages messages = TrainerCaps.getMessages(mob);
                PokecubeCore.LOGGER.debug("Editing Messages");
                try
                {
                    messages.deserializeNBT((CompoundTag) this.getTag().get("__messages__"));
                    player.displayClientMessage(new TextComponent("Updated AI Setting"), true);
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }

            // Here we edit the mob itself
            if (!type.isEmpty())
            {
                final ICopyMob copied = CopyCaps.get(mob);
                mobHolder = TrainerCaps.getHasPokemobs(mob);
                if (copied != null)
                {
                    final String res = this.getTag().getString("fM");
                    copied.setCopiedID(res.isEmpty() ? null : new ResourceLocation(res));
                }
                if (mob instanceof NpcMob)
                {
                    final NpcMob npc = (NpcMob) mob;
                    final NpcType newType = NpcType.byType(type);
                    npc.setNpcType(newType);
                    npc.setMale(male);
                    if (this.getTag().getBoolean("rawName")) npc.setNPCName(name);
                    else npc.setTypedName(name);
                    npc.urlSkin = this.getTag().getString("uS");
                    npc.playerName = this.getTag().getString("pS");
                    npc.customTex = this.getTag().getString("cS");
                    final String prev = npc.customTrades;
                    npc.customTrades = this.getTag().getString("cT");
                    if (!prev.equals(npc.customTrades)) npc.resetTrades();
                    EntityUpdate.sendEntityUpdate(npc);
                }
            }
            break;
        case KILLTRAINER:
            if (!PermissionAPI.hasPermission(player, PacketTrainer.EDITTRAINER))
            {
                player.sendMessage(new TextComponent(ChatFormatting.RED + "You are not allowed to do that."),
                        Util.NIL_UUID);
                return;
            }
            mob = player.getCommandSenderWorld().getEntity(id);
            if (mob != null) mob.discard();
            break;
        case UPDATEMOB:
            if (!PermissionAPI.hasPermission(player, PacketTrainer.EDITMOB))
            {
                player.sendMessage(new TextComponent(ChatFormatting.RED + "You are not allowed to do that."),
                        Util.NIL_UUID);
                return;
            }
            mob = player.getCommandSenderWorld().getEntity(id);
            mobHolder = TrainerCaps.getHasPokemobs(mob);
            // This means we are editing a mob of a trainer.
            if (this.getTag().contains("__trainers__") && mobHolder != null)
            {
                final int index = this.getTag().getInt("__trainers__");
                ItemStack cube = ItemStack.EMPTY;
                if (this.getTag().contains("__pokemob__"))
                {
                    final CompoundTag mobtag = this.getTag().getCompound("__pokemob__");
                    cube = ItemStack.of(mobtag);
                    final IPokemob pokemob = PokecubeManager.itemToPokemob(cube, world);
                    // Load out the moves, since those don't send properly...
                    if (mobtag.contains("__custom_info__"))
                        this.readPokemob(pokemob, mobtag.getCompound("__custom_info__"));
                    cube = PokecubeManager.pokemobToItem(pokemob);
                }
                mobHolder.setPokemob(index, cube);
                EntityUpdate.sendEntityUpdate(mob);
                if (this.getTag().getBoolean("__reopen__")) PacketTrainer.sendEditOpenPacket(mob, player);
            }
            else if (this.getTag().contains("__pokemob__"))
            {
                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
                if (pokemob != null)
                {
                    final CompoundTag mobtag = this.getTag().getCompound("__pokemob__");
                    this.readPokemob(pokemob, mobtag);
                    EntityUpdate.sendEntityUpdate(mob);
                }
            }
            break;
        }
    }

    private void readPokemob(final IPokemob pokemob, final CompoundTag mobtag)
    {
        for (int i = 0; i < 4; i++)
        {
            final String move = mobtag.getString("m_" + i);
            pokemob.setMove(i, move);
        }
        for (int i = 0; i < 6; i++)
        {
            final byte iv = mobtag.getByte("iv_" + i);
            final byte[] ivs = pokemob.getIVs();
            ivs[i] = iv;
            pokemob.setIVs(ivs);
            final byte ev = mobtag.getByte("ev_" + i);
            final byte[] evs = pokemob.getEVs();
            evs[i] = ev;
            pokemob.setEVs(evs);
        }
        final String ability = mobtag.getString("a");
        final int level = mobtag.getInt("l");
        final String nature = mobtag.getString("n");
        final float size = mobtag.getFloat("s");
        final boolean shiny = mobtag.getBoolean("sh");
        final byte gender = mobtag.getByte("g");
        if (gender != 0) pokemob.setSexe(gender);
        if (!nature.isEmpty()) pokemob.setNature(Nature.valueOf(nature));
        pokemob.setAbilityRaw(AbilityManager.getAbility(ability));
        pokemob.setShiny(shiny);
        pokemob.setSize(size);
        pokemob.setExp(Tools.levelToXp(pokemob.getExperienceMode(), level), false);
        pokemob.onGenesChanged();
    }
}
