package thut.core.common.terrain;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thut.api.level.terrain.BiomeType;
import thut.api.level.terrain.TerrainSegment;
import thut.api.level.terrain.TerrainSegment.ISubBiomeChecker;
import thut.api.maths.Vector3;

public class ConfigTerrainChecker implements ISubBiomeChecker
{
    private final List<Predicate<BlockState>> list;
    private final BiomeType                   subbiome;

    public ConfigTerrainChecker(final List<Predicate<BlockState>> list, final BiomeType subbiome)
    {
        this.list = list;
        this.subbiome = subbiome;
    }

    private boolean apply(final BlockState state)
    {
        if (state.is(Blocks.AIR) || state.is(Blocks.CAVE_AIR)) return false;
        for (final Predicate<BlockState> predicate : this.list)
            if (predicate.apply(state)) return true;
        return false;
    }

    @Override
    public BiomeType getSubBiome(final LevelAccessor world, final Vector3 v, final TerrainSegment segment,
            final boolean caveAdjusted)
    {
        if (caveAdjusted)
        {
            final Vector3 temp1 = new Vector3();
            final int x0 = segment.chunkX * 16, y0 = segment.chunkY * 16, z0 = segment.chunkZ * 16;
            final int dx = (v.intX() - x0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
            final int dy = (v.intY() - y0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
            final int dz = (v.intZ() - z0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
            final int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
            for (int i = x1; i < x1 + TerrainSegment.GRIDSIZE; i++)
                for (int j = y1; j < y1 + TerrainSegment.GRIDSIZE; j++)
                    for (int k = z1; k < z1 + TerrainSegment.GRIDSIZE; k++)
                    {
                        temp1.set(i, j, k);
                        if (this.apply(temp1.getBlockState(world))) return this.subbiome;
                    }
        }
        return BiomeType.NONE;
    }

}
