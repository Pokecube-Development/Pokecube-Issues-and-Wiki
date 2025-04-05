package pokecube.nbtedit.packets;

import org.apache.logging.log4j.Level;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.nbtedit.NBTEdit;
import thut.core.common.network.Packet;

public class TileRequestPacket extends Packet
{
    /** The position of the tileEntity requested. */
    private BlockPos pos;

    /** Required default constructor. */
    public TileRequestPacket()
    {
    }

    public TileRequestPacket(final BlockPos pos)
    {
        this.pos = pos;
    }

    public void read(final FriendlyByteBuf buf)
    {
        this.pos = BlockPos.of(buf.readLong());
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        NBTEdit.log(Level.TRACE, player.getName().getString() + " requested tileEntity at " + this.pos.getX() + ", " + this.pos
                .getY() + ", " + this.pos.getZ());
        PacketHandler.sendTile(player, this.pos);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeLong(this.pos.asLong());
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:nbtedit_tile_request"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

}
