package pokecube.adventures.network;

import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
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
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.trainers.IHasMessages;
import pokecube.api.entity.trainers.IHasNPCAIStates;
import pokecube.api.entity.trainers.IHasNPCAIStates.AIState;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.IHasRewards;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.events.StructureEvent;
import pokecube.api.events.StructureEvent.ReadTag;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.eventhandlers.SpawnEventsHandler.GuardInfo;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.CapHolders;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.api.maths.Vector3;
import thut.api.util.JsonUtil;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.core.common.network.EntityUpdate;
import thut.core.common.network.nbtpacket.NBTPacket;
import thut.core.common.network.nbtpacket.PacketAssembly;
import thut.lib.TComponent;

public class PacketTrainer extends NBTPacket
{
    public static final PacketAssembly<PacketTrainer> ASSEMBLER = PacketAssembly.registerAssembler(PacketTrainer.class,
            PacketTrainer::new, PokecubeAdv.packets);

    public static final String EDITSELF = "traineredit.self";
    public static final String EDITOTHER = "traineredit.other";
    public static final String EDITMOB = "traineredit.mob";
    public static final String EDITTRAINER = "traineredit.trainer";
    public static final String SPAWNTRAINER = "traineredit.spawn";

    public static final byte REQUESTEDIT = -1;

    public static final byte UPDATETRAINER = 0;
    public static final byte NOTIFYDEFEAT = 1;
    public static final byte KILLTRAINER = 2;
    public static final byte UPDATEMOB = 3;
    public static final byte SPAWN = 4;

    public static void register()
    {
        PermNodes.registerNode(PacketTrainer.EDITSELF, DefaultPermissionLevel.OP,
                "Allowed to edit self with trainer editor");
        PermNodes.registerNode(PacketTrainer.EDITOTHER, DefaultPermissionLevel.OP,
                "Allowed to edit other player with trainer editor");
        PermNodes.registerNode(PacketTrainer.EDITMOB, DefaultPermissionLevel.OP,
                "Allowed to edit pokemobs with trainer editor");
        PermNodes.registerNode(PacketTrainer.EDITTRAINER, DefaultPermissionLevel.OP,
                "Allowed to edit trainer with trainer editor");
        PermNodes.registerNode(PacketTrainer.SPAWNTRAINER, DefaultPermissionLevel.OP,
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
        final boolean canEdit = !editor.getServer().isDedicatedServer() || PermNodes.getBooleanPerm(editor, node);

        if (!canEdit)
        {
            thut.lib.ChatHelper.sendSystemMessage(editor,
                    TComponent.literal(ChatFormatting.RED + "You are not allowed to do that."));
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
                final Entity mob = player.getLevel().getEntity(id);
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
        final Level world = player.getLevel();
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
            if (!PermNodes.getBooleanPerm(player, PacketTrainer.SPAWNTRAINER))
            {
                thut.lib.ChatHelper.sendSystemMessage(player,
                        TComponent.literal(ChatFormatting.RED + "You are not allowed to do that."));
                return;
            }
            PokecubeAPI.logDebug("Recieved Trainer Spawn Packet");

            final int level = this.getTag().getInt("L");
            final Vector3 vec = new Vector3().set(player);
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
            final StructureEvent.ReadTag event = new ReadTag(args, vec.getPos(), player.getLevel(),
                    (ServerLevel) player.getLevel(), player.getRandom(), BoundingBox.infinite());
            MinecraftForge.EVENT_BUS.post(event);
            break;
        case UPDATETRAINER:
            if (!PermNodes.getBooleanPerm(player, PacketTrainer.EDITTRAINER))
            {
                thut.lib.ChatHelper.sendSystemMessage(player,
                        TComponent.literal(ChatFormatting.RED + "You are not allowed to do that."));
                return;
            }

            // Here we are editing a rewards list for a trainer
            if (this.getTag().contains("__rewards__"))
            {
                final IHasRewards rewards = TrainerCaps.getHasRewards(mob);
                try
                {
                    rewards.deserializeNBT((ListTag) this.getTag().get("__rewards__"));
                    player.displayClientMessage(TComponent.literal("Updated rewards list"), true);
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }

            // Here we do the trainer in general
            if (this.getTag().contains("__T__"))
            {
                mobHolder = TrainerCaps.getHasPokemobs(mob);
                try
                {
                    mobHolder.deserializeNBT((CompoundTag) this.getTag().get("__T__"));
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
                    player.displayClientMessage(TComponent.literal("Updated AI Setting"), true);
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
                PokecubeAPI.logDebug("Editing Messages");
                try
                {
                    messages.deserializeNBT((CompoundTag) this.getTag().get("__messages__"));
                    player.displayClientMessage(TComponent.literal("Updated AI Setting"), true);
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
            if (!PermNodes.getBooleanPerm(player, PacketTrainer.EDITTRAINER))
            {
                thut.lib.ChatHelper.sendSystemMessage(player,
                        TComponent.literal(ChatFormatting.RED + "You are not allowed to do that."));
                return;
            }
            mob = player.getLevel().getEntity(id);
            if (mob != null) mob.discard();
            break;
        case UPDATEMOB:
            if (!PermNodes.getBooleanPerm(player, PacketTrainer.EDITMOB))
            {
                thut.lib.ChatHelper.sendSystemMessage(player,
                        TComponent.literal(ChatFormatting.RED + "You are not allowed to do that."));
                return;
            }
            mob = player.getLevel().getEntity(id);
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
                final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
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
