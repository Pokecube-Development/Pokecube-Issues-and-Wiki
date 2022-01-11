package thut.crafts.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import thut.core.common.network.Packet;
import thut.crafts.ThutCrafts;
import thut.crafts.entity.CraftController;
import thut.crafts.entity.EntityCraft;

public class PacketCraftControl extends Packet
{

    private static final short FORWARD = 1;
    private static final short BACK    = 2;
    private static final short LEFT    = 4;
    private static final short RIGHT   = 8;
    private static final short UP      = 16;
    private static final short DOWN    = 32;
    private static final short RLEFT   = 64;
    private static final short RRIGHT  = 128;

    public static void sendControlPacket(final Entity pokemob, final CraftController controller)
    {
        final PacketCraftControl packet = new PacketCraftControl();
        packet.entityId = pokemob.getId();
        if (controller.backInputDown) packet.message += PacketCraftControl.BACK;
        if (controller.forwardInputDown) packet.message += PacketCraftControl.FORWARD;
        if (controller.leftInputDown) packet.message += PacketCraftControl.LEFT;
        if (controller.rightInputDown) packet.message += PacketCraftControl.RIGHT;
        if (controller.upInputDown) packet.message += PacketCraftControl.UP;
        if (controller.downInputDown) packet.message += PacketCraftControl.DOWN;
        if (controller.leftRotateDown) packet.message += PacketCraftControl.RLEFT;
        if (controller.rightRotateDown) packet.message += PacketCraftControl.RRIGHT;
        ThutCrafts.packets.sendToServer(packet);
    }

    int entityId;

    short message;

    PacketCraftControl()
    {
        super(null);
    }

    public PacketCraftControl(final FriendlyByteBuf buffer)
    {
        super(buffer);
        this.entityId = buffer.readInt();
        this.message = buffer.readShort();
    }

    @Override
    public void handle(final Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            final Player player = ctx.get().getSender();
            final Entity mob = player.getLevel().getEntity(this.entityId);
            if (mob != null && mob instanceof EntityCraft)
            {
                final CraftController controller = ((EntityCraft) mob).controller;
                controller.forwardInputDown = (this.message & PacketCraftControl.FORWARD) > 0;
                controller.backInputDown = (this.message & PacketCraftControl.BACK) > 0;
                controller.leftInputDown = (this.message & PacketCraftControl.LEFT) > 0;
                controller.rightInputDown = (this.message & PacketCraftControl.RIGHT) > 0;
                controller.upInputDown = (this.message & PacketCraftControl.UP) > 0;
                controller.downInputDown = (this.message & PacketCraftControl.DOWN) > 0;
                controller.leftRotateDown = (this.message & PacketCraftControl.RLEFT) > 0;
                controller.rightRotateDown = (this.message & PacketCraftControl.RRIGHT) > 0;
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeShort(this.message);
    }
}
