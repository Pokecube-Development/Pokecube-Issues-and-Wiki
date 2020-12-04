package pokecube.adventures.network;

import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasRewards;
import pokecube.adventures.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.client.gui.items.editor.EditorGui;
import pokecube.adventures.events.TrainerSpawnHandler;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.StructureEvent.ReadTag;
import pokecube.core.handlers.events.SpawnEventsHandler.GuardInfo;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.CapHolders;
import thut.api.maths.Vector3;
import thut.core.common.network.EntityUpdate;
import thut.core.common.network.NBTPacket;
import thut.core.common.network.PacketAssembly;

public class PacketTrainer extends NBTPacket
{
    public static final PacketAssembly<PacketTrainer> ASSEMBLER = PacketAssembly.registerAssembler(PacketTrainer.class,
            PacketTrainer::new, PokecubeAdv.packets);

    public static final String EDITSELF     = "pokecube_adventures.traineredit.self";
    public static final String EDITOTHER    = "pokecube_adventures.traineredit.other";
    public static final String EDITMOB      = "pokecube_adventures.traineredit.mob";
    public static final String EDITTRAINER  = "pokecube_adventures.traineredit.trainer";
    public static final String SPAWNTRAINER = "pokecube_adventures.traineredit.spawn";

    public static final byte REQUESTEDIT = -1;

    public static final byte UPDATETRAINER = 0;
    public static final byte NOTIFYDEFEAT  = 1;
    public static final byte KILLTRAINER   = 2;
    public static final byte UPDATEMOB     = 3;
    public static final byte SPAWN         = 4;

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
        packet.getTag().putInt("I", target == null ? -1 : target.getEntityId());
        PacketTrainer.ASSEMBLER.sendToServer(packet);
    }

    public static void sendEditOpenPacket(final Entity target, final ServerPlayerEntity editor)
    {
        final String node = target == editor || target == null ? editor.isCrouching() ? PacketTrainer.EDITSELF
                : PacketTrainer.SPAWNTRAINER
                : target instanceof ServerPlayerEntity ? PacketTrainer.EDITOTHER
                        : TrainerCaps.getHasPokemobs(target) != null ? PacketTrainer.EDITTRAINER
                                : PacketTrainer.EDITMOB;
        final boolean canEdit = !editor.getServer().isDedicatedServer() || PermissionAPI.hasPermission(editor, node);

        if (!canEdit)
        {
            editor.sendMessage(new StringTextComponent(TextFormatting.RED + "You are not allowed to do that."),
                    Util.DUMMY_UUID);
            return;
        }
        final PacketTrainer packet = new PacketTrainer(PacketTrainer.UPDATETRAINER);
        packet.getTag().putBoolean("O", true);
        packet.getTag().putInt("I", target == null ? -1 : target.getEntityId());

        if (target != null)
        {
            final CompoundNBT tag = new CompoundNBT();
            final IHasNPCAIStates ai = TrainerCaps.getNPCAIStates(target);
            final IGuardAICapability guard = target.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);
            final IHasPokemobs pokemobs = TrainerCaps.getHasPokemobs(target);
            final IHasRewards rewards = TrainerCaps.getHasRewards(target);
            final IHasMessages messages = TrainerCaps.getMessages(target);
            if (ai != null) tag.put("A", CapabilityNPCAIStates.storage.writeNBT(TrainerCaps.AISTATES_CAP, ai, null));
            if (guard != null) tag.put("G", CapHolders.GUARDAI_CAP.getStorage().writeNBT(CapHolders.GUARDAI_CAP, guard,
                    null));
            if (pokemobs != null) tag.put("P", CapabilityHasPokemobs.storage.writeNBT(TrainerCaps.HASPOKEMOBS_CAP,
                    pokemobs, null));
            if (rewards != null) tag.put("R", CapabilityHasRewards.storage.writeNBT(TrainerCaps.REWARDS_CAP, rewards,
                    null));
            if (messages != null) tag.put("M", CapabilityNPCMessages.storage.writeNBT(TrainerCaps.MESSAGES_CAP,
                    messages, null));
            packet.getTag().put("C", tag);
        }
        PacketTrainer.ASSEMBLER.sendTo(packet, editor);
    }

    byte message;

    public PacketTrainer()
    {
    }

    public PacketTrainer(final byte message)
    {
        this.getTag().putByte("__message__", message);
    }

    public PacketTrainer(final PacketBuffer buffer)
    {
        super(buffer);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        this.message = this.getTag().getByte("__message__");
        switch (this.message)
        {
        case UPDATETRAINER:
            // O for Open Gui Packet.
            if (this.getTag().getBoolean("O"))
            {
                final int id = this.getTag().getInt("I");
                final Entity mob = player.getEntityWorld().getEntityByID(id);
                if (mob != null && this.getTag().contains("C"))
                {
                    final CompoundNBT nbt = this.getTag().getCompound("C");
                    final IHasNPCAIStates ai = TrainerCaps.getNPCAIStates(mob);
                    final IGuardAICapability guard = mob.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
                    final IHasPokemobs pokemobs = TrainerCaps.getHasPokemobs(mob);
                    final IHasRewards rewards = TrainerCaps.getHasRewards(mob);
                    final IHasMessages messages = TrainerCaps.getMessages(mob);
                    if (nbt.contains("A")) if (ai != null) CapabilityNPCAIStates.storage.readNBT(
                            TrainerCaps.AISTATES_CAP, ai, null, nbt.get("A"));
                    if (nbt.contains("G")) if (guard != null) CapHolders.GUARDAI_CAP.getStorage().readNBT(
                            CapHolders.GUARDAI_CAP, guard, null, nbt.get("G"));
                    if (nbt.contains("P")) if (pokemobs != null) CapabilityHasPokemobs.storage.readNBT(
                            TrainerCaps.HASPOKEMOBS_CAP, pokemobs, null, nbt.get("P"));
                    if (nbt.contains("R")) if (rewards != null) CapabilityHasRewards.storage.readNBT(
                            TrainerCaps.REWARDS_CAP, rewards, null, nbt.get("R"));
                    if (nbt.contains("M")) if (messages != null) CapabilityNPCMessages.storage.readNBT(
                            TrainerCaps.MESSAGES_CAP, messages, null, nbt.get("M"));
                }
                net.minecraft.client.Minecraft.getInstance().displayGuiScreen(new EditorGui(mob));
                return;
            }

            break;
        }
    }

    @Override
    protected void onCompleteServer(final ServerPlayerEntity player)
    {
        this.message = this.getTag().getByte("__message__");
        String type;
        String name;
        boolean male;
        final int id = this.getTag().getInt("I");
        type = this.getTag().getString("__type__");
        name = this.getTag().getString("N");
        male = this.getTag().getBoolean("G");
        Entity mob;
        mob = player.getEntityWorld().getEntityByID(id);
        IHasPokemobs mobHolder;
        switch (this.message)
        {
        case REQUESTEDIT:
            mob = id == -1 ? null : player.getEntityWorld().getEntityByID(id);
            PacketTrainer.sendEditOpenPacket(mob, player);
            break;
        case SPAWN:
            if (!PermissionAPI.hasPermission(player, PacketTrainer.SPAWNTRAINER))
            {
                player.sendMessage(new StringTextComponent(TextFormatting.RED + "You are not allowed to do that."),
                        Util.DUMMY_UUID);
                return;
            }

            PokecubeCore.LOGGER.debug("Recieved Trainer Spawn Packet");

            int level = this.getTag().getInt("L");
            final boolean leader = this.getTag().getBoolean("C");
            final Vector3 vec = Vector3.getNewVector().set(player);
            String args = "pokecube_adventures:" + (leader ? "leader" : "trainer");
            final JsonObject thing = new JsonObject();
            thing.addProperty("level", level);
            thing.addProperty("trainerType", type);
            if (this.getTag().getBoolean("S"))
            {
                final GuardInfo info = new GuardInfo();
                info.time = "day";
                info.roam = 2;
                thing.add("guard", PokedexEntryLoader.gson.toJsonTree(info));
            }
            final String var = PokedexEntryLoader.gson.toJson(thing);
            args = args + var;
            final StructureEvent.ReadTag event = new ReadTag(args, vec.getPos(), player.getEntityWorld(),
                    (ServerWorld) player.getEntityWorld(), player.getRNG(), MutableBoundingBox.getNewBoundingBox());
            TrainerSpawnHandler.StructureSpawn(event);
            break;
        case UPDATETRAINER:
            if (!PermissionAPI.hasPermission(player, PacketTrainer.EDITTRAINER))
            {
                player.sendMessage(new StringTextComponent(TextFormatting.RED + "You are not allowed to do that."),
                        Util.DUMMY_UUID);
                return;
            }

            // Here we are editing a rewards list for a trainer
            if (this.getTag().contains("__rewards__"))
            {
                final IHasRewards rewards = TrainerCaps.getHasRewards(mob);
                if (rewards instanceof ICapabilitySerializable) try
                {
                    @SuppressWarnings("unchecked")
                    final ICapabilitySerializable<INBT> ser = (ICapabilitySerializable<INBT>) rewards;
                    ser.deserializeNBT(this.getTag().get("__rewards__"));
                    Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent("Updated rewards list"),
                            true);
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
                if (aiStates instanceof ICapabilitySerializable) try
                {
                    @SuppressWarnings("unchecked")
                    final ICapabilitySerializable<INBT> ser = (ICapabilitySerializable<INBT>) aiStates;
                    ser.deserializeNBT(this.getTag().get("__ai__"));
                    Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent("Updated AI Setting"),
                            true);
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
                if (messages instanceof ICapabilitySerializable) try
                {
                    @SuppressWarnings("unchecked")
                    final ICapabilitySerializable<INBT> ser = (ICapabilitySerializable<INBT>) messages;
                    ser.deserializeNBT(this.getTag().get("__messages__"));
                    Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent("Updated AI Setting"),
                            true);
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }

            // Here we edit the mob itself
            if (!type.isEmpty())
            {
                mobHolder = TrainerCaps.getHasPokemobs(mob);
                if (mob instanceof NpcMob)
                {
                    final NpcMob npc = (NpcMob) mob;
                    final NpcType newType = mobHolder != null ? TypeTrainer.getTrainer(type) : NpcType.byType(type);
                    npc.setNpcType(newType);
                    npc.setMale(male);
                    if (this.getTag().getBoolean("rawName")) npc.name = name;
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
                player.sendMessage(new StringTextComponent(TextFormatting.RED + "You are not allowed to do that."),
                        Util.DUMMY_UUID);
                return;
            }
            mob = player.getEntityWorld().getEntityByID(id);
            if (mob != null) mob.remove();
            break;
        case UPDATEMOB:
            mob = player.getEntityWorld().getEntityByID(id);
            mobHolder = TrainerCaps.getHasPokemobs(mob);
            // This means we are editing a mob of a trainer.
            if (this.getTag().contains("__trainers__") && mobHolder != null)
            {
                final int index = this.getTag().getInt("__trainers__");
                ItemStack cube = ItemStack.EMPTY;
                if (this.getTag().contains("__pokemob__"))
                {
                    final CompoundNBT mobtag = this.getTag().getCompound("__pokemob__");
                    cube = ItemStack.read(mobtag);
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
                    final CompoundNBT mobtag = this.getTag().getCompound("__pokemob__");
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
                    level = mobtag.getInt("l");
                    final String nature = mobtag.getString("n");
                    final float size = mobtag.getFloat("s");
                    final boolean shiny = mobtag.getBoolean("sh");
                    male = mobtag.getBoolean("g");
                    final byte old = pokemob.getSexe();
                    if (old == IPokemob.MALE || old == IPokemob.FEMALE)
                    {
                        final byte newSexe = old == IPokemob.MALE ? IPokemob.FEMALE : IPokemob.MALE;
                        pokemob.setSexe(newSexe);
                    }
                    pokemob.setNature(Nature.valueOf(nature));
                    pokemob.setAbility(AbilityManager.getAbility(ability));
                    pokemob.setShiny(shiny);
                    pokemob.setSize(size);

                    pokemob.onGenesChanged();
                    EntityUpdate.sendEntityUpdate(mob);
                }
            }
            break;
        }
    }
}
