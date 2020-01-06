package pokecube.core.world.terrain;

import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.BiomeDictionary.Type;
import pokecube.core.PokecubeCore;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class PokecubeTerrainChecker implements ISubBiomeChecker
{
    public static BiomeType INSIDE = BiomeType.getBiome("inside", true);

    public static Map<String, String> structureSubbiomeMap = Maps.newHashMap();

    public static void init()
    {
        final PokecubeTerrainChecker checker = new PokecubeTerrainChecker();
        TerrainSegment.defaultChecker = checker;
    }

    public static boolean isCave(BlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (final Predicate<BlockState> predicate : PokecubeCore.getConfig().getCaveBlocks())
            if (predicate.apply(state)) return true;
        return false;
    }

    public static boolean isDirt(BlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (final Predicate<BlockState> predicate : PokecubeCore.getConfig().getDirtTypes())
            if (predicate.apply(state)) return true;
        return false;
    }

    public static boolean isFruit(BlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (final Predicate<BlockState> predicate : PokecubeCore.getConfig().getFruitTypes())
            if (predicate.apply(state)) return true;
        return false;
    }

    public static boolean isIndustrial(BlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (final Predicate<BlockState> predicate : PokecubeCore.getConfig().getIndustrial())
            if (predicate.apply(state)) return true;
        return false;
    }

    public static boolean isPlant(BlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (final Predicate<BlockState> predicate : PokecubeCore.getConfig().getPlantTypes())
            if (predicate.apply(state)) return true;
        return false;
    }

    public static boolean isRock(BlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (final Predicate<BlockState> predicate : PokecubeCore.getConfig().getRocks())
            if (predicate.apply(state)) return true;
        return false;
    }

    public static boolean isSurface(BlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (final Predicate<BlockState> predicate : PokecubeCore.getConfig().getSurfaceBlocks())
            if (predicate.apply(state)) return true;
        return false;
    }

    public static boolean isTerrain(BlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (final Predicate<BlockState> predicate : PokecubeCore.getConfig().getTerrain())
            if (predicate.apply(state)) return true;
        return false;
    }

    public static boolean isWood(BlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (final Predicate<BlockState> predicate : PokecubeCore.getConfig().getWoodTypes())
            if (predicate.apply(state)) return true;
        return false;
    }

    @Override
    public int getSubBiome(World world, Vector3 v, TerrainSegment segment, Chunk chunk, boolean caveAdjusted)
    {
        if (caveAdjusted)
        {
            if (world instanceof ServerWorld)
            {
                final ServerWorld worldS = (ServerWorld) world;
                final ChunkGenerator<?> generator = worldS.getChunkProvider().generator;

                if (generator != null) for (final String key : PokecubeTerrainChecker.structureSubbiomeMap.keySet())
                {
                    final boolean in = worldS.getChunkProvider().getChunkGenerator().findNearestStructure(worldS, key, v
                            .getPos(), 0, true) != null;
                    if (in)
                    {
                        final String mapping = PokecubeTerrainChecker.structureSubbiomeMap.get(key);
                        final BiomeType biome = BiomeType.getBiome(mapping, true);
                        return biome.getType();
                    }
                }
            }
            if (world.dimension.doesWaterVaporize() || v.canSeeSky(world) || !PokecubeCore
                    .getConfig().autoDetectSubbiomes) return -1;
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
            for (int i = x1; i < x1 + TerrainSegment.GRIDSIZE; i++)
                for (int j = y1; j < y1 + TerrainSegment.GRIDSIZE; j++)
                    for (int k = z1; k < z1 + TerrainSegment.GRIDSIZE; k++)
                    {
                        temp1.set(i, j, k);
                        if (segment.isInTerrainSegment(temp1.x, temp1.y, temp1.z))
                        {
                            final double y = temp1.getMaxY(world);
                            sky = y <= temp1.y;
                        }
                        BlockState state;
                        if (PokecubeTerrainChecker.isIndustrial(state = temp1.getBlockState(world))) industrial++;
                        if (industrial > 2) return BiomeType.INDUSTRIAL.getType();
                        if (state.getMaterial() == Material.WATER) water++;
                        if (sky) break outer;
                    }
            if (sky) return -1;
            if (water > 4) return BiomeType.CAVE_WATER.getType();
            else if (this.isCave(v, world)) return BiomeType.CAVE.getType();

            return PokecubeTerrainChecker.INSIDE.getType();
        }
        int biome = -1;
        final Biome b = v.getBiome(world);
        if (!PokecubeCore.getConfig().autoDetectSubbiomes) return biome;
        final boolean notLake = BiomeDatabase.contains(b, Type.OCEAN) || BiomeDatabase.contains(b, Type.SWAMP)
                || BiomeDatabase.contains(b, Type.RIVER) || BiomeDatabase.contains(b, Type.WATER) || BiomeDatabase
                        .contains(b, Type.BEACH);
        int industrial = 0;
        int water = 0;
        final Vector3 temp1 = Vector3.getNewVector();
        final int x0 = segment.chunkX * 16, y0 = segment.chunkY * 16, z0 = segment.chunkZ * 16;
        final int dx = (v.intX() - x0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
        final int dy = (v.intY() - y0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
        final int dz = (v.intZ() - z0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
        final int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
        for (int i = x1; i < x1 + TerrainSegment.GRIDSIZE; i++)
            for (int j = y1; j < y1 + TerrainSegment.GRIDSIZE; j++)
                for (int k = z1; k < z1 + TerrainSegment.GRIDSIZE; k++)
                {
                    BlockState state;
                    if (PokecubeTerrainChecker.isIndustrial(state = temp1.set(i, j, k).getBlockState(world)))
                        industrial++;
                    if (industrial > 2) return BiomeType.INDUSTRIAL.getType();
                    if (state.getMaterial() == Material.WATER) water++;
                }
        if (water > 4)
        {
            if (!notLake) biome = BiomeType.LAKE.getType();
            return biome;
        }
        boolean sky = v.canSeeSky(world);
        if (sky)
        {
            sky = v.findNextSolidBlock(world, Vector3.secondAxisNeg, 16) == null;
            if (sky) return BiomeType.SKY.getType();
        }
        // Check nearby villages, and if in one, define as village type.
        if (world instanceof ServerWorld)
        {
            final BlockPos pos = v.getPos();
            final ServerWorld server = (ServerWorld) world;
            if (server.func_217483_b_(pos)) biome = BiomeType.VILLAGE.getType();
        }
        return biome;
    }

    public boolean isCave(Vector3 v, World world)
    {
        return this.isCaveFloor(v, world) && this.isCaveCeiling(v, world);
    }

    public boolean isCaveCeiling(Vector3 v, World world)
    {
        final double y = v.getMaxY(world);
        if (y <= v.y) return false;
        BlockState state = v.getBlockState(world);
        if (state.getMaterial().isSolid()) return PokecubeTerrainChecker.isCave(state);
        final Vector3 up = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxis, 255 - v.y);
        if (up == null) return false;
        state = up.getBlockState(world);
        return PokecubeTerrainChecker.isCave(state);
    }

    public boolean isCaveFloor(Vector3 v, World world)
    {
        final BlockState state = v.getBlockState(world);
        if (state.getMaterial().isSolid()) return PokecubeTerrainChecker.isCave(state);
        final Vector3 down = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg, v.y);
        if (down == null) return false;
        return PokecubeTerrainChecker.isCave(down.getBlockState(world));
    }
}
