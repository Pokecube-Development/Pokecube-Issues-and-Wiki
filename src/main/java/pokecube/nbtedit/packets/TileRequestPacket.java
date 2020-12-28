package pokecube.nbtedit.packets;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
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

    public TileRequestPacket(final PacketBuffer buf)
    {
        this.pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        NBTEdit.log(Level.TRACE, player.getName().getString() + " requested tileEntity at " + this.pos.getX() + ", " + this.pos
                .getY() + ", " + this.pos.getZ());
        PacketHandler.sendTile(player, this.pos);
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        buf.writeLong(this.pos.toLong());
    }

}
