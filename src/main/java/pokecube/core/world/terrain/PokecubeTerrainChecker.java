package pokecube.core.world.terrain;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import pokecube.core.PokecubeCore;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.api.terrain.TerrainChecker;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class PokecubeTerrainChecker extends TerrainChecker implements ISubBiomeChecker
{
    public static void init()
    {
        final PokecubeTerrainChecker checker = new PokecubeTerrainChecker();
        TerrainSegment.defaultChecker = checker;
    }

    @Override
    public BiomeType getSubBiome(final LevelAccessor world, final Vector3 v, final TerrainSegment segment,
            final boolean caveAdjusted)
    {
        if (!(world instanceof Level)) return BiomeType.NONE;
        final Level rworld = (Level) world;
        if (caveAdjusted)
        {
            final Set<StructureInfo> set = StructureManager.getFor(rworld.dimension(), v.getPos());
            for (final StructureInfo info : set)
            {
                String name = info.name;
                if (!name.contains(":")) name = "minecraft:" + name;
                String subbiome = PokecubeTerrainChecker.structureSubbiomeMap.get(name);
                if (subbiome == null && PokecubeCore.getConfig().structs_default_ruins) subbiome = "ruin";
                if (subbiome != null)
                {
                    final BiomeType biom = BiomeType.getBiome(subbiome, true);
                    return biom;
                }
            }
            if (rworld.dimensionType().hasCeiling() || v.canSeeSky(world)
                    || !PokecubeCore.getConfig().autoDetectSubbiomes)
                return BiomeType.NONE;
            boolean sky = false;
            final Vector3 temp1 = Vector3.getNewVector();
            final int x0 = segment.chunkX * 16, y0 = segment.chunkY * 16, z0 = segment.chunkZ * 16;
            final int dx = (v.intX() - x0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
            final int dy = (v.intY() - y0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
            final int dz = (v.intZ() - z0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
            final int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
            int industrial = 0;
            int water = 0;
            outer:
            for (int x = x1; x < x1 + TerrainSegment.GRIDSIZE; x++)
                for (int y = y1; y < y1 + TerrainSegment.GRIDSIZE; y++)
                    for (int z = z1; z < z1 + TerrainSegment.GRIDSIZE; z++)
            {
                temp1.set(x, y, z);
                if (segment.isInTerrainSegment(temp1.x, temp1.y, temp1.z))
                {
                    final double y2 = temp1.getMaxY(world);
                    sky = y2 <= temp1.y;
                }
                BlockState state;
                if (PokecubeTerrainChecker.isIndustrial(state = temp1.getBlockState(world))) industrial++;
                if (industrial > 2) return BiomeType.INDUSTRIAL;
                if (state.getMaterial() == Material.WATER) water++;
                if (sky) break outer;
            }
            if (sky) return BiomeType.NONE;
            if (water > 4) return BiomeType.CAVE_WATER;
            else if (this.isCave(v, world)) return BiomeType.CAVE;
            return PokecubeTerrainChecker.INSIDE;
        }
        BiomeType biome = BiomeType.NONE;
        final Biome b = v.getBiome(world);
        if (!PokecubeCore.getConfig().autoDetectSubbiomes) return biome;
        final boolean notLake = this.isWatery(b);
        int industrial = 0;
        int water = 0;
        int flower = 0;
        final Vector3 temp1 = Vector3.getNewVector();
        final int x0 = segment.chunkX * 16, y0 = segment.chunkY * 16, z0 = segment.chunkZ * 16;
        final int dx = (v.intX() - x0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
        final int dy = (v.intY() - y0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
        final int dz = (v.intZ() - z0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
        final int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
        for (int i = x1; i < x1 + TerrainSegment.GRIDSIZE; i++) for (int j = y1; j < y1 + TerrainSegment.GRIDSIZE; j++)
            for (int k = z1; k < z1 + TerrainSegment.GRIDSIZE; k++)
        {
            BlockState state;
            if (PokecubeTerrainChecker.isIndustrial(state = temp1.set(i, j, k).getBlockState(world))) industrial++;
            if (PokecubeTerrainChecker.isFlower(state)) flower++;
            if (industrial > 2) return BiomeType.INDUSTRIAL;
            if (flower > 3) return BiomeType.FLOWER;
            if (state.getMaterial() == Material.WATER) water++;
        }
        if (water > 4)
        {
            if (!notLake) biome = BiomeType.LAKE;
            return biome;
        }
        boolean sky = v.canSeeSky(world);
        if (sky)
        {
            sky = v.findNextSolidBlock(world, Vector3.secondAxisNeg, 16) == null;
            if (sky) return BiomeType.SKY;
        }
        // Check nearby villages, and if in one, define as village type.
        if (world instanceof ServerLevel)
        {
            final BlockPos pos = v.getPos();
            final ServerLevel server = (ServerLevel) world;
            if (server.isVillage(pos)) biome = BiomeType.VILLAGE;
        }
        return biome;
    }

    public boolean isCave(final Vector3 v, final LevelAccessor world)
    {
        return this.isCaveFloor(v, world) && this.isCaveCeiling(v, world);
    }

    public boolean isCaveCeiling(final Vector3 v, final LevelAccessor world)
    {
        final double y = v.getMaxY(world);
        if (y <= v.y) return false;
        BlockState state = v.getBlockState(world);
        if (state.getMaterial().isSolid()) return PokecubeTerrainChecker.isCave(state);
        final Vector3 up = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxis, y - v.y);
        if (up == null) return false;
        state = up.getBlockState(world);
        return PokecubeTerrainChecker.isCave(state);
    }

    public boolean isCaveFloor(final Vector3 v, final LevelAccessor world)
    {
        final BlockState state = v.getBlockState(world);
        if (state.getMaterial().isSolid()) return PokecubeTerrainChecker.isCave(state);
        final Vector3 down = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg,
                v.y - world.getMinBuildHeight());
        if (down == null) return false;
        return PokecubeTerrainChecker.isCave(down.getBlockState(world));
    }
}
