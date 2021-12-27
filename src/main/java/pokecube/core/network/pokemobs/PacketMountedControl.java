package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.network.Packet;

public class PacketMountedControl extends Packet
{
    //@formatter:off
    private static final byte FORWARD  = 1 >> 0;
    private static final byte BACK     = 1 >> 1;
    private static final byte LEFT     = 1 >> 2;
    private static final byte RIGHT    = 1 >> 3;
    private static final byte UP       = 1 >> 4;
    private static final byte DOWN     = 1 >> 5;
    private static final byte SYNCLOOK = 1 >> 6;
    private static final byte PATHS    = 1 >> 7;
    //@formatter:on

    public static void sendControlPacket(final Entity pokemob, final LogicMountedControl controller)
    {
        final PacketMountedControl packet = new PacketMountedControl();
        packet.entityId = pokemob.getId();
        if (controller.backInputDown) packet.message |= PacketMountedControl.BACK;
        if (controller.forwardInputDown) packet.message |= PacketMountedControl.FORWARD;
        if (controller.leftInputDown) packet.message |= PacketMountedControl.LEFT;
        if (controller.rightInputDown) packet.message |= PacketMountedControl.RIGHT;
        if (controller.upInputDown) packet.message |= PacketMountedControl.UP;
        if (controller.downInputDown) packet.message |= PacketMountedControl.DOWN;
        if (controller.followOwnerLook) packet.message |= PacketMountedControl.SYNCLOOK;
        if (controller.canPathWhileRidden) packet.message |= PacketMountedControl.SYNCLOOK;
        packet.y = (float) controller.throttle;
        controller.refreshInput();
        PokecubeCore.packets.sendToServer(packet);
    }

    public static void sendUpdatePacket(final Entity pokemob)
    {
        final PacketMountedControl packet = new PacketMountedControl();
        packet.entityId = pokemob.getId();
        final Vec3 pos = pokemob.position();
        packet.message = 0;
        packet.x = (float) pos.x;
        packet.y = (float) pos.y;
        packet.z = (float) pos.z;
        PokecubeCore.packets.sendToTracking(packet, pokemob);
        packet.message = 1;
        packet.x = (float) pokemob.xo;
        packet.y = (float) pokemob.yo;
        packet.z = (float) pokemob.zo;
        PokecubeCore.packets.sendToTracking(packet, pokemob);
        packet.message = 2;
        packet.x = pokemob.yRot;
        packet.y = pokemob.yRotO;
        PokecubeCore.packets.sendToTracking(packet, pokemob);
    }

    int entityId;
    byte message;

    float x;
    float y;
    float z;

    public PacketMountedControl()
    {
        super(null);
    }

    public PacketMountedControl(final FriendlyByteBuf buf)
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
        final Entity mob = PokecubeCore.proxy.getWorld().getEntity(this.entityId);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (mob != null && mob.getControllingPassenger() != PokecubeCore.proxy.getPlayer()) switch (this.message)
        {
        case 0:
            mob.setPos(this.x, this.y, this.z);
            break;
        case 1:
            mob.xo = this.x;
            mob.yo = this.y;
            mob.zo = this.z;
            break;
        case 2:
            mob.yRot = this.x;
            mob.yRotO = this.y;

            if (pokemob != null) pokemob.setHeading(this.x);

            break;
        }
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final Entity mob = player.getCommandSenderWorld().getEntity(this.entityId);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob != null && pokemob.getController() != null)
        {
            final Entity entity = pokemob.getEntity().getControllingPassenger();
            if (entity == null || !entity.getUUID().equals(player.getUUID())) return;
            final LogicMountedControl controller = pokemob.getController();
            controller.forwardInputDown = (this.message & PacketMountedControl.FORWARD) > 0;
            controller.backInputDown = (this.message & PacketMountedControl.BACK) > 0;
            controller.leftInputDown = (this.message & PacketMountedControl.LEFT) > 0;
            controller.rightInputDown = (this.message & PacketMountedControl.RIGHT) > 0;
            controller.upInputDown = (this.message & PacketMountedControl.UP) > 0;
            controller.downInputDown = (this.message & PacketMountedControl.DOWN) > 0;
            controller.followOwnerLook = (this.message & PacketMountedControl.SYNCLOOK) > 0;
            controller.canPathWhileRidden = (this.message & PacketMountedControl.PATHS) > 0;
            controller.throttle = this.y;
            controller.refreshInput();
            mob.getPersistentData().putDouble("pokecube:mob_throttle", this.y);
            PacketMountedControl.sendUpdatePacket(mob);
        }
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.entityId);
        buf.writeByte(this.message);
        buf.writeFloat(this.x);
        buf.writeFloat(this.y);
        buf.writeFloat(this.z);
    }
}
