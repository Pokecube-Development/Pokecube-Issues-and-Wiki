package thut.api.entity.blockentity.world;

import java.util.Map;
import java.util.function.BooleanSupplier;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;
import thut.api.entity.blockentity.IBlockEntity;

public class BlockEntityChunkProvider extends ChunkSource
{
    private final WorldEntity world;

    private final LevelLightEngine lightManager;

    private final Map<BlockPos, LevelChunk> chunks = Maps.newHashMap();

    private BlockPos lastOrigin = null;

    public BlockEntityChunkProvider(final WorldEntity worldIn)
    {
        this.world = worldIn;
        this.lightManager = new LevelLightEngine(this, true, worldIn.getWorld().dimensionType().hasSkyLight());
    }

    @Override
    public ChunkAccess getChunk(final int chunkX, final int chunkZ, final ChunkStatus status, final boolean load)
    {
        final AABB chunkBox = new AABB(chunkX * 16, 0, chunkZ * 16, chunkX * 16 + 15, this.world.getWorld()
                .getMaxBuildHeight(), chunkZ * 16 + 15);
        if (!this.intersects(chunkBox)) return this.world.getWorld().getChunk(chunkX, chunkZ);

        // TODO improvements to this.

        final Entity entity = (Entity) this.world.getBlockEntity();
        if (this.lastOrigin == null || !this.lastOrigin.equals(entity.blockPosition()))
        {
            this.lastOrigin = entity.blockPosition();
            this.chunks.clear();
        }

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        pos.set(chunkX, 0, chunkZ);
        final BlockPos immut = pos.immutable();
        if (this.chunks.containsKey(immut)) return this.chunks.get(immut);
        final ChunkPos cpos = new ChunkPos(chunkX, chunkZ);
        final LevelChunk ret = new EntityChunk(this.world, cpos);
        this.chunks.put(immut, ret);
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 256; j++)
                for (int k = 0; k < 16; k++)
                {
                    final int x = chunkX * 16 + i;
                    final int y = j;
                    final int z = chunkZ * 16 + k;
                    pos.set(x, y, z);
                    final BlockState state = this.world.getBlockState(pos);
                    if (state.getBlock() == Blocks.AIR) continue;
                    LevelChunkSection storage = ret.getSections()[j >> 4];
                    if (storage == null)
                    {
                        storage = new LevelChunkSection(j >> 4 << 4);
                        ret.getSections()[j >> 4] = storage;
                    }
                    storage.setBlockState(i & 15, j & 15, k & 15, state, false);
                    final BlockEntity tile = this.world.getBlockEntity(pos);
                    if (tile != null) ret.setBlockEntity(tile);
                }
        return ret;
    }

    @Override
    public LevelLightEngine getLightEngine()
    {
        return this.lightManager;
    }

    @Override
    public BlockGetter getLevel()
    {
        return this.world;
    }

    private boolean intersects(final AABB other)
    {
        final IBlockEntity mob = this.world.getBlockEntity();
        final AABB thisBox = ((Entity) mob).getBoundingBox();
        if (thisBox.intersects(other)) return true;
        return false;
    }

    @Override
    public String gatherStats()
    {
        return "BlockEntity: " + this.world + " " + this.world.getWorld();
    }

    @Override
    public void tick(final BooleanSupplier p_156184_)
    {
    }

    @Override
    public int getLoadedChunksCount()
    {
        return 0;
    }
}
