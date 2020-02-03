package pokecube.adventures.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.handlers.events.EventsHandler;
import thut.core.common.network.Packet;

public class PacketTrainer extends Packet
{
    public static final String EDITSELF     = "pokecube_adventures.traineredit.self";
    public static final String EDITOTHER    = "pokecube_adventures.traineredit.other";
    public static final String EDITMOB      = "pokecube_adventures.traineredit.mob";
    public static final String EDITTRAINER  = "pokecube_adventures.traineredit.trainer";
    public static final String SPAWNTRAINER = "pokecube_adventures.traineredit.spawn";

    public static final byte MESSAGEUPDATETRAINER = 0;
    public static final byte MESSAGENOTIFYDEFEAT  = 1;
    public static final byte MESSAGEKILLTRAINER   = 2;
    public static final byte MESSAGEUPDATEMOB     = 3;
    public static final byte MESSAGESPAWNTRAINER  = 4;

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
        final String node = target == editor || target == null ? editor.isCrouching() ? PacketTrainer.EDITSELF
                : PacketTrainer.SPAWNTRAINER
                : target instanceof ServerPlayerEntity ? PacketTrainer.EDITOTHER
                        : CapabilityHasPokemobs.getHasPokemobs(target) != null ? PacketTrainer.EDITTRAINER
                                : PacketTrainer.EDITMOB;
        final boolean canEdit = !editor.getServer().isDedicatedServer() || PermissionAPI.hasPermission(editor, node);

        if (!canEdit)
        {
            editor.sendMessage(new StringTextComponent(TextFormatting.RED + "You are not allowed to do that."));
            return;
        }
        final PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
        packet.data.putBoolean("O", true);
        packet.data.putInt("I", target == null ? -1 : target.getEntityId());

        if (target != null)
        {
            final CompoundNBT tag = new CompoundNBT();
            final IHasNPCAIStates ai = CapabilityNPCAIStates.getNPCAIStates(target);
            final IGuardAICapability guard = target.getCapability(EventsHandler.GUARDAI_CAP, null).orElse(null);
            final IHasPokemobs pokemobs = CapabilityHasPokemobs.getHasPokemobs(target);
            if (ai != null) tag.put("A", CapabilityNPCAIStates.storage.writeNBT(CapabilityNPCAIStates.AISTATES_CAP, ai,
                    null));
            if (guard != null) tag.put("G", EventsHandler.GUARDAI_CAP.getStorage().writeNBT(EventsHandler.GUARDAI_CAP,
                    guard, null));
            if (pokemobs != null) tag.put("P", CapabilityHasPokemobs.storage.writeNBT(
                    CapabilityHasPokemobs.HASPOKEMOBS_CAP, pokemobs, null));
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

}
