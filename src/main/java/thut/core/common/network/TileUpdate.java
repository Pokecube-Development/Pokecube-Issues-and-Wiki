package thut.core.common.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.fml.network.PacketDistributor;
import thut.core.common.ThutCore;

public class TileUpdate extends NBTPacket
{
    public static final PacketAssembly<TileUpdate> ASSEMBLER = PacketAssembly.registerAssembler(TileUpdate.class,
            TileUpdate::new, ThutCore.packets);

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
        final IChunk chunk = tile.getWorld().getChunk(tile.getPos());
        if (chunk instanceof Chunk && ((Chunk) chunk).getWorld().getChunkProvider() instanceof ServerChunkProvider)
            TileUpdate.ASSEMBLER.sendTo(message, PacketDistributor.TRACKING_CHUNK.with(() -> (Chunk) chunk));
    }

    public TileUpdate()
    {
        super();
    }

    public TileUpdate(final CompoundNBT tag)
    {
        super();
        this.tag = tag;
    }

    public TileUpdate(final PacketBuffer buffer)
    {
        super(buffer);
    }

    @Override
    protected void onCompleteClient()
    {
        final PlayerEntity player = ThutCore.proxy.getPlayer();
        final BlockPos pos = NBTUtil.readBlockPos(this.tag.getCompound("pos"));
        final TileEntity tile = player.getEntityWorld().getTileEntity(pos);
        if (tile != null) tile.handleUpdateTag(this.tag.getCompound("tag"));
    }
}
