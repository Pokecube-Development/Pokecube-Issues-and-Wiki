package thut.api.entity.blockentity.world;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.lighting.WorldLightManager;
import thut.api.entity.blockentity.IBlockEntity;

public class BlockEntityChunkProvider extends AbstractChunkProvider
{
    private final WorldEntity          world;
    private final WorldLightManager    lightManager;

    private final Map<BlockPos, Chunk> chunks     = Maps.newHashMap();
    private BlockPos                   lastOrigin = null;

    public BlockEntityChunkProvider(final WorldEntity worldIn)
    {
        this.world = worldIn;
        this.lightManager = new WorldLightManager(this, true, worldIn.getWorld().dimensionType().hasSkyLight());
    }

    @Override
    public IChunk getChunk(final int chunkX, final int chunkZ, final ChunkStatus status, final boolean load)
    {
        final AxisAlignedBB chunkBox = new AxisAlignedBB(chunkX * 16, 0, chunkZ * 16, chunkX * 16 + 15,
                this.world.getWorld().getMaxBuildHeight(), chunkZ * 16 + 15);
        if (!this.intersects(chunkBox)) return this.world.getWorld().getChunk(chunkX, chunkZ);

        // TODO improvements to this.

        final Entity entity = (Entity) this.world.getBlockEntity();
        if (this.lastOrigin == null || !this.lastOrigin.equals(entity.blockPosition()))
        {
            this.lastOrigin = entity.blockPosition();
            this.chunks.clear();
        }

        final BlockPos.Mutable pos = new BlockPos.Mutable();
        pos.set(chunkX, 0, chunkZ);
        final BlockPos immut = pos.immutable();
        if (this.chunks.containsKey(immut)) return this.chunks.get(immut);
        final ChunkPrimer primer = new ChunkPrimer(new ChunkPos(chunkX, chunkZ), UpgradeData.EMPTY);

        final Chunk ret = new Chunk(this.world.getWorld(), primer);
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
                    ChunkSection storage = ret.getSections()[j >> 4];
                    if (storage == null)
                    {
                        storage = new ChunkSection(j >> 4 << 4);
                        ret.getSections()[j >> 4] = storage;
                    }
                    storage.setBlockState(i & 15, j & 15, k & 15, state, false);
                    final TileEntity tile = this.world.getBlockEntity(pos);
                    if (tile != null) ret.addBlockEntity(tile);
                }
        return ret;
    }

    @Override
    public WorldLightManager getLightEngine()
    {
        return this.lightManager;
    }

    @Override
    public IBlockReader getLevel()
    {
        return this.world;
    }

    private boolean intersects(final AxisAlignedBB other)
    {
        final IBlockEntity mob = this.world.getBlockEntity();
        final AxisAlignedBB thisBox = ((Entity) mob).getBoundingBox();
        if (thisBox.intersects(other)) return true;
        return false;
    }

    @Override
    public String gatherStats()
    {
        return "BlockEntity: " + this.world + " " + this.world.getWorld();
    }

    @Override
    public boolean isTickingChunk(final BlockPos pos)
    {
        return false;
    }
}
