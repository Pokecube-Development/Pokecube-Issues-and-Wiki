package pokecube.nbtedit.packets;

import org.apache.logging.log4j.Level;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
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

    public TileNBTPacket(final BlockPos pos, final CompoundTag tag)
    {
        super();
        tag.putLong("_nbtedit_pos", pos.asLong());
        this.tag = tag;

    }

    public TileNBTPacket(final FriendlyByteBuf buf)
    {
        super(buf);
    }

    @Override
    protected void onCompleteClient()
    {
        this.pos = BlockPos.of(this.getTag().getLong("_nbtedit_pos"));
        this.getTag().remove("_nbtedit_pos");
        NBTEdit.proxy.openEditGUI(this.pos, this.getTag());
    }

    @Override
    protected void onCompleteServer(final ServerPlayer player)
    {
        this.pos = BlockPos.of(this.getTag().getLong("_nbtedit_pos"));
        this.getTag().remove("_nbtedit_pos");
        final BlockEntity te = player.level.getBlockEntity(this.pos);
        if (te != null && NBTEdit.proxy.checkPermission(player)) try
        {
            te.load(player.level.getBlockState(this.pos), this.getTag());
            te.setChanged();// Ensures changes gets saved to disk later on.
            if (te.hasLevel() && te.getLevel() instanceof ServerLevel) ((ServerLevel) te.getLevel()).getChunkSource()
                    .blockChanged(this.pos);
            NBTEdit.log(Level.TRACE, player.getName().getString() + " edited a tag -- Tile Entity at " + this.pos.getX()
                    + ", " + this.pos.getY() + ", " + this.pos.getZ());
            NBTEdit.logTag(this.getTag());
            NBTEdit.proxy.sendMessage(player, "Your changes have been saved", ChatFormatting.WHITE);
        }
        catch (final Throwable t)
        {
            NBTEdit.proxy.sendMessage(player, "Save Failed - Invalid NBT format for Tile Entity", ChatFormatting.RED);
            NBTEdit.log(Level.WARN, player.getName().getString() + " edited a tag and caused an exception");
            NBTEdit.logTag(this.getTag());
            NBTEdit.throwing("TileNBTPacket", "Handler.onMessage", t);
        }
        else
        {
            NBTEdit.log(Level.WARN, player.getName().getString() + " tried to edit a non-existent TileEntity at "
                    + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ());
            NBTEdit.proxy.sendMessage(player, "cSave Failed - There is no TileEntity at " + this.pos.getX() + ", "
                    + this.pos.getY() + ", " + this.pos.getZ(), ChatFormatting.RED);
        }
    }
}
