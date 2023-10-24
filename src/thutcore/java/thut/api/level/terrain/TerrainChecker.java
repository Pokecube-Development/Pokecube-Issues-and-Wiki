package thut.api.level.terrain;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
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

    public static ResourceLocation CAVETAG = new ResourceLocation(ThutCore.MODID, "cave");
    public static ResourceLocation FRUITTAG = new ResourceLocation(ThutCore.MODID, "fruit");
    public static ResourceLocation GROUNDTAG = new ResourceLocation(ThutCore.MODID, "ground");
    public static ResourceLocation INDUSTRIALTAG = new ResourceLocation(ThutCore.MODID, "industrial");
    public static ResourceLocation PLANTEATTAG = new ResourceLocation(ThutCore.MODID, "plants_edible");
    public static ResourceLocation PLANTCUTTAG = new ResourceLocation(ThutCore.MODID, "plants_cutable");
    public static ResourceLocation ROCKTAG = new ResourceLocation(ThutCore.MODID, "rocks");
    public static ResourceLocation SURFACETAG = new ResourceLocation(ThutCore.MODID, "surface");
    public static ResourceLocation TERRAINTAG = new ResourceLocation(ThutCore.MODID, "terrain");
    public static ResourceLocation WOODTAG = new ResourceLocation(ThutCore.MODID, "wood");

    public static ResourceLocation LEAVES = new ResourceLocation("minecraft:leaves");
    public static ResourceLocation FLOWERS = new ResourceLocation("minecraft:small_flowers");

    public static final String tagKey = "thutcore:structure_subbiomes";

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
        return ItemList.is(TerrainChecker.CAVETAG, state);
    }

    public static boolean isGround(final BlockState state)
    {
        return ItemList.is(TerrainChecker.GROUNDTAG, state);
    }

    public static boolean isFruit(final BlockState state)
    {
        return ItemList.is(TerrainChecker.FRUITTAG, state);
    }

    public static boolean isIndustrial(final BlockState state)
    {
        return ItemList.is(TerrainChecker.INDUSTRIALTAG, state);
    }

    private static boolean isPlant(final Material m)
    {
        return m == Material.PLANT || m == Material.REPLACEABLE_PLANT || m == Material.REPLACEABLE_WATER_PLANT
                || m == Material.WATER_PLANT;
    }

    public static boolean isEdiblePlant(final BlockState state)
    {
        return ItemList.is(TerrainChecker.PLANTEATTAG, state)
                || ThutCore.getConfig().autoPopulateLists && TerrainChecker.isPlant(state.getMaterial());
    }

    public static boolean isCutablePlant(final BlockState state)
    {
        return ItemList.is(TerrainChecker.PLANTEATTAG, state) || ItemList.is(BlockTags.LEAVES.location(), state)
                || ThutCore.getConfig().autoPopulateLists && TerrainChecker.isPlant(state.getMaterial());
    }

    public static boolean isRock(final BlockState state)
    {
        return ItemList.is(TerrainChecker.ROCKTAG, state);
    }

    public static boolean isSurface(final BlockState state)
    {
        return ItemList.is(TerrainChecker.SURFACETAG, state);
    }

    public static boolean isTerrain(final BlockState state)
    {
        return ItemList.is(TerrainChecker.TERRAINTAG, state);
    }

    public static boolean isWood(final BlockState state)
    {
        return ItemList.is(TerrainChecker.WOODTAG, state);
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
