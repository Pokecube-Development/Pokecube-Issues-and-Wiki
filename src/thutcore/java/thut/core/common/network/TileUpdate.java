package thut.core.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thut.core.common.ThutCore;
import thut.core.common.network.nbtpacket.NBTPacket;
import thut.core.common.network.nbtpacket.PacketAssembly;

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
        var pos = NbtUtils.writeBlockPos(tile.getBlockPos());
        var pos_tag = new CompoundTag();
        pos_tag.put("v", pos);
        tag.put("pos", pos_tag);
        final CompoundTag mobtag = tile.getUpdateTag(tile.getLevel().registryAccess());
        tag.put("tag", mobtag);
        final ChunkAccess chunk = tile.getLevel().getChunk(tile.getBlockPos());
        if (chunk instanceof LevelChunk lchunk && lchunk.getLevel().getChunkSource() instanceof ServerChunkCache)
            TileUpdate.ASSEMBLER.sendToTracking(tag, lchunk);
    }

    public TileUpdate()
    {
        super();
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient(Player player)
    {
        final Level world = player.level;
        final BlockPos pos = NbtUtils.readBlockPos(this.tag.getCompound("pos"), "v").get();
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile != null) tile.handleUpdateTag(this.tag.getCompound("tag"), world.registryAccess());
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("thutcore:tile_sync"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
