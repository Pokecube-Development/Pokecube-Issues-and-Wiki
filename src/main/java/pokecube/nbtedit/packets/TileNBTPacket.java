package pokecube.nbtedit.packets;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import pokecube.nbtedit.NBTEdit;
import pokecube.nbtedit.NBTHelper;
import thut.core.common.network.Packet;

public class TileNBTPacket extends Packet
{
    /** The block of the tileEntity. */
    protected BlockPos    pos;
    /** The nbt data of the tileEntity. */
    protected CompoundNBT tag;

    /** Required default constructor. */
    public TileNBTPacket()
    {
    }

    public TileNBTPacket(final BlockPos pos, final CompoundNBT tag)
    {
        this.pos = pos;
        this.tag = tag;
    }

    public TileNBTPacket(final PacketBuffer buf)
    {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.tag = NBTHelper.readNbtFromBuffer(buf);
    }

    @Override
    public void handleClient()
    {
        NBTEdit.proxy.openEditGUI(this.pos, this.tag);
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final TileEntity te = player.world.getTileEntity(this.pos);
        if (te != null && NBTEdit.proxy.checkPermission(player)) try
        {
            te.read(this.tag);
            te.markDirty();// Ensures changes gets saved to disk later on.
            if (te.hasWorld() && te.getWorld() instanceof ServerWorld) ((ServerWorld) te.getWorld()).getChunkProvider()
                    .markBlockChanged(this.pos);
            NBTEdit.log(Level.TRACE, player.getName() + " edited a tag -- Tile Entity at " + this.pos.getX() + ", "
                    + this.pos.getY() + ", " + this.pos.getZ());
            NBTEdit.logTag(this.tag);
            NBTEdit.proxy.sendMessage(player, "Your changes have been saved", TextFormatting.WHITE);
        }
        catch (final Throwable t)
        {
            NBTEdit.proxy.sendMessage(player, "Save Failed - Invalid NBT format for Tile Entity", TextFormatting.RED);
            NBTEdit.log(Level.WARN, player.getName() + " edited a tag and caused an exception");
            NBTEdit.logTag(this.tag);
            NBTEdit.throwing("TileNBTPacket", "Handler.onMessage", t);
        }
        else
        {
            NBTEdit.log(Level.WARN, player.getName() + " tried to edit a non-existent TileEntity at " + this.pos.getX()
                    + ", " + this.pos.getY() + ", " + this.pos.getZ());
            NBTEdit.proxy.sendMessage(player, "cSave Failed - There is no TileEntity at " + this.pos.getX() + ", "
                    + this.pos.getY() + ", " + this.pos.getZ(), TextFormatting.RED);
        }
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        buf.writeLong(this.pos.toLong());
        NBTHelper.writeToBuffer(this.tag, buf);
    }
}
