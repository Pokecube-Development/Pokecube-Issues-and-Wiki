package thut.core.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import thut.core.common.ThutCore;

public class TileUpdate extends NBTPacket
{
    public static final PacketAssembly<TileUpdate> ASSEMBLER = PacketAssembly.registerAssembler(TileUpdate.class,
            TileUpdate::new, ThutCore.packets);

    public static void sendUpdate(final BlockEntity tile)
    {
        if (tile.getLevel().isClientSide)
        {
            ThutCore.LOGGER.error("Packet sent on wrong side!");
            return;
        }
        final CompoundTag tag = new CompoundTag();
        final CompoundTag pos = NbtUtils.writeBlockPos(tile.getBlockPos());
        tag.put("pos", pos);
        final CompoundTag mobtag = tile.getUpdateTag();
        tag.put("tag", mobtag);
        final TileUpdate message = new TileUpdate(tag);
        final ChunkAccess chunk = tile.getLevel().getChunk(tile.getBlockPos());
        if (chunk instanceof LevelChunk && ((LevelChunk) chunk).getLevel().getChunkSource() instanceof ServerChunkCache)
            TileUpdate.ASSEMBLER.sendTo(message, PacketDistributor.TRACKING_CHUNK.with(() -> (LevelChunk) chunk));
    }

    public TileUpdate()
    {
        super();
    }

    public TileUpdate(final CompoundTag tag)
    {
        super();
        this.tag = tag;
    }

    public TileUpdate(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient()
    {
        final Level world = net.minecraft.client.Minecraft.getInstance().level;
        final BlockPos pos = NbtUtils.readBlockPos(this.tag.getCompound("pos"));
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile != null) tile.handleUpdateTag(this.tag.getCompound("tag"));
    }
}
