package thut.core.common.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import thut.core.common.ThutCore;

public class TileUpdate extends Packet
{

    public static void sendUpdate(final TileEntity tile)
    {
        if (tile.getWorld().isRemote)
        {
            ThutCore.LOGGER.error("Packet sent on wrong side!");
            return;
        }
        final CompoundNBT tag = new CompoundNBT();
        final CompoundNBT pos = NBTUtil.writeBlockPos(tile.getPos());
        tag.put("pos", pos);
        final CompoundNBT mobtag = tile.getUpdateTag();
        tag.put("tag", mobtag);
        final TileUpdate message = new TileUpdate(tag);
        ThutCore.packets.sendToTracking(message, tile.getWorld().getChunk(tile.getPos()));
    }

    CompoundNBT tag;

    public TileUpdate(final CompoundNBT tag)
    {
        super(null);
        this.tag = tag;
    }

    public TileUpdate(final PacketBuffer buffer)
    {
        super(buffer);
        this.tag = buffer.readCompoundTag();
    }

    @Override
    public void handleClient()
    {
        final PlayerEntity player = ThutCore.proxy.getPlayer();
        final BlockPos pos = NBTUtil.readBlockPos(this.tag.getCompound("pos"));
        final TileEntity tile = player.getEntityWorld().getTileEntity(pos);
        if (tile != null) tile.handleUpdateTag(this.tag.getCompound("tag"));
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeCompoundTag(this.tag);
    }
}
