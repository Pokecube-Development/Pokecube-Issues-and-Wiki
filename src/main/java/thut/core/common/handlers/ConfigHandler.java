package thut.core.common.handlers;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import thut.api.boom.ExplosionCustom;
import thut.api.data.StringTag;
import thut.api.entity.blockentity.BlockEntityUpdater;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.level.terrain.TerrainChecker;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Cruncher;
import thut.core.common.ThutCore;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;
import thut.core.common.terrain.ConfigTerrainBuilder;
import thut.core.common.terrain.ConfigTerrainChecker;

public class ConfigHandler extends ConfigData
{

    private static final String BOOMS = "explosions";
    private static final String BIOMES = "biomes";
    private static final String BLOCKENTITY = "blockentity";
    private static final String MISC = "misc";
    private static final String CLIENT = "client";
    private static final String WORLD = "generation";
    private static final String DEBUG = "debug";

    @Configure(category = WORLD, comment = "Structures listed here will have the relevant subbiome applied for if minecraft thinks that the block is inside the structure.")
    public List<String> structure_subbiomes = Lists.newArrayList(
    //@formatter:off pokecube_world:meteorites
            "{\"struct\":\"#pokecube_world:village\",\"subbiome\":\"village\"}",
            "{\"struct\":\"#pokecube_world:town\",\"subbiome\":\"village\"}",
            "{\"struct\":\"#minecraft:village\",\"subbiome\":\"village\"}",
            "{\"struct\":\"#minecraft:on_ocean_explorer_maps\",\"subbiome\":\"monument\"}"
            );
    //@formatter:on
    @Configure(category = WORLD, comment = "Does a blanket \"plant material\" check for cuttable and edible plants, rather than relying entirely on the block tags. [Default: true]")
    public boolean autoPopulateLists = true;

    @Configure(category = ConfigHandler.BOOMS)
    public int maxMsPerTick = 25;

    @Configure(category = ConfigHandler.BOOMS)
    public int explosionRadius = 127;
    @Configure(category = ConfigHandler.BOOMS)
    public double minBlastEffect = 0.25;
    @Configure(category = ConfigHandler.BOOMS)
    public boolean affectAir = true;
    @Configure(category = ConfigHandler.BOOMS)
    public boolean generateCache = true;

    @Configure(category = ConfigHandler.BIOMES)
    public boolean resetAllTerrain = false;
    @Configure(category = ConfigHandler.BIOMES)
    public List<String> customBiomeMappings = Lists.newArrayList();

    @Configure(category = ConfigHandler.BLOCKENTITY)
    public List<String> teblacklist = Lists.newArrayList();
    @Configure(category = ConfigHandler.BLOCKENTITY)
    public List<String> blockblacklist = Lists.newArrayList(new String[]
    { "minecraft:bedrock" });
    @Configure(category = ConfigHandler.BLOCKENTITY)
    public boolean autoBlacklistErroredTEs = true;
    @Configure(category = ConfigHandler.MISC)
    public boolean supress_warns = false;

    @Configure(category = ConfigHandler.CLIENT)
    public boolean asyncModelLoads = true;

    @Configure(category = ConfigHandler.CLIENT)
    public double modelCullThreshold = 1e-1;

    @Configure(category = ConfigHandler.DEBUG)
    public boolean debug = false;
    @Configure(category = ConfigHandler.DEBUG)
    public boolean debug_data = false;
    @Configure(category = ConfigHandler.DEBUG)
    public boolean debug_models = false;

    public static final StringTag<String> STRUCTURE_SUBBIOMES = new StringTag<>("tags/structure_subbiomes/",
            String.class, TerrainChecker::initStructMap);

    public ConfigHandler()
    {
        super(ThutCore.MODID);
    }

    @Override
    public void onUpdated()
    {
        ExplosionCustom.MAX_RADIUS = this.explosionRadius;
        ExplosionCustom.AFFECTINAIR = this.affectAir;
        ExplosionCustom.MAXPERTICK = this.maxMsPerTick;
        ExplosionCustom.MINBLASTDAMAGE = (float) this.minBlastEffect;
        TerrainSegment.noLoad = this.resetAllTerrain;
        IBlockEntity.TEBLACKLIST.clear();
        BlockEntityUpdater.autoBlacklist = this.autoBlacklistErroredTEs;
        for (String s : this.teblacklist)
        {
            if (!s.contains(":")) s = "minecraft:" + s;
            IBlockEntity.TEBLACKLIST.add(s);
            IBlockEntity.TEBLACKLIST.add(s.toLowerCase(Locale.ENGLISH));
        }
        for (final String s : this.blockblacklist) IBlockEntity.BLOCKBLACKLIST.add(new ResourceLocation(s));
        TerrainSegment.biomeCheckers.removeIf(t -> t instanceof ConfigTerrainChecker);
        ConfigTerrainBuilder.process(this.customBiomeMappings);
        if (this.generateCache) Cruncher.init();
        TerrainChecker.initStructMap();
    }
}
