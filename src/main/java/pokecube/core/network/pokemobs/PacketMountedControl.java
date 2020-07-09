package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
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
        packet.y = (float) controller.throttle;
        PokecubeCore.packets.sendToServer(packet);
    }

    public static void sendUpdatePacket(final Entity pokemob)
    {
        final PacketMountedControl packet = new PacketMountedControl();
        packet.entityId = pokemob.getEntityId();
        final Vector3d pos = pokemob.getPositionVector();
        packet.message = 0;
        packet.x = (float) pos.x;
        packet.y = (float) pos.y;
        packet.z = (float) pos.z;
        PokecubeCore.packets.sendToTracking(packet, pokemob);
        packet.message = 1;
        packet.x = (float) pokemob.prevPosX;
        packet.y = (float) pokemob.prevPosY;
        packet.z = (float) pokemob.prevPosZ;
        PokecubeCore.packets.sendToTracking(packet, pokemob);
        packet.message = 2;
        packet.x = pokemob.rotationYaw;
        packet.y = pokemob.prevRotationYaw;
        PokecubeCore.packets.sendToTracking(packet, pokemob);
    }

    int  entityId;
    byte message;

    float x;
    float y;
    float z;

    public PacketMountedControl()
    {
        super(null);
    }

    public PacketMountedControl(final PacketBuffer buf)
    {
        super(buf);
        this.entityId = buf.readInt();
        this.message = buf.readByte();
        this.x = buf.readFloat();
        this.y = buf.readFloat();
        this.z = buf.readFloat();
    }

    @Override
    public void handleClient()
    {
        final Entity mob = PokecubeCore.proxy.getWorld().getEntityByID(this.entityId);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (mob != null && mob.getControllingPassenger() != PokecubeCore.proxy.getPlayer()) switch (this.message)
        {
        case 0:
            mob.setPosition(this.x, this.y, this.z);
            break;
        case 1:
            mob.prevPosX = this.x;
            mob.prevPosY = this.y;
            mob.prevPosZ = this.z;
            break;
        case 2:
            mob.rotationYaw = this.x;
            mob.prevRotationYaw = this.y;

            if (pokemob != null) pokemob.setHeading(this.x);

            break;
        }
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final Entity mob = player.getEntityWorld().getEntityByID(this.entityId);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob != null && pokemob.getController() != null)
        {
            final Entity entity = pokemob.getEntity().getControllingPassenger();
            if (entity == null || !entity.getUniqueID().equals(player.getUniqueID())) return;
            final LogicMountedControl controller = pokemob.getController();
            controller.forwardInputDown = (this.message & PacketMountedControl.FORWARD) > 0;
            controller.backInputDown = (this.message & PacketMountedControl.BACK) > 0;
            controller.leftInputDown = (this.message & PacketMountedControl.LEFT) > 0;
            controller.rightInputDown = (this.message & PacketMountedControl.RIGHT) > 0;
            controller.upInputDown = (this.message & PacketMountedControl.UP) > 0;
            controller.downInputDown = (this.message & PacketMountedControl.DOWN) > 0;
            controller.followOwnerLook = (this.message & PacketMountedControl.SYNCLOOK) > 0;
            controller.throttle = this.y;
            mob.getPersistentData().putDouble("pokecube:mob_throttle", this.y);
            PacketMountedControl.sendUpdatePacket(mob);
        }
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        buf.writeInt(this.entityId);
        buf.writeByte(this.message);
        buf.writeFloat(this.x);
        buf.writeFloat(this.y);
        buf.writeFloat(this.z);
    }
}
