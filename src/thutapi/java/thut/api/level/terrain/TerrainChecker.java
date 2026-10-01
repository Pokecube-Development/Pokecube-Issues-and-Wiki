package thut.api.level.terrain;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import thut.api.ThutAPI;
import thut.api.item.ItemList;

public class TerrainChecker
{
    public static class StructInfo
    {
        public String struct;
        public String subbiome;
    }

    public static BiomeType INSIDE = BiomeType.getBiome("inside", true).setNoSave();

    public static ResourceLocation CAVE_TAG = ResourceLocation.fromNamespaceAndPath(ThutAPI.MODID, "cave");
    public static ResourceLocation FRUIT_TAG = ResourceLocation.fromNamespaceAndPath(ThutAPI.MODID, "fruit");
    public static ResourceLocation GROUND_TAG = ResourceLocation.fromNamespaceAndPath(ThutAPI.MODID, "ground");
    public static ResourceLocation INDUSTRIAL_TAG = ResourceLocation.fromNamespaceAndPath(ThutAPI.MODID, "industrial");
    public static ResourceLocation PLANTS_EDIBLE_TAG = ResourceLocation.fromNamespaceAndPath(ThutAPI.MODID, "plants_edible");
    public static ResourceLocation PLANTS_CUTABLE_TAG = ResourceLocation.fromNamespaceAndPath(ThutAPI.MODID, "plants_cutable");
    public static ResourceLocation ROCK_TAG = ResourceLocation.fromNamespaceAndPath(ThutAPI.MODID, "rocks");
    public static ResourceLocation SURFACE_TAG = ResourceLocation.fromNamespaceAndPath(ThutAPI.MODID, "surface");
    public static ResourceLocation TERRAIN_TAG = ResourceLocation.fromNamespaceAndPath(ThutAPI.MODID, "terrain");
    public static ResourceLocation WOOD_TAG = ResourceLocation.fromNamespaceAndPath(ThutAPI.MODID, "wood");

    public static ResourceLocation LEAVES = ResourceLocation.parse("minecraft:leaves");
    public static ResourceLocation FLOWERS = ResourceLocation.parse("minecraft:small_flowers");

    public static final String tagKey = "thutcore:structure_subbiomes";

    public static boolean isCave(final BlockState state)
    {
        return ItemList.is(TerrainChecker.CAVE_TAG, state);
    }

    public static boolean isGround(final BlockState state)
    {
        return ItemList.is(TerrainChecker.GROUND_TAG, state);
    }

    public static boolean isFruit(final BlockState state)
    {
        return ItemList.is(TerrainChecker.FRUIT_TAG, state);
    }

    public static boolean isIndustrial(final BlockState state)
    {
        return ItemList.is(TerrainChecker.INDUSTRIAL_TAG, state);
    }

    public static boolean isEdiblePlant(final BlockState state)
    {
        return ItemList.is(TerrainChecker.PLANTS_EDIBLE_TAG, state) || state.is(BlockTags.FLOWERS);
    }

    public static boolean isCutablePlant(final BlockState state)
    {
        return ItemList.is(TerrainChecker.PLANTS_CUTABLE_TAG, state) || ItemList.is(BlockTags.LEAVES.location(), state);
    }

    public static boolean isRock(final BlockState state)
    {
        return ItemList.is(TerrainChecker.ROCK_TAG, state);
    }

    public static boolean isSurface(final BlockState state)
    {
        return ItemList.is(TerrainChecker.SURFACE_TAG, state);
    }

    public static boolean isTerrain(final BlockState state)
    {
        return ItemList.is(TerrainChecker.TERRAIN_TAG, state);
    }

    public static boolean isWood(final BlockState state)
    {
        return ItemList.is(TerrainChecker.WOOD_TAG, state);
    }

    public static boolean isLeaves(final BlockState state)
    {
        return ItemList.is(TerrainChecker.LEAVES, state);
    }

    public static boolean isFlower(final BlockState state)
    {
        return ItemList.is(TerrainChecker.FLOWERS, state);
    }
}
