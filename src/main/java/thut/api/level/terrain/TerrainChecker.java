package thut.api.level.terrain;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import thut.api.data.StringTag.StringValue;
import thut.api.item.ItemList;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;
import thut.core.common.handlers.ConfigHandler;

public class TerrainChecker
{
    private static class StructInfo
    {
        public String struct;
        public String subbiome;
    }

    public static BiomeType INSIDE = BiomeType.getBiome("inside", true).setNoSave();

    public static ResourceLocation CAVE_TAG = new ResourceLocation(ThutCore.MODID, "cave");
    public static ResourceLocation FRUIT_TAG = new ResourceLocation(ThutCore.MODID, "fruit");
    public static ResourceLocation GROUND_TAG = new ResourceLocation(ThutCore.MODID, "ground");
    public static ResourceLocation INDUSTRIAL_TAG = new ResourceLocation(ThutCore.MODID, "industrial");
    public static ResourceLocation PLANTS_EDIBLE_TAG = new ResourceLocation(ThutCore.MODID, "plants_edible");
    public static ResourceLocation PLANTS_CUTABLE_TAG = new ResourceLocation(ThutCore.MODID, "plants_cutable");
    public static ResourceLocation ROCK_TAG = new ResourceLocation(ThutCore.MODID, "rocks");
    public static ResourceLocation SURFACE_TAG = new ResourceLocation(ThutCore.MODID, "surface");
    public static ResourceLocation TERRAIN_TAG = new ResourceLocation(ThutCore.MODID, "terrain");
    public static ResourceLocation WOOD_TAG = new ResourceLocation(ThutCore.MODID, "wood");

    public static ResourceLocation LEAVES = new ResourceLocation("minecraft:leaves");
    public static ResourceLocation FLOWERS = new ResourceLocation("minecraft:small_flowers");

    public static final String tagKey = "structure_subbiomes";

    public static void initStructMap()
    {
        for (final String s : ThutCore.getConfig().structure_subbiomes)
        {
            final StructInfo info = JsonUtil.gson.fromJson(s, StructInfo.class);
            String key = info.struct.replace("#", "");
            if (key.contains(":"))
            {
                String[] args = key.split(":");
                key = ThutCore.trim(args[0]) + ":" + ThutCore.trim(args[1]);
            }
            else
            {
                key = ThutCore.trim(key);
            }
            var value = new StringValue<String>(key).setValue(info.subbiome);
            ConfigHandler.STRUCTURE_SUBBIOMES.AddValue(tagKey, value);
        }
    }

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
