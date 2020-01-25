package pokecube.legends.worldgen.genlayer;

import java.lang.reflect.Type;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.init.Biomes;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.biome.Biome;

public class ChunkGenSettings 
{
	public final float coordinateScale;
    public final float heightScale;
    public final float upperLimitScale;
    public final float lowerLimitScale;
    public final float depthNoiseScaleX;
    public final float depthNoiseScaleZ;
    public final float depthNoiseScaleExponent;
    public final float mainNoiseScaleX;
    public final float mainNoiseScaleY;
    public final float mainNoiseScaleZ;
    public final float baseSize;
    public final float stretchY;
    public final float biomeDepthWeight;
    public final float biomeDepthOffSet;
    public final float biomeScaleWeight;
    public final float biomeScaleOffset;
    public final int seaLevel;
    public final boolean useCaves;
    public final boolean useDungeons;
    public final int dungeonChance;
    public final boolean useStrongholds;
    public final boolean useVillages;
    public final boolean useMineShafts;
    public final boolean useTemples;
    public final boolean useMonuments;
    public final boolean useMansions;
    public final boolean useRavines;
    public final boolean useWaterLakes;
    public final int waterLakeChance;
    public final boolean useLavaLakes;
    public final int lavaLakeChance;
    public final boolean useLavaOceans;
    public final int fixedBiome;
    public final int biomeSize;
    public final int riverSize;
    public final int dirtSize;
    public final int dirtCount;
    public final int dirtMinHeight;
    public final int dirtMaxHeight;
    public final int gravelSize;
    public final int gravelCount;
    public final int gravelMinHeight;
    public final int gravelMaxHeight;
    public final int graniteSize;
    public final int graniteCount;
    public final int graniteMinHeight;
    public final int graniteMaxHeight;
    public final int dioriteSize;
    public final int dioriteCount;
    public final int dioriteMinHeight;
    public final int dioriteMaxHeight;
    public final int andesiteSize;
    public final int andesiteCount;
    public final int andesiteMinHeight;
    public final int andesiteMaxHeight;
    public final int coalSize;
    public final int coalCount;
    public final int coalMinHeight;
    public final int coalMaxHeight;
    public final int ironSize;
    public final int ironCount;
    public final int ironMinHeight;
    public final int ironMaxHeight;
    public final int goldSize;
    public final int goldCount;
    public final int goldMinHeight;
    public final int goldMaxHeight;
    public final int redstoneSize;
    public final int redstoneCount;
    public final int redstoneMinHeight;
    public final int redstoneMaxHeight;
    public final int diamondSize;
    public final int diamondCount;
    public final int diamondMinHeight;
    public final int diamondMaxHeight;
    public final int lapisSize;
    public final int lapisCount;
    public final int lapisCenterHeight;
    public final int lapisSpread;

    private ChunkGenSettings(ChunkGenSettings.Factory settingsFactory)
    {
        this.coordinateScale = settingsFactory.coordinateScale;
        this.heightScale = settingsFactory.heightScale;
        this.upperLimitScale = settingsFactory.upperLimitScale;
        this.lowerLimitScale = settingsFactory.lowerLimitScale;
        this.depthNoiseScaleX = settingsFactory.depthNoiseScaleX;
        this.depthNoiseScaleZ = settingsFactory.depthNoiseScaleZ;
        this.depthNoiseScaleExponent = settingsFactory.depthNoiseScaleExponent;
        this.mainNoiseScaleX = settingsFactory.mainNoiseScaleX;
        this.mainNoiseScaleY = settingsFactory.mainNoiseScaleY;
        this.mainNoiseScaleZ = settingsFactory.mainNoiseScaleZ;
        this.baseSize = settingsFactory.baseSize;
        this.stretchY = settingsFactory.stretchY;
        this.biomeDepthWeight = settingsFactory.biomeDepthWeight;
        this.biomeDepthOffSet = settingsFactory.biomeDepthOffset;
        this.biomeScaleWeight = settingsFactory.biomeScaleWeight;
        this.biomeScaleOffset = settingsFactory.biomeScaleOffset;
        this.seaLevel = settingsFactory.seaLevel;
        this.useCaves = settingsFactory.useCaves;
        this.useDungeons = settingsFactory.useDungeons;
        this.dungeonChance = settingsFactory.dungeonChance;
        this.useStrongholds = settingsFactory.useStrongholds;
        this.useVillages = settingsFactory.useVillages;
        this.useMineShafts = settingsFactory.useMineShafts;
        this.useTemples = settingsFactory.useTemples;
        this.useMonuments = settingsFactory.useMonuments;
        this.useMansions = settingsFactory.useMansions;
        this.useRavines = settingsFactory.useRavines;
        this.useWaterLakes = settingsFactory.useWaterLakes;
        this.waterLakeChance = settingsFactory.waterLakeChance;
        this.useLavaLakes = settingsFactory.useLavaLakes;
        this.lavaLakeChance = settingsFactory.lavaLakeChance;
        this.useLavaOceans = settingsFactory.useLavaOceans;
        this.fixedBiome = settingsFactory.fixedBiome;
        this.biomeSize = settingsFactory.biomeSize;
        this.riverSize = settingsFactory.riverSize;
        this.dirtSize = settingsFactory.dirtSize;
        this.dirtCount = settingsFactory.dirtCount;
        this.dirtMinHeight = settingsFactory.dirtMinHeight;
        this.dirtMaxHeight = settingsFactory.dirtMaxHeight;
        this.gravelSize = settingsFactory.gravelSize;
        this.gravelCount = settingsFactory.gravelCount;
        this.gravelMinHeight = settingsFactory.gravelMinHeight;
        this.gravelMaxHeight = settingsFactory.gravelMaxHeight;
        this.graniteSize = settingsFactory.graniteSize;
        this.graniteCount = settingsFactory.graniteCount;
        this.graniteMinHeight = settingsFactory.graniteMinHeight;
        this.graniteMaxHeight = settingsFactory.graniteMaxHeight;
        this.dioriteSize = settingsFactory.dioriteSize;
        this.dioriteCount = settingsFactory.dioriteCount;
        this.dioriteMinHeight = settingsFactory.dioriteMinHeight;
        this.dioriteMaxHeight = settingsFactory.dioriteMaxHeight;
        this.andesiteSize = settingsFactory.andesiteSize;
        this.andesiteCount = settingsFactory.andesiteCount;
        this.andesiteMinHeight = settingsFactory.andesiteMinHeight;
        this.andesiteMaxHeight = settingsFactory.andesiteMaxHeight;
        this.coalSize = settingsFactory.coalSize;
        this.coalCount = settingsFactory.coalCount;
        this.coalMinHeight = settingsFactory.coalMinHeight;
        this.coalMaxHeight = settingsFactory.coalMaxHeight;
        this.ironSize = settingsFactory.ironSize;
        this.ironCount = settingsFactory.ironCount;
        this.ironMinHeight = settingsFactory.ironMinHeight;
        this.ironMaxHeight = settingsFactory.ironMaxHeight;
        this.goldSize = settingsFactory.goldSize;
        this.goldCount = settingsFactory.goldCount;
        this.goldMinHeight = settingsFactory.goldMinHeight;
        this.goldMaxHeight = settingsFactory.goldMaxHeight;
        this.redstoneSize = settingsFactory.redstoneSize;
        this.redstoneCount = settingsFactory.redstoneCount;
        this.redstoneMinHeight = settingsFactory.redstoneMinHeight;
        this.redstoneMaxHeight = settingsFactory.redstoneMaxHeight;
        this.diamondSize = settingsFactory.diamondSize;
        this.diamondCount = settingsFactory.diamondCount;
        this.diamondMinHeight = settingsFactory.diamondMinHeight;
        this.diamondMaxHeight = settingsFactory.diamondMaxHeight;
        this.lapisSize = settingsFactory.lapisSize;
        this.lapisCount = settingsFactory.lapisCount;
        this.lapisCenterHeight = settingsFactory.lapisCenterHeight;
        this.lapisSpread = settingsFactory.lapisSpread;
    }

    public static class Factory
        {
            @VisibleForTesting
            static final Gson JSON_ADAPTER = (new GsonBuilder()).registerTypeAdapter(ChunkGenSettings.Factory.class, new ChunkGenSettings.Serializer()).create();
            public float coordinateScale = 3000.0F; //684.412
            public float heightScale = 6000.0F;
            public float upperLimitScale = 512.0F;
            public float lowerLimitScale = 512.0F;
            public float depthNoiseScaleX = 200.0F;
            public float depthNoiseScaleZ = 200.0F;
            public float depthNoiseScaleExponent = 0.5F;
            public float mainNoiseScaleX = 80.0F;
            public float mainNoiseScaleY = 160.0F;
            public float mainNoiseScaleZ = 80.0F;
            public float baseSize = 8.5F;
            public float stretchY = 10.0F;
            public float biomeDepthWeight = 1.0F;
            public float biomeDepthOffset;
            public float biomeScaleWeight = 1.0F;
            public float biomeScaleOffset;
            public int seaLevel = 30;
            public boolean useCaves = true;
            public boolean useDungeons = true;
            public int dungeonChance = 20;
            public boolean useStrongholds = true;
            public boolean useVillages = true;
            public boolean useMineShafts = true;
            public boolean useTemples = true;
            public boolean useMonuments = true;
            public boolean useMansions = true;
            public boolean useRavines = true;
            public boolean useWaterLakes = true;
            public int waterLakeChance = 4;
            public boolean useLavaLakes = true;
            public int lavaLakeChance = 40;
            public boolean useLavaOceans;
            public int fixedBiome = -3;
            public int biomeSize = 4;
            public int riverSize = 4;
            public int dirtSize = 33;
            public int dirtCount = 10;
            public int dirtMinHeight;
            public int dirtMaxHeight = 256;
            public int gravelSize = 33;
            public int gravelCount = 8;
            public int gravelMinHeight;
            public int gravelMaxHeight = 256;
            public int graniteSize = 33;
            public int graniteCount = 10;
            public int graniteMinHeight;
            public int graniteMaxHeight = 80;
            public int dioriteSize = 33;
            public int dioriteCount = 10;
            public int dioriteMinHeight;
            public int dioriteMaxHeight = 80;
            public int andesiteSize = 33;
            public int andesiteCount = 10;
            public int andesiteMinHeight;
            public int andesiteMaxHeight = 80;
            public int coalSize = 17;
            public int coalCount = 20;
            public int coalMinHeight;
            public int coalMaxHeight = 128;
            public int ironSize = 9;
            public int ironCount = 20;
            public int ironMinHeight;
            public int ironMaxHeight = 64;
            public int goldSize = 9;
            public int goldCount = 2;
            public int goldMinHeight;
            public int goldMaxHeight = 32;
            public int redstoneSize = 8;
            public int redstoneCount = 8;
            public int redstoneMinHeight;
            public int redstoneMaxHeight = 16;
            public int diamondSize = 8;
            public int diamondCount = 1;
            public int diamondMinHeight;
            public int diamondMaxHeight = 16;
            public int lapisSize = 7;
            public int lapisCount = 1;
            public int lapisCenterHeight = 16;
            public int lapisSpread = 16;

            public static ChunkGenSettings.Factory jsonToFactory(String p_177865_0_)
            {
                if (p_177865_0_.isEmpty())
                {
                    return new ChunkGenSettings.Factory();
                }
                else
                {
                    try
                    {
                        return (ChunkGenSettings.Factory)JsonUtils.gsonDeserialize(JSON_ADAPTER, p_177865_0_, ChunkGenSettings.Factory.class);
                    }
                    catch (Exception var2)
                    {
                        return new ChunkGenSettings.Factory();
                    }
                }
            }

            public String toString()
            {
                return JSON_ADAPTER.toJson(this);
            }

            public Factory()
            {
                this.setDefaults();
            }

            public void setDefaults()
            {
                this.coordinateScale = 684.412F; //684.412
                this.heightScale = 847.0F; //--
                this.upperLimitScale = 412.0F; //512
                this.lowerLimitScale = 412.0F; //--
                this.depthNoiseScaleX = 200.0F; //200
                this.depthNoiseScaleZ = 200.0F; //--
                this.depthNoiseScaleExponent = 0.5F; //0.5
                this.mainNoiseScaleX = 80.0F;
                this.mainNoiseScaleY = 160.0F;
                this.mainNoiseScaleZ = 80.0F;
                this.baseSize = 8.5F; //8.5
                this.stretchY = 12.0F; //12.0
                this.biomeDepthWeight = 1.3F; // 1.0
                this.biomeDepthOffset = 0.0F;
                this.biomeScaleWeight = 1F;
                this.biomeScaleOffset = 0.0F;
                this.seaLevel = 60; //63
                this.useCaves = true;
                this.useDungeons = true;
                this.dungeonChance = 16;
                this.useStrongholds = true;
                this.useVillages = true;
                this.useMineShafts = true;
                this.useTemples = true;
                this.useMonuments = true;
                this.useMansions = true;
                this.useRavines = true;
                this.useWaterLakes = true;
                this.waterLakeChance = 4;
                this.useLavaLakes = true;
                this.lavaLakeChance = 40;
                this.useLavaOceans = false;
                this.fixedBiome = -2; //-1
                this.biomeSize = 3; //3
                this.riverSize = 4;
                this.dirtSize = 33;
                this.dirtCount = 10;
                this.dirtMinHeight = 0;
                this.dirtMaxHeight = 256;
                this.gravelSize = 33;
                this.gravelCount = 8;
                this.gravelMinHeight = 0;
                this.gravelMaxHeight = 256;
                this.graniteSize = 33;
                this.graniteCount = 10;
                this.graniteMinHeight = 0;
                this.graniteMaxHeight = 80;
                this.dioriteSize = 33;
                this.dioriteCount = 10;
                this.dioriteMinHeight = 0;
                this.dioriteMaxHeight = 80;
                this.andesiteSize = 33;
                this.andesiteCount = 10;
                this.andesiteMinHeight = 0;
                this.andesiteMaxHeight = 80;
                this.coalSize = 17;
                this.coalCount = 20;
                this.coalMinHeight = 0;
                this.coalMaxHeight = 128;
                this.ironSize = 9;
                this.ironCount = 20;
                this.ironMinHeight = 0;
                this.ironMaxHeight = 64;
                this.goldSize = 9;
                this.goldCount = 2;
                this.goldMinHeight = 0;
                this.goldMaxHeight = 32;
                this.redstoneSize = 8;
                this.redstoneCount = 8;
                this.redstoneMinHeight = 0;
                this.redstoneMaxHeight = 16;
                this.diamondSize = 8;
                this.diamondCount = 1;
                this.diamondMinHeight = 0;
                this.diamondMaxHeight = 16;
                this.lapisSize = 7;
                this.lapisCount = 1;
                this.lapisCenterHeight = 16;
                this.lapisSpread = 16;
            }

            public boolean equals(Object p_equals_1_)
            {
                if (this == p_equals_1_)
                {
                    return true;
                }
                else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass())
                {
                    ChunkGenSettings.Factory chunkGenSettings$factory = (ChunkGenSettings.Factory)p_equals_1_;

                    if (this.andesiteCount != chunkGenSettings$factory.andesiteCount)
                    {
                        return false;
                    }
                    else if (this.andesiteMaxHeight != chunkGenSettings$factory.andesiteMaxHeight)
                    {
                        return false;
                    }
                    else if (this.andesiteMinHeight != chunkGenSettings$factory.andesiteMinHeight)
                    {
                        return false;
                    }
                    else if (this.andesiteSize != chunkGenSettings$factory.andesiteSize)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.baseSize, this.baseSize) != 0)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.biomeDepthOffset, this.biomeDepthOffset) != 0)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.biomeDepthWeight, this.biomeDepthWeight) != 0)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.biomeScaleOffset, this.biomeScaleOffset) != 0)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.biomeScaleWeight, this.biomeScaleWeight) != 0)
                    {
                        return false;
                    }
                    else if (this.biomeSize != chunkGenSettings$factory.biomeSize)
                    {
                        return false;
                    }
                    else if (this.coalCount != chunkGenSettings$factory.coalCount)
                    {
                        return false;
                    }
                    else if (this.coalMaxHeight != chunkGenSettings$factory.coalMaxHeight)
                    {
                        return false;
                    }
                    else if (this.coalMinHeight != chunkGenSettings$factory.coalMinHeight)
                    {
                        return false;
                    }
                    else if (this.coalSize != chunkGenSettings$factory.coalSize)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.coordinateScale, this.coordinateScale) != 0)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.depthNoiseScaleExponent, this.depthNoiseScaleExponent) != 0)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.depthNoiseScaleX, this.depthNoiseScaleX) != 0)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.depthNoiseScaleZ, this.depthNoiseScaleZ) != 0)
                    {
                        return false;
                    }
                    else if (this.diamondCount != chunkGenSettings$factory.diamondCount)
                    {
                        return false;
                    }
                    else if (this.diamondMaxHeight != chunkGenSettings$factory.diamondMaxHeight)
                    {
                        return false;
                    }
                    else if (this.diamondMinHeight != chunkGenSettings$factory.diamondMinHeight)
                    {
                        return false;
                    }
                    else if (this.diamondSize != chunkGenSettings$factory.diamondSize)
                    {
                        return false;
                    }
                    else if (this.dioriteCount != chunkGenSettings$factory.dioriteCount)
                    {
                        return false;
                    }
                    else if (this.dioriteMaxHeight != chunkGenSettings$factory.dioriteMaxHeight)
                    {
                        return false;
                    }
                    else if (this.dioriteMinHeight != chunkGenSettings$factory.dioriteMinHeight)
                    {
                        return false;
                    }
                    else if (this.dioriteSize != chunkGenSettings$factory.dioriteSize)
                    {
                        return false;
                    }
                    else if (this.dirtCount != chunkGenSettings$factory.dirtCount)
                    {
                        return false;
                    }
                    else if (this.dirtMaxHeight != chunkGenSettings$factory.dirtMaxHeight)
                    {
                        return false;
                    }
                    else if (this.dirtMinHeight != chunkGenSettings$factory.dirtMinHeight)
                    {
                        return false;
                    }
                    else if (this.dirtSize != chunkGenSettings$factory.dirtSize)
                    {
                        return false;
                    }
                    else if (this.dungeonChance != chunkGenSettings$factory.dungeonChance)
                    {
                        return false;
                    }
                    else if (this.fixedBiome != chunkGenSettings$factory.fixedBiome)
                    {
                        return false;
                    }
                    else if (this.goldCount != chunkGenSettings$factory.goldCount)
                    {
                        return false;
                    }
                    else if (this.goldMaxHeight != chunkGenSettings$factory.goldMaxHeight)
                    {
                        return false;
                    }
                    else if (this.goldMinHeight != chunkGenSettings$factory.goldMinHeight)
                    {
                        return false;
                    }
                    else if (this.goldSize != chunkGenSettings$factory.goldSize)
                    {
                        return false;
                    }
                    else if (this.graniteCount != chunkGenSettings$factory.graniteCount)
                    {
                        return false;
                    }
                    else if (this.graniteMaxHeight != chunkGenSettings$factory.graniteMaxHeight)
                    {
                        return false;
                    }
                    else if (this.graniteMinHeight != chunkGenSettings$factory.graniteMinHeight)
                    {
                        return false;
                    }
                    else if (this.graniteSize != chunkGenSettings$factory.graniteSize)
                    {
                        return false;
                    }
                    else if (this.gravelCount != chunkGenSettings$factory.gravelCount)
                    {
                        return false;
                    }
                    else if (this.gravelMaxHeight != chunkGenSettings$factory.gravelMaxHeight)
                    {
                        return false;
                    }
                    else if (this.gravelMinHeight != chunkGenSettings$factory.gravelMinHeight)
                    {
                        return false;
                    }
                    else if (this.gravelSize != chunkGenSettings$factory.gravelSize)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.heightScale, this.heightScale) != 0)
                    {
                        return false;
                    }
                    else if (this.ironCount != chunkGenSettings$factory.ironCount)
                    {
                        return false;
                    }
                    else if (this.ironMaxHeight != chunkGenSettings$factory.ironMaxHeight)
                    {
                        return false;
                    }
                    else if (this.ironMinHeight != chunkGenSettings$factory.ironMinHeight)
                    {
                        return false;
                    }
                    else if (this.ironSize != chunkGenSettings$factory.ironSize)
                    {
                        return false;
                    }
                    else if (this.lapisCenterHeight != chunkGenSettings$factory.lapisCenterHeight)
                    {
                        return false;
                    }
                    else if (this.lapisCount != chunkGenSettings$factory.lapisCount)
                    {
                        return false;
                    }
                    else if (this.lapisSize != chunkGenSettings$factory.lapisSize)
                    {
                        return false;
                    }
                    else if (this.lapisSpread != chunkGenSettings$factory.lapisSpread)
                    {
                        return false;
                    }
                    else if (this.lavaLakeChance != chunkGenSettings$factory.lavaLakeChance)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.lowerLimitScale, this.lowerLimitScale) != 0)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.mainNoiseScaleX, this.mainNoiseScaleX) != 0)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.mainNoiseScaleY, this.mainNoiseScaleY) != 0)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.mainNoiseScaleZ, this.mainNoiseScaleZ) != 0)
                    {
                        return false;
                    }
                    else if (this.redstoneCount != chunkGenSettings$factory.redstoneCount)
                    {
                        return false;
                    }
                    else if (this.redstoneMaxHeight != chunkGenSettings$factory.redstoneMaxHeight)
                    {
                        return false;
                    }
                    else if (this.redstoneMinHeight != chunkGenSettings$factory.redstoneMinHeight)
                    {
                        return false;
                    }
                    else if (this.redstoneSize != chunkGenSettings$factory.redstoneSize)
                    {
                        return false;
                    }
                    else if (this.riverSize != chunkGenSettings$factory.riverSize)
                    {
                        return false;
                    }
                    else if (this.seaLevel != chunkGenSettings$factory.seaLevel)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.stretchY, this.stretchY) != 0)
                    {
                        return false;
                    }
                    else if (Float.compare(chunkGenSettings$factory.upperLimitScale, this.upperLimitScale) != 0)
                    {
                        return false;
                    }
                    else if (this.useCaves != chunkGenSettings$factory.useCaves)
                    {
                        return false;
                    }
                    else if (this.useDungeons != chunkGenSettings$factory.useDungeons)
                    {
                        return false;
                    }
                    else if (this.useLavaLakes != chunkGenSettings$factory.useLavaLakes)
                    {
                        return false;
                    }
                    else if (this.useLavaOceans != chunkGenSettings$factory.useLavaOceans)
                    {
                        return false;
                    }
                    else if (this.useMineShafts != chunkGenSettings$factory.useMineShafts)
                    {
                        return false;
                    }
                    else if (this.useRavines != chunkGenSettings$factory.useRavines)
                    {
                        return false;
                    }
                    else if (this.useStrongholds != chunkGenSettings$factory.useStrongholds)
                    {
                        return false;
                    }
                    else if (this.useTemples != chunkGenSettings$factory.useTemples)
                    {
                        return false;
                    }
                    else if (this.useMonuments != chunkGenSettings$factory.useMonuments)
                    {
                        return false;
                    }
                    else if (this.useMansions != chunkGenSettings$factory.useMansions)
                    {
                        return false;
                    }
                    else if (this.useVillages != chunkGenSettings$factory.useVillages)
                    {
                        return false;
                    }
                    else if (this.useWaterLakes != chunkGenSettings$factory.useWaterLakes)
                    {
                        return false;
                    }
                    else
                    {
                        return this.waterLakeChance == chunkGenSettings$factory.waterLakeChance;
                    }
                }
                else
                {
                    return false;
                }
            }

            public int hashCode()
            {
                int i = this.coordinateScale == 0.0F ? 0 : Float.floatToIntBits(this.coordinateScale);
                i = 31 * i + (this.heightScale == 0.0F ? 0 : Float.floatToIntBits(this.heightScale));
                i = 31 * i + (this.upperLimitScale == 0.0F ? 0 : Float.floatToIntBits(this.upperLimitScale));
                i = 31 * i + (this.lowerLimitScale == 0.0F ? 0 : Float.floatToIntBits(this.lowerLimitScale));
                i = 31 * i + (this.depthNoiseScaleX == 0.0F ? 0 : Float.floatToIntBits(this.depthNoiseScaleX));
                i = 31 * i + (this.depthNoiseScaleZ == 0.0F ? 0 : Float.floatToIntBits(this.depthNoiseScaleZ));
                i = 31 * i + (this.depthNoiseScaleExponent == 0.0F ? 0 : Float.floatToIntBits(this.depthNoiseScaleExponent));
                i = 31 * i + (this.mainNoiseScaleX == 0.0F ? 0 : Float.floatToIntBits(this.mainNoiseScaleX));
                i = 31 * i + (this.mainNoiseScaleY == 0.0F ? 0 : Float.floatToIntBits(this.mainNoiseScaleY));
                i = 31 * i + (this.mainNoiseScaleZ == 0.0F ? 0 : Float.floatToIntBits(this.mainNoiseScaleZ));
                i = 31 * i + (this.baseSize == 0.0F ? 0 : Float.floatToIntBits(this.baseSize));
                i = 31 * i + (this.stretchY == 0.0F ? 0 : Float.floatToIntBits(this.stretchY));
                i = 31 * i + (this.biomeDepthWeight == 0.0F ? 0 : Float.floatToIntBits(this.biomeDepthWeight));
                i = 31 * i + (this.biomeDepthOffset == 0.0F ? 0 : Float.floatToIntBits(this.biomeDepthOffset));
                i = 31 * i + (this.biomeScaleWeight == 0.0F ? 0 : Float.floatToIntBits(this.biomeScaleWeight));
                i = 31 * i + (this.biomeScaleOffset == 0.0F ? 0 : Float.floatToIntBits(this.biomeScaleOffset));
                i = 31 * i + this.seaLevel;
                i = 31 * i + (this.useCaves ? 1 : 0);
                i = 31 * i + (this.useDungeons ? 1 : 0);
                i = 31 * i + this.dungeonChance;
                i = 31 * i + (this.useStrongholds ? 1 : 0);
                i = 31 * i + (this.useVillages ? 1 : 0);
                i = 31 * i + (this.useMineShafts ? 1 : 0);
                i = 31 * i + (this.useTemples ? 1 : 0);
                i = 31 * i + (this.useMonuments ? 1 : 0);
                i = 31 * i + (this.useMansions ? 1 : 0);
                i = 31 * i + (this.useRavines ? 1 : 0);
                i = 31 * i + (this.useWaterLakes ? 1 : 0);
                i = 31 * i + this.waterLakeChance;
                i = 31 * i + (this.useLavaLakes ? 1 : 0);
                i = 31 * i + this.lavaLakeChance;
                i = 31 * i + (this.useLavaOceans ? 1 : 0);
                i = 31 * i + this.fixedBiome;
                i = 31 * i + this.biomeSize;
                i = 31 * i + this.riverSize;
                i = 31 * i + this.dirtSize;
                i = 31 * i + this.dirtCount;
                i = 31 * i + this.dirtMinHeight;
                i = 31 * i + this.dirtMaxHeight;
                i = 31 * i + this.gravelSize;
                i = 31 * i + this.gravelCount;
                i = 31 * i + this.gravelMinHeight;
                i = 31 * i + this.gravelMaxHeight;
                i = 31 * i + this.graniteSize;
                i = 31 * i + this.graniteCount;
                i = 31 * i + this.graniteMinHeight;
                i = 31 * i + this.graniteMaxHeight;
                i = 31 * i + this.dioriteSize;
                i = 31 * i + this.dioriteCount;
                i = 31 * i + this.dioriteMinHeight;
                i = 31 * i + this.dioriteMaxHeight;
                i = 31 * i + this.andesiteSize;
                i = 31 * i + this.andesiteCount;
                i = 31 * i + this.andesiteMinHeight;
                i = 31 * i + this.andesiteMaxHeight;
                i = 31 * i + this.coalSize;
                i = 31 * i + this.coalCount;
                i = 31 * i + this.coalMinHeight;
                i = 31 * i + this.coalMaxHeight;
                i = 31 * i + this.ironSize;
                i = 31 * i + this.ironCount;
                i = 31 * i + this.ironMinHeight;
                i = 31 * i + this.ironMaxHeight;
                i = 31 * i + this.goldSize;
                i = 31 * i + this.goldCount;
                i = 31 * i + this.goldMinHeight;
                i = 31 * i + this.goldMaxHeight;
                i = 31 * i + this.redstoneSize;
                i = 31 * i + this.redstoneCount;
                i = 31 * i + this.redstoneMinHeight;
                i = 31 * i + this.redstoneMaxHeight;
                i = 31 * i + this.diamondSize;
                i = 31 * i + this.diamondCount;
                i = 31 * i + this.diamondMinHeight;
                i = 31 * i + this.diamondMaxHeight;
                i = 31 * i + this.lapisSize;
                i = 31 * i + this.lapisCount;
                i = 31 * i + this.lapisCenterHeight;
                i = 31 * i + this.lapisSpread;
                return i;
            }

            public ChunkGenSettings build()
            {
                return new ChunkGenSettings(this);
            }
        }

    public static class Serializer implements JsonDeserializer<ChunkGenSettings.Factory>, JsonSerializer<ChunkGenSettings.Factory>
        {
            public ChunkGenSettings.Factory deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException
            {
                JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
                ChunkGenSettings.Factory chunkGenSettings$factory = new ChunkGenSettings.Factory();

                try
                {
                    chunkGenSettings$factory.coordinateScale = JsonUtils.getFloat(jsonobject, "coordinateScale", chunkGenSettings$factory.coordinateScale);
                    chunkGenSettings$factory.heightScale = JsonUtils.getFloat(jsonobject, "heightScale", chunkGenSettings$factory.heightScale);
                    chunkGenSettings$factory.lowerLimitScale = JsonUtils.getFloat(jsonobject, "lowerLimitScale", chunkGenSettings$factory.lowerLimitScale);
                    chunkGenSettings$factory.upperLimitScale = JsonUtils.getFloat(jsonobject, "upperLimitScale", chunkGenSettings$factory.upperLimitScale);
                    chunkGenSettings$factory.depthNoiseScaleX = JsonUtils.getFloat(jsonobject, "depthNoiseScaleX", chunkGenSettings$factory.depthNoiseScaleX);
                    chunkGenSettings$factory.depthNoiseScaleZ = JsonUtils.getFloat(jsonobject, "depthNoiseScaleZ", chunkGenSettings$factory.depthNoiseScaleZ);
                    chunkGenSettings$factory.depthNoiseScaleExponent = JsonUtils.getFloat(jsonobject, "depthNoiseScaleExponent", chunkGenSettings$factory.depthNoiseScaleExponent);
                    chunkGenSettings$factory.mainNoiseScaleX = JsonUtils.getFloat(jsonobject, "mainNoiseScaleX", chunkGenSettings$factory.mainNoiseScaleX);
                    chunkGenSettings$factory.mainNoiseScaleY = JsonUtils.getFloat(jsonobject, "mainNoiseScaleY", chunkGenSettings$factory.mainNoiseScaleY);
                    chunkGenSettings$factory.mainNoiseScaleZ = JsonUtils.getFloat(jsonobject, "mainNoiseScaleZ", chunkGenSettings$factory.mainNoiseScaleZ);
                    chunkGenSettings$factory.baseSize = JsonUtils.getFloat(jsonobject, "baseSize", chunkGenSettings$factory.baseSize);
                    chunkGenSettings$factory.stretchY = JsonUtils.getFloat(jsonobject, "stretchY", chunkGenSettings$factory.stretchY);
                    chunkGenSettings$factory.biomeDepthWeight = JsonUtils.getFloat(jsonobject, "biomeDepthWeight", chunkGenSettings$factory.biomeDepthWeight);
                    chunkGenSettings$factory.biomeDepthOffset = JsonUtils.getFloat(jsonobject, "biomeDepthOffset", chunkGenSettings$factory.biomeDepthOffset);
                    chunkGenSettings$factory.biomeScaleWeight = JsonUtils.getFloat(jsonobject, "biomeScaleWeight", chunkGenSettings$factory.biomeScaleWeight);
                    chunkGenSettings$factory.biomeScaleOffset = JsonUtils.getFloat(jsonobject, "biomeScaleOffset", chunkGenSettings$factory.biomeScaleOffset);
                    chunkGenSettings$factory.seaLevel = JsonUtils.getInt(jsonobject, "seaLevel", chunkGenSettings$factory.seaLevel);
                    chunkGenSettings$factory.useCaves = JsonUtils.getBoolean(jsonobject, "useCaves", chunkGenSettings$factory.useCaves);
                    chunkGenSettings$factory.useDungeons = JsonUtils.getBoolean(jsonobject, "useDungeons", chunkGenSettings$factory.useDungeons);
                    chunkGenSettings$factory.dungeonChance = JsonUtils.getInt(jsonobject, "dungeonChance", chunkGenSettings$factory.dungeonChance);
                    chunkGenSettings$factory.useStrongholds = JsonUtils.getBoolean(jsonobject, "useStrongholds", chunkGenSettings$factory.useStrongholds);
                    chunkGenSettings$factory.useVillages = JsonUtils.getBoolean(jsonobject, "useVillages", chunkGenSettings$factory.useVillages);
                    chunkGenSettings$factory.useMineShafts = JsonUtils.getBoolean(jsonobject, "useMineShafts", chunkGenSettings$factory.useMineShafts);
                    chunkGenSettings$factory.useTemples = JsonUtils.getBoolean(jsonobject, "useTemples", chunkGenSettings$factory.useTemples);
                    chunkGenSettings$factory.useMonuments = JsonUtils.getBoolean(jsonobject, "useMonuments", chunkGenSettings$factory.useMonuments);
                    chunkGenSettings$factory.useMansions = JsonUtils.getBoolean(jsonobject, "useMansions", chunkGenSettings$factory.useMansions);
                    chunkGenSettings$factory.useRavines = JsonUtils.getBoolean(jsonobject, "useRavines", chunkGenSettings$factory.useRavines);
                    chunkGenSettings$factory.useWaterLakes = JsonUtils.getBoolean(jsonobject, "useWaterLakes", chunkGenSettings$factory.useWaterLakes);
                    chunkGenSettings$factory.waterLakeChance = JsonUtils.getInt(jsonobject, "waterLakeChance", chunkGenSettings$factory.waterLakeChance);
                    chunkGenSettings$factory.useLavaLakes = JsonUtils.getBoolean(jsonobject, "useLavaLakes", chunkGenSettings$factory.useLavaLakes);
                    chunkGenSettings$factory.lavaLakeChance = JsonUtils.getInt(jsonobject, "lavaLakeChance", chunkGenSettings$factory.lavaLakeChance);
                    chunkGenSettings$factory.useLavaOceans = JsonUtils.getBoolean(jsonobject, "useLavaOceans", chunkGenSettings$factory.useLavaOceans);
                    chunkGenSettings$factory.fixedBiome = JsonUtils.getInt(jsonobject, "fixedBiome", chunkGenSettings$factory.fixedBiome);

                    if (chunkGenSettings$factory.fixedBiome < 38 && chunkGenSettings$factory.fixedBiome >= -1)
                    {
                        if (chunkGenSettings$factory.fixedBiome >= Biome.getIdForBiome(Biomes.HELL))
                        {
                            chunkGenSettings$factory.fixedBiome += 2;
                        }
                    }
                    else
                    {
                        chunkGenSettings$factory.fixedBiome = -1;
                    }

                    chunkGenSettings$factory.biomeSize = JsonUtils.getInt(jsonobject, "biomeSize", chunkGenSettings$factory.biomeSize);
                    chunkGenSettings$factory.riverSize = JsonUtils.getInt(jsonobject, "riverSize", chunkGenSettings$factory.riverSize);
                    chunkGenSettings$factory.dirtSize = JsonUtils.getInt(jsonobject, "dirtSize", chunkGenSettings$factory.dirtSize);
                    chunkGenSettings$factory.dirtCount = JsonUtils.getInt(jsonobject, "dirtCount", chunkGenSettings$factory.dirtCount);
                    chunkGenSettings$factory.dirtMinHeight = JsonUtils.getInt(jsonobject, "dirtMinHeight", chunkGenSettings$factory.dirtMinHeight);
                    chunkGenSettings$factory.dirtMaxHeight = JsonUtils.getInt(jsonobject, "dirtMaxHeight", chunkGenSettings$factory.dirtMaxHeight);
                    chunkGenSettings$factory.gravelSize = JsonUtils.getInt(jsonobject, "gravelSize", chunkGenSettings$factory.gravelSize);
                    chunkGenSettings$factory.gravelCount = JsonUtils.getInt(jsonobject, "gravelCount", chunkGenSettings$factory.gravelCount);
                    chunkGenSettings$factory.gravelMinHeight = JsonUtils.getInt(jsonobject, "gravelMinHeight", chunkGenSettings$factory.gravelMinHeight);
                    chunkGenSettings$factory.gravelMaxHeight = JsonUtils.getInt(jsonobject, "gravelMaxHeight", chunkGenSettings$factory.gravelMaxHeight);
                    chunkGenSettings$factory.graniteSize = JsonUtils.getInt(jsonobject, "graniteSize", chunkGenSettings$factory.graniteSize);
                    chunkGenSettings$factory.graniteCount = JsonUtils.getInt(jsonobject, "graniteCount", chunkGenSettings$factory.graniteCount);
                    chunkGenSettings$factory.graniteMinHeight = JsonUtils.getInt(jsonobject, "graniteMinHeight", chunkGenSettings$factory.graniteMinHeight);
                    chunkGenSettings$factory.graniteMaxHeight = JsonUtils.getInt(jsonobject, "graniteMaxHeight", chunkGenSettings$factory.graniteMaxHeight);
                    chunkGenSettings$factory.dioriteSize = JsonUtils.getInt(jsonobject, "dioriteSize", chunkGenSettings$factory.dioriteSize);
                    chunkGenSettings$factory.dioriteCount = JsonUtils.getInt(jsonobject, "dioriteCount", chunkGenSettings$factory.dioriteCount);
                    chunkGenSettings$factory.dioriteMinHeight = JsonUtils.getInt(jsonobject, "dioriteMinHeight", chunkGenSettings$factory.dioriteMinHeight);
                    chunkGenSettings$factory.dioriteMaxHeight = JsonUtils.getInt(jsonobject, "dioriteMaxHeight", chunkGenSettings$factory.dioriteMaxHeight);
                    chunkGenSettings$factory.andesiteSize = JsonUtils.getInt(jsonobject, "andesiteSize", chunkGenSettings$factory.andesiteSize);
                    chunkGenSettings$factory.andesiteCount = JsonUtils.getInt(jsonobject, "andesiteCount", chunkGenSettings$factory.andesiteCount);
                    chunkGenSettings$factory.andesiteMinHeight = JsonUtils.getInt(jsonobject, "andesiteMinHeight", chunkGenSettings$factory.andesiteMinHeight);
                    chunkGenSettings$factory.andesiteMaxHeight = JsonUtils.getInt(jsonobject, "andesiteMaxHeight", chunkGenSettings$factory.andesiteMaxHeight);
                    chunkGenSettings$factory.coalSize = JsonUtils.getInt(jsonobject, "coalSize", chunkGenSettings$factory.coalSize);
                    chunkGenSettings$factory.coalCount = JsonUtils.getInt(jsonobject, "coalCount", chunkGenSettings$factory.coalCount);
                    chunkGenSettings$factory.coalMinHeight = JsonUtils.getInt(jsonobject, "coalMinHeight", chunkGenSettings$factory.coalMinHeight);
                    chunkGenSettings$factory.coalMaxHeight = JsonUtils.getInt(jsonobject, "coalMaxHeight", chunkGenSettings$factory.coalMaxHeight);
                    chunkGenSettings$factory.ironSize = JsonUtils.getInt(jsonobject, "ironSize", chunkGenSettings$factory.ironSize);
                    chunkGenSettings$factory.ironCount = JsonUtils.getInt(jsonobject, "ironCount", chunkGenSettings$factory.ironCount);
                    chunkGenSettings$factory.ironMinHeight = JsonUtils.getInt(jsonobject, "ironMinHeight", chunkGenSettings$factory.ironMinHeight);
                    chunkGenSettings$factory.ironMaxHeight = JsonUtils.getInt(jsonobject, "ironMaxHeight", chunkGenSettings$factory.ironMaxHeight);
                    chunkGenSettings$factory.goldSize = JsonUtils.getInt(jsonobject, "goldSize", chunkGenSettings$factory.goldSize);
                    chunkGenSettings$factory.goldCount = JsonUtils.getInt(jsonobject, "goldCount", chunkGenSettings$factory.goldCount);
                    chunkGenSettings$factory.goldMinHeight = JsonUtils.getInt(jsonobject, "goldMinHeight", chunkGenSettings$factory.goldMinHeight);
                    chunkGenSettings$factory.goldMaxHeight = JsonUtils.getInt(jsonobject, "goldMaxHeight", chunkGenSettings$factory.goldMaxHeight);
                    chunkGenSettings$factory.redstoneSize = JsonUtils.getInt(jsonobject, "redstoneSize", chunkGenSettings$factory.redstoneSize);
                    chunkGenSettings$factory.redstoneCount = JsonUtils.getInt(jsonobject, "redstoneCount", chunkGenSettings$factory.redstoneCount);
                    chunkGenSettings$factory.redstoneMinHeight = JsonUtils.getInt(jsonobject, "redstoneMinHeight", chunkGenSettings$factory.redstoneMinHeight);
                    chunkGenSettings$factory.redstoneMaxHeight = JsonUtils.getInt(jsonobject, "redstoneMaxHeight", chunkGenSettings$factory.redstoneMaxHeight);
                    chunkGenSettings$factory.diamondSize = JsonUtils.getInt(jsonobject, "diamondSize", chunkGenSettings$factory.diamondSize);
                    chunkGenSettings$factory.diamondCount = JsonUtils.getInt(jsonobject, "diamondCount", chunkGenSettings$factory.diamondCount);
                    chunkGenSettings$factory.diamondMinHeight = JsonUtils.getInt(jsonobject, "diamondMinHeight", chunkGenSettings$factory.diamondMinHeight);
                    chunkGenSettings$factory.diamondMaxHeight = JsonUtils.getInt(jsonobject, "diamondMaxHeight", chunkGenSettings$factory.diamondMaxHeight);
                    chunkGenSettings$factory.lapisSize = JsonUtils.getInt(jsonobject, "lapisSize", chunkGenSettings$factory.lapisSize);
                    chunkGenSettings$factory.lapisCount = JsonUtils.getInt(jsonobject, "lapisCount", chunkGenSettings$factory.lapisCount);
                    chunkGenSettings$factory.lapisCenterHeight = JsonUtils.getInt(jsonobject, "lapisCenterHeight", chunkGenSettings$factory.lapisCenterHeight);
                    chunkGenSettings$factory.lapisSpread = JsonUtils.getInt(jsonobject, "lapisSpread", chunkGenSettings$factory.lapisSpread);
                }
                catch (Exception var7)
                {
                    ;
                }

                return chunkGenSettings$factory;
            }

            public JsonElement serialize(ChunkGenSettings.Factory p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_)
            {
                JsonObject jsonobject = new JsonObject();
                jsonobject.addProperty("coordinateScale", Float.valueOf(p_serialize_1_.coordinateScale));
                jsonobject.addProperty("heightScale", Float.valueOf(p_serialize_1_.heightScale));
                jsonobject.addProperty("lowerLimitScale", Float.valueOf(p_serialize_1_.lowerLimitScale));
                jsonobject.addProperty("upperLimitScale", Float.valueOf(p_serialize_1_.upperLimitScale));
                jsonobject.addProperty("depthNoiseScaleX", Float.valueOf(p_serialize_1_.depthNoiseScaleX));
                jsonobject.addProperty("depthNoiseScaleZ", Float.valueOf(p_serialize_1_.depthNoiseScaleZ));
                jsonobject.addProperty("depthNoiseScaleExponent", Float.valueOf(p_serialize_1_.depthNoiseScaleExponent));
                jsonobject.addProperty("mainNoiseScaleX", Float.valueOf(p_serialize_1_.mainNoiseScaleX));
                jsonobject.addProperty("mainNoiseScaleY", Float.valueOf(p_serialize_1_.mainNoiseScaleY));
                jsonobject.addProperty("mainNoiseScaleZ", Float.valueOf(p_serialize_1_.mainNoiseScaleZ));
                jsonobject.addProperty("baseSize", Float.valueOf(p_serialize_1_.baseSize));
                jsonobject.addProperty("stretchY", Float.valueOf(p_serialize_1_.stretchY));
                jsonobject.addProperty("biomeDepthWeight", Float.valueOf(p_serialize_1_.biomeDepthWeight));
                jsonobject.addProperty("biomeDepthOffset", Float.valueOf(p_serialize_1_.biomeDepthOffset));
                jsonobject.addProperty("biomeScaleWeight", Float.valueOf(p_serialize_1_.biomeScaleWeight));
                jsonobject.addProperty("biomeScaleOffset", Float.valueOf(p_serialize_1_.biomeScaleOffset));
                jsonobject.addProperty("seaLevel", Integer.valueOf(p_serialize_1_.seaLevel));
                jsonobject.addProperty("useCaves", Boolean.valueOf(p_serialize_1_.useCaves));
                jsonobject.addProperty("useDungeons", Boolean.valueOf(p_serialize_1_.useDungeons));
                jsonobject.addProperty("dungeonChance", Integer.valueOf(p_serialize_1_.dungeonChance));
                jsonobject.addProperty("useStrongholds", Boolean.valueOf(p_serialize_1_.useStrongholds));
                jsonobject.addProperty("useVillages", Boolean.valueOf(p_serialize_1_.useVillages));
                jsonobject.addProperty("useMineShafts", Boolean.valueOf(p_serialize_1_.useMineShafts));
                jsonobject.addProperty("useTemples", Boolean.valueOf(p_serialize_1_.useTemples));
                jsonobject.addProperty("useMonuments", Boolean.valueOf(p_serialize_1_.useMonuments));
                jsonobject.addProperty("useMansions", Boolean.valueOf(p_serialize_1_.useMansions));
                jsonobject.addProperty("useRavines", Boolean.valueOf(p_serialize_1_.useRavines));
                jsonobject.addProperty("useWaterLakes", Boolean.valueOf(p_serialize_1_.useWaterLakes));
                jsonobject.addProperty("waterLakeChance", Integer.valueOf(p_serialize_1_.waterLakeChance));
                jsonobject.addProperty("useLavaLakes", Boolean.valueOf(p_serialize_1_.useLavaLakes));
                jsonobject.addProperty("lavaLakeChance", Integer.valueOf(p_serialize_1_.lavaLakeChance));
                jsonobject.addProperty("useLavaOceans", Boolean.valueOf(p_serialize_1_.useLavaOceans));
                jsonobject.addProperty("fixedBiome", Integer.valueOf(p_serialize_1_.fixedBiome));
                jsonobject.addProperty("biomeSize", Integer.valueOf(p_serialize_1_.biomeSize));
                jsonobject.addProperty("riverSize", Integer.valueOf(p_serialize_1_.riverSize));
                jsonobject.addProperty("dirtSize", Integer.valueOf(p_serialize_1_.dirtSize));
                jsonobject.addProperty("dirtCount", Integer.valueOf(p_serialize_1_.dirtCount));
                jsonobject.addProperty("dirtMinHeight", Integer.valueOf(p_serialize_1_.dirtMinHeight));
                jsonobject.addProperty("dirtMaxHeight", Integer.valueOf(p_serialize_1_.dirtMaxHeight));
                jsonobject.addProperty("gravelSize", Integer.valueOf(p_serialize_1_.gravelSize));
                jsonobject.addProperty("gravelCount", Integer.valueOf(p_serialize_1_.gravelCount));
                jsonobject.addProperty("gravelMinHeight", Integer.valueOf(p_serialize_1_.gravelMinHeight));
                jsonobject.addProperty("gravelMaxHeight", Integer.valueOf(p_serialize_1_.gravelMaxHeight));
                jsonobject.addProperty("graniteSize", Integer.valueOf(p_serialize_1_.graniteSize));
                jsonobject.addProperty("graniteCount", Integer.valueOf(p_serialize_1_.graniteCount));
                jsonobject.addProperty("graniteMinHeight", Integer.valueOf(p_serialize_1_.graniteMinHeight));
                jsonobject.addProperty("graniteMaxHeight", Integer.valueOf(p_serialize_1_.graniteMaxHeight));
                jsonobject.addProperty("dioriteSize", Integer.valueOf(p_serialize_1_.dioriteSize));
                jsonobject.addProperty("dioriteCount", Integer.valueOf(p_serialize_1_.dioriteCount));
                jsonobject.addProperty("dioriteMinHeight", Integer.valueOf(p_serialize_1_.dioriteMinHeight));
                jsonobject.addProperty("dioriteMaxHeight", Integer.valueOf(p_serialize_1_.dioriteMaxHeight));
                jsonobject.addProperty("andesiteSize", Integer.valueOf(p_serialize_1_.andesiteSize));
                jsonobject.addProperty("andesiteCount", Integer.valueOf(p_serialize_1_.andesiteCount));
                jsonobject.addProperty("andesiteMinHeight", Integer.valueOf(p_serialize_1_.andesiteMinHeight));
                jsonobject.addProperty("andesiteMaxHeight", Integer.valueOf(p_serialize_1_.andesiteMaxHeight));
                jsonobject.addProperty("coalSize", Integer.valueOf(p_serialize_1_.coalSize));
                jsonobject.addProperty("coalCount", Integer.valueOf(p_serialize_1_.coalCount));
                jsonobject.addProperty("coalMinHeight", Integer.valueOf(p_serialize_1_.coalMinHeight));
                jsonobject.addProperty("coalMaxHeight", Integer.valueOf(p_serialize_1_.coalMaxHeight));
                jsonobject.addProperty("ironSize", Integer.valueOf(p_serialize_1_.ironSize));
                jsonobject.addProperty("ironCount", Integer.valueOf(p_serialize_1_.ironCount));
                jsonobject.addProperty("ironMinHeight", Integer.valueOf(p_serialize_1_.ironMinHeight));
                jsonobject.addProperty("ironMaxHeight", Integer.valueOf(p_serialize_1_.ironMaxHeight));
                jsonobject.addProperty("goldSize", Integer.valueOf(p_serialize_1_.goldSize));
                jsonobject.addProperty("goldCount", Integer.valueOf(p_serialize_1_.goldCount));
                jsonobject.addProperty("goldMinHeight", Integer.valueOf(p_serialize_1_.goldMinHeight));
                jsonobject.addProperty("goldMaxHeight", Integer.valueOf(p_serialize_1_.goldMaxHeight));
                jsonobject.addProperty("redstoneSize", Integer.valueOf(p_serialize_1_.redstoneSize));
                jsonobject.addProperty("redstoneCount", Integer.valueOf(p_serialize_1_.redstoneCount));
                jsonobject.addProperty("redstoneMinHeight", Integer.valueOf(p_serialize_1_.redstoneMinHeight));
                jsonobject.addProperty("redstoneMaxHeight", Integer.valueOf(p_serialize_1_.redstoneMaxHeight));
                jsonobject.addProperty("diamondSize", Integer.valueOf(p_serialize_1_.diamondSize));
                jsonobject.addProperty("diamondCount", Integer.valueOf(p_serialize_1_.diamondCount));
                jsonobject.addProperty("diamondMinHeight", Integer.valueOf(p_serialize_1_.diamondMinHeight));
                jsonobject.addProperty("diamondMaxHeight", Integer.valueOf(p_serialize_1_.diamondMaxHeight));
                jsonobject.addProperty("lapisSize", Integer.valueOf(p_serialize_1_.lapisSize));
                jsonobject.addProperty("lapisCount", Integer.valueOf(p_serialize_1_.lapisCount));
                jsonobject.addProperty("lapisCenterHeight", Integer.valueOf(p_serialize_1_.lapisCenterHeight));
                jsonobject.addProperty("lapisSpread", Integer.valueOf(p_serialize_1_.lapisSpread));
                return jsonobject;
            }
        }
}
