package pokecube.adventures.network;

import com.google.gson.JsonObject;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.adventures.client.gui.items.editor.EditorGui;
import pokecube.adventures.events.TrainerSpawnHandler;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.StructureEvent.ReadTag;
import pokecube.core.handlers.events.SpawnEventsHandler.GuardInfo;
import pokecube.core.utils.CapHolders;
import thut.api.maths.Vector3;
import thut.core.common.network.Packet;

public class PacketTrainer extends Packet
{
    public static final String EDITSELF     = "pokecube_adventures.traineredit.self";
    public static final String EDITOTHER    = "pokecube_adventures.traineredit.other";
    public static final String EDITMOB      = "pokecube_adventures.traineredit.mob";
    public static final String EDITTRAINER  = "pokecube_adventures.traineredit.trainer";
    public static final String SPAWNTRAINER = "pokecube_adventures.traineredit.spawn";

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

    public static void sendEditOpenPacket(final Entity target, final ServerPlayerEntity editor)
    {
        final String node = target == editor || target == null ? editor.isSneaking() ? PacketTrainer.EDITSELF
                : PacketTrainer.SPAWNTRAINER
                : target instanceof ServerPlayerEntity ? PacketTrainer.EDITOTHER
                        : TrainerCaps.getHasPokemobs(target) != null ? PacketTrainer.EDITTRAINER
                                : PacketTrainer.EDITMOB;
        final boolean canEdit = !editor.getServer().isDedicatedServer() || PermissionAPI.hasPermission(editor, node);

        if (!canEdit)
        {
            editor.sendMessage(new StringTextComponent(TextFormatting.RED + "You are not allowed to do that."));
            return;
        }
        final PacketTrainer packet = new PacketTrainer(PacketTrainer.UPDATETRAINER);
        packet.data.putBoolean("O", true);
        packet.data.putInt("I", target == null ? -1 : target.getEntityId());

        if (target != null)
        {
            final CompoundNBT tag = new CompoundNBT();
            final IHasNPCAIStates ai = TrainerCaps.getNPCAIStates(target);
            final IGuardAICapability guard = target.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);
            final IHasPokemobs pokemobs = TrainerCaps.getHasPokemobs(target);
            if (ai != null) tag.put("A", CapabilityNPCAIStates.storage.writeNBT(TrainerCaps.AISTATES_CAP, ai, null));
            if (guard != null) tag.put("G", CapHolders.GUARDAI_CAP.getStorage().writeNBT(CapHolders.GUARDAI_CAP, guard,
                    null));
            if (pokemobs != null) tag.put("P", CapabilityHasPokemobs.storage.writeNBT(TrainerCaps.HASPOKEMOBS_CAP,
                    pokemobs, null));
            packet.data.put("C", tag);
        }
        PokecubeAdv.packets.sendTo(packet, editor);
    }

    byte               message;
    public CompoundNBT data = new CompoundNBT();

    public PacketTrainer()
    {
    }

    public PacketTrainer(final byte message)
    {
        this.message = message;
    }

    public PacketTrainer(final PacketBuffer buffer)
    {
        super(buffer);
        this.message = buffer.readByte();
        this.data = buffer.readCompoundTag();
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeByte(this.message);
        buffer.writeCompoundTag(this.data);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        switch (this.message)
        {
        case UPDATETRAINER:

            // O for Open Gui Packet.
            if (this.data.getBoolean("O"))
            {
                final int id = this.data.getInt("I");
                final Entity mob = player.getEntityWorld().getEntityByID(id);
                if (mob != null && this.data.contains("C"))
                {
                    final CompoundNBT nbt = this.data.getCompound("C");
                    final IHasNPCAIStates ai = TrainerCaps.getNPCAIStates(mob);
                    final IGuardAICapability guard = mob.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
                    final IHasPokemobs pokemobs = TrainerCaps.getHasPokemobs(mob);
                    if (nbt.contains("A")) if (ai != null) CapabilityNPCAIStates.storage.readNBT(
                            TrainerCaps.AISTATES_CAP, ai, null, nbt.get("A"));
                    if (nbt.contains("G")) if (guard != null) CapHolders.GUARDAI_CAP.getStorage().readNBT(
                            CapHolders.GUARDAI_CAP, guard, null, nbt.get("G"));
                    if (nbt.contains("P")) if (pokemobs != null) CapabilityHasPokemobs.storage.readNBT(
                            TrainerCaps.HASPOKEMOBS_CAP, pokemobs, null, nbt.get("P"));
                }
                net.minecraft.client.Minecraft.getInstance().displayGuiScreen(new EditorGui(mob));
                return;
            }

            break;
        }
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        switch (this.message)
        {
        case SPAWN:

            if (!PermissionAPI.hasPermission(player, PacketTrainer.SPAWNTRAINER))
            {
                player.sendMessage(new StringTextComponent(TextFormatting.RED + "You are not allowed to do that."));
                return;
            }

            final String type = this.data.getString("T");
            final int level = this.data.getInt("L");
            final boolean leader = this.data.getBoolean("C");
            final Vector3 vec = Vector3.getNewVector().set(player);
            String args = "pokecube_adventures:" + (leader ? "leader" : "trainer");
            final JsonObject thing = new JsonObject();
            thing.addProperty("level", level);
            thing.addProperty("trainerType", type);
            final GuardInfo info = new GuardInfo();
            info.time = "day";
            info.roam = 2;
            thing.add("guard", PokedexEntryLoader.gson.toJsonTree(info));
            final String var = PokedexEntryLoader.gson.toJson(thing);
            args = args + var;
            final StructureEvent.ReadTag event = new ReadTag(args, vec.getPos(), player.getEntityWorld(), player
                    .getRNG(), MutableBoundingBox.getNewBoundingBox());
            TrainerSpawnHandler.StructureSpawn(event);
            break;
        }
    }
}
