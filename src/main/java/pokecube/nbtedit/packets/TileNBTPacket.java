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
import thut.core.common.network.NBTPacket;
import thut.core.common.network.PacketAssembly;

public class TileNBTPacket extends NBTPacket
{
    public static final PacketAssembly<TileNBTPacket> ASSEMBLER = PacketAssembly.registerAssembler(TileNBTPacket.class,
            TileNBTPacket::new, PacketHandler.INSTANCE);

    /** The block of the tileEntity. */
    protected BlockPos pos;

    /** Required default constructor. */
    public TileNBTPacket()
    {
        super();
    }

    public TileNBTPacket(final BlockPos pos, final CompoundNBT tag)
    {
        super();
        tag.putLong("_nbtedit_pos", pos.toLong());
        this.tag = tag;

    }

    public TileNBTPacket(final PacketBuffer buf)
    {
        super(buf);
    }

    @Override
    protected void onCompleteClient()
    {
        this.pos = BlockPos.fromLong(this.getTag().getLong("_nbtedit_pos"));
        this.getTag().remove("_nbtedit_pos");
        NBTEdit.proxy.openEditGUI(this.pos, this.getTag());
    }

    @Override
    protected void onCompleteServer(final ServerPlayerEntity player)
    {
        this.pos = BlockPos.fromLong(this.getTag().getLong("_nbtedit_pos"));
        this.getTag().remove("_nbtedit_pos");
        final TileEntity te = player.world.getTileEntity(this.pos);
        if (te != null && NBTEdit.proxy.checkPermission(player)) try
        {
            te.read(player.world.getBlockState(this.pos), this.getTag());
            te.markDirty();// Ensures changes gets saved to disk later on.
            if (te.hasWorld() && te.getWorld() instanceof ServerWorld) ((ServerWorld) te.getWorld()).getChunkProvider()
                    .markBlockChanged(this.pos);
            NBTEdit.log(Level.TRACE, player.getName() + " edited a tag -- Tile Entity at " + this.pos.getX() + ", "
                    + this.pos.getY() + ", " + this.pos.getZ());
            NBTEdit.logTag(this.getTag());
            NBTEdit.proxy.sendMessage(player, "Your changes have been saved", TextFormatting.WHITE);
        }
        catch (final Throwable t)
        {
            NBTEdit.proxy.sendMessage(player, "Save Failed - Invalid NBT format for Tile Entity", TextFormatting.RED);
            NBTEdit.log(Level.WARN, player.getName() + " edited a tag and caused an exception");
            NBTEdit.logTag(this.getTag());
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
}
