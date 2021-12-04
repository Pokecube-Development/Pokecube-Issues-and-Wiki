package pokecube.core.world.terrain;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import pokecube.core.PokecubeCore;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class PokecubeTerrainChecker implements ISubBiomeChecker
{
    private static class StructInfo
    {
        public String struct;
        public String subbiome;
    }

    public static BiomeType INSIDE = BiomeType.getBiome("inside", true).setNoSave();

    public static ResourceLocation CAVETAG = new ResourceLocation(PokecubeCore.MODID, "cave");
    public static ResourceLocation FRUITTAG = new ResourceLocation(PokecubeCore.MODID, "fruit");
    public static ResourceLocation GROUNDTAG = new ResourceLocation(PokecubeCore.MODID, "ground");
    public static ResourceLocation INDUSTRIALTAG = new ResourceLocation(PokecubeCore.MODID, "industrial");
    public static ResourceLocation PLANTEATTAG = new ResourceLocation(PokecubeCore.MODID, "plants_edible");
    public static ResourceLocation PLANTCUTTAG = new ResourceLocation(PokecubeCore.MODID, "plants_cutable");
    public static ResourceLocation ROCKTAG = new ResourceLocation(PokecubeCore.MODID, "rocks");
    public static ResourceLocation SURFACETAG = new ResourceLocation(PokecubeCore.MODID, "surface");
    public static ResourceLocation TERRAINTAG = new ResourceLocation(PokecubeCore.MODID, "terrain");
    public static ResourceLocation WOODTAG = new ResourceLocation(PokecubeCore.MODID, "wood");

    public static ResourceLocation LEAVES = new ResourceLocation("minecraft:leaves");
    public static ResourceLocation FLOWERS = new ResourceLocation("minecraft:small_flowers");

    public static Map<String, String> structureSubbiomeMap = Maps.newHashMap();
    public static Map<String, String> manualStructureSubbiomes = Maps.newHashMap();

    public static void initStructMap()
    {
        PokecubeTerrainChecker.structureSubbiomeMap.clear();
        for (final String s : PokecubeCore.getConfig().structure_subbiomes)
        {
            final StructInfo info = PokedexEntryLoader.gson.fromJson(s, StructInfo.class);
            PokecubeTerrainChecker.structureSubbiomeMap.put(info.struct, info.subbiome);
        }
        PokecubeTerrainChecker.structureSubbiomeMap.putAll(PokecubeTerrainChecker.manualStructureSubbiomes);
    }

    public static void init()
    {
        final PokecubeTerrainChecker checker = new PokecubeTerrainChecker();
        TerrainSegment.defaultChecker = checker;
    }

    public static boolean isCave(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.CAVETAG, state);
    }

    public static boolean isGround(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.GROUNDTAG, state);
    }

    public static boolean isFruit(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.FRUITTAG, state);
    }

    public static boolean isIndustrial(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.INDUSTRIALTAG, state);
    }

    private static boolean isPlant(final Material m)
    {
        return m == Material.PLANT || m == Material.REPLACEABLE_PLANT || m == Material.REPLACEABLE_WATER_PLANT
                || m == Material.WATER_PLANT;
    }

    public static boolean isEdiblePlant(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.PLANTEATTAG, state)
                || PokecubeCore.getConfig().autoPopulateLists && PokecubeTerrainChecker.isPlant(state.getMaterial());
    }

    public static boolean isCutablePlant(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.PLANTEATTAG, state) || ItemList.is(BlockTags.LEAVES.getName(), state)
                || PokecubeCore.getConfig().autoPopulateLists && PokecubeTerrainChecker.isPlant(state.getMaterial());
    }

    public static boolean isRock(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.ROCKTAG, state);
    }

    public static boolean isSurface(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.SURFACETAG, state);
    }

    public static boolean isTerrain(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.TERRAINTAG, state);
    }

    public static boolean isWood(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.WOODTAG, state);
    }

    public static boolean isLeaves(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.LEAVES, state);
    }

    public static boolean isFlower(final BlockState state)
    {
        return ItemList.is(PokecubeTerrainChecker.FLOWERS, state);
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
