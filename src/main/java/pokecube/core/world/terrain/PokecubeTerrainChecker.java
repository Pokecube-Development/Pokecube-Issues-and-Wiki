package pokecube.core.world.terrain;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.BiomeDictionary.Type;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class PokecubeTerrainChecker implements ISubBiomeChecker
{
    public static BiomeType INSIDE = BiomeType.getBiome("inside", true);

    public static ResourceLocation CAVETAG       = new ResourceLocation(PokecubeCore.MODID, "cave");
    public static ResourceLocation FRUITTAG      = new ResourceLocation(PokecubeCore.MODID, "fruit");
    public static ResourceLocation GROUNDTAG     = new ResourceLocation(PokecubeCore.MODID, "ground");
    public static ResourceLocation INDUSTRIALTAG = new ResourceLocation(PokecubeCore.MODID, "industrial");
    public static ResourceLocation PLANTEATTAG   = new ResourceLocation(PokecubeCore.MODID, "plants_edible");
    public static ResourceLocation PLANTCUTTAG   = new ResourceLocation(PokecubeCore.MODID, "plants_cutable");
    public static ResourceLocation ROCKTAG       = new ResourceLocation(PokecubeCore.MODID, "rocks");
    public static ResourceLocation SURFACETAG    = new ResourceLocation(PokecubeCore.MODID, "surface");
    public static ResourceLocation TERRAINTAG    = new ResourceLocation(PokecubeCore.MODID, "terrain");
    public static ResourceLocation WOODTAG       = new ResourceLocation(PokecubeCore.MODID, "wood");

    public static Map<String, String> structureSubbiomeMap = Maps.newHashMap();

    public static void init()
    {
        final PokecubeTerrainChecker checker = new PokecubeTerrainChecker();
        TerrainSegment.defaultChecker = checker;
    }

    public static boolean isCave(final BlockState state)
    {
        return PokecubeItems.is(PokecubeTerrainChecker.CAVETAG, state);
    }

    public static boolean isGround(final BlockState state)
    {
        return PokecubeItems.is(PokecubeTerrainChecker.GROUNDTAG, state);
    }

    public static boolean isFruit(final BlockState state)
    {
        return PokecubeItems.is(PokecubeTerrainChecker.FRUITTAG, state);
    }

    public static boolean isIndustrial(final BlockState state)
    {
        return PokecubeItems.is(PokecubeTerrainChecker.INDUSTRIALTAG, state);
    }

    private static boolean isPlant(final Material m)
    {
        return m == Material.PLANTS || m == Material.TALL_PLANTS || m == Material.SEA_GRASS
                || m == Material.OCEAN_PLANT;
    }

    public static boolean isEdiblePlant(final BlockState state)
    {
        return PokecubeItems.is(PokecubeTerrainChecker.PLANTEATTAG, state) || PokecubeCore.getConfig().autoPopulateLists
                && PokecubeTerrainChecker.isPlant(state.getMaterial());
    }

    public static boolean isCutablePlant(final BlockState state)
    {
        return PokecubeItems.is(PokecubeTerrainChecker.PLANTEATTAG, state) || PokecubeCore.getConfig().autoPopulateLists
                && PokecubeTerrainChecker.isPlant(state.getMaterial());
    }

    public static boolean isRock(final BlockState state)
    {
        return PokecubeItems.is(PokecubeTerrainChecker.ROCKTAG, state);
    }

    public static boolean isSurface(final BlockState state)
    {
        return PokecubeItems.is(PokecubeTerrainChecker.SURFACETAG, state);
    }

    public static boolean isTerrain(final BlockState state)
    {
        return PokecubeItems.is(PokecubeTerrainChecker.TERRAINTAG, state);
    }

    public static boolean isWood(final BlockState state)
    {
        return PokecubeItems.is(PokecubeTerrainChecker.WOODTAG, state);
    }

    @Override
    public int getSubBiome(final IWorld world, final Vector3 v, final TerrainSegment segment,
            final boolean caveAdjusted)
    {
        if (caveAdjusted)
        {
            if (world instanceof ServerWorld)
            {
                final ServerWorld worldS = (ServerWorld) world;
                final ChunkGenerator<?> generator = worldS.getChunkProvider().generator;

                if (generator != null) for (final String key : PokecubeTerrainChecker.structureSubbiomeMap.keySet())
                {
                    final BlockPos near = worldS.getChunkProvider().getChunkGenerator().findNearestStructure(worldS,
                            key, v.getPos(), 0, true);
                    final boolean in = near != null && v.distanceTo(Vector3.getNewVector().set(near)) < 16;
                    if (in)
                    {
                        final String mapping = PokecubeTerrainChecker.structureSubbiomeMap.get(key);
                        final BiomeType biome = BiomeType.getBiome(mapping, true);
                        return biome.getType();
                    }
                }
            }
            if (world.getDimension().doesWaterVaporize() || v.canSeeSky(world) || !PokecubeCore
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

    public boolean isCave(final Vector3 v, final IWorld world)
    {
        return this.isCaveFloor(v, world) && this.isCaveCeiling(v, world);
    }

    public boolean isCaveCeiling(final Vector3 v, final IWorld world)
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

    public boolean isCaveFloor(final Vector3 v, final IWorld world)
    {
        final BlockState state = v.getBlockState(world);
        if (state.getMaterial().isSolid()) return PokecubeTerrainChecker.isCave(state);
        final Vector3 down = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg, v.y);
        if (down == null) return false;
        return PokecubeTerrainChecker.isCave(down.getBlockState(world));
    }
}
