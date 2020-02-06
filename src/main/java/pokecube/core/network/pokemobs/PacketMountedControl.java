package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.network.Packet;

public class PacketMountedControl extends Packet
{
    private static final byte FORWARD  = 1;
    private static final byte BACK     = 2;
    private static final byte LEFT     = 4;
    private static final byte RIGHT    = 8;
    private static final byte UP       = 16;
    private static final byte DOWN     = 32;
    private static final byte SYNCLOOK = 64;

    public static void sendControlPacket(final Entity pokemob, final LogicMountedControl controller)
    {
        final PacketMountedControl packet = new PacketMountedControl();
        packet.entityId = pokemob.getEntityId();
        if (controller.backInputDown) packet.message += PacketMountedControl.BACK;
        if (controller.forwardInputDown) packet.message += PacketMountedControl.FORWARD;
        if (controller.leftInputDown) packet.message += PacketMountedControl.LEFT;
        if (controller.rightInputDown) packet.message += PacketMountedControl.RIGHT;
        if (controller.upInputDown) packet.message += PacketMountedControl.UP;
        if (controller.downInputDown) packet.message += PacketMountedControl.DOWN;
        if (controller.followOwnerLook) packet.message += PacketMountedControl.SYNCLOOK;
        packet.throttle = (float) controller.throttle;
        PokecubeCore.packets.sendToServer(packet);
    }

    int  entityId;
    byte message;

    float throttle;

    public PacketMountedControl()
    {
        super(null);
    }

    public PacketMountedControl(final PacketBuffer buf)
    {
        super(buf);
        this.entityId = buf.readInt();
        this.message = buf.readByte();
        this.throttle = buf.readFloat();
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final Entity mob = player.getEntityWorld().getEntityByID(this.entityId);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob != null && pokemob.getController() != null)
        {
            if (pokemob.getOwner() != player) return;
            final LogicMountedControl controller = pokemob.getController();
            controller.forwardInputDown = (this.message & PacketMountedControl.FORWARD) > 0;
            controller.backInputDown = (this.message & PacketMountedControl.BACK) > 0;
            controller.leftInputDown = (this.message & PacketMountedControl.LEFT) > 0;
            controller.rightInputDown = (this.message & PacketMountedControl.RIGHT) > 0;
            controller.upInputDown = (this.message & PacketMountedControl.UP) > 0;
            controller.downInputDown = (this.message & PacketMountedControl.DOWN) > 0;
            controller.followOwnerLook = (this.message & PacketMountedControl.SYNCLOOK) > 0;
            controller.throttle = this.throttle;
            mob.getPersistentData().putDouble("pokecube:mob_throttle", this.throttle);
        }
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        buf.writeInt(this.entityId);
        buf.writeByte(this.message);
        buf.writeFloat(this.throttle);
    }
}
