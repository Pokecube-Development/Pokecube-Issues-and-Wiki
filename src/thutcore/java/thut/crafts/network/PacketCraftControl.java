package thut.crafts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import thut.core.common.network.Packet;
import thut.crafts.ThutCrafts;
import thut.crafts.entity.CraftController;
import thut.crafts.entity.EntityCraft;

public class PacketCraftControl extends Packet
{

    private static final short FORWARD = 1;
    private static final short BACK = 2;
    private static final short LEFT = 4;
    private static final short RIGHT = 8;
    private static final short UP = 16;
    private static final short DOWN = 32;
    private static final short RLEFT = 64;
    private static final short RRIGHT = 128;

    public static void sendControlPacket(final Entity craft, final CraftController controller)
    {
        final PacketCraftControl packet = new PacketCraftControl();
        packet.entityId = craft.getId();
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

    public PacketCraftControl()
    {
    }

    public void read(final FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
        this.message = buffer.readShort();
    }

    /*
     * Handles Server side interaction.
     */
    @Override
    public void handleServer(ServerPlayer player)
    {
        final Entity mob = player.level().getEntity(this.entityId);
        if (mob instanceof EntityCraft craft)
        {
            final CraftController controller = craft.controller;
            controller.forwardInputDown = (this.message & PacketCraftControl.FORWARD) > 0;
            controller.backInputDown = (this.message & PacketCraftControl.BACK) > 0;
            controller.leftInputDown = (this.message & PacketCraftControl.LEFT) > 0;
            controller.rightInputDown = (this.message & PacketCraftControl.RIGHT) > 0;
            controller.upInputDown = (this.message & PacketCraftControl.UP) > 0;
            controller.downInputDown = (this.message & PacketCraftControl.DOWN) > 0;
            controller.leftRotateDown = (this.message & PacketCraftControl.RLEFT) > 0;
            controller.rightRotateDown = (this.message & PacketCraftControl.RRIGHT) > 0;
        }
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeShort(this.message);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("thutcrafts:craft_control"));
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
