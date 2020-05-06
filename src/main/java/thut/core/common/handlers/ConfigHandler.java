package thut.core.common.handlers;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import thut.api.boom.ExplosionCustom;
import thut.api.entity.blockentity.BlockEntityUpdater;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.terrain.TerrainSegment;
import thut.core.common.ThutCore;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;
import thut.core.common.terrain.ConfigTerrainBuilder;
import thut.core.common.terrain.ConfigTerrainChecker;

public class ConfigHandler extends ConfigData
{

    private static final String BOOMS       = "explosions";
    private static final String BIOMES      = "biomes";
    private static final String BLOCKENTITY = "blockentity";
    private static final String MISC        = "misc";
    private static final String CLIENT      = "client";

    @Configure(category = ConfigHandler.BOOMS)
    private final int           explosionRadius     = 127;
    @Configure(category = ConfigHandler.BOOMS)
    private final List<Integer> explosionRate       = Lists.newArrayList(new Integer[] { 2000, 10000 });
    @Configure(category = ConfigHandler.BOOMS)
    private final boolean       affectAir           = true;
    @Configure(category = ConfigHandler.BOOMS)
    private final double        minBlastEffect      = 0.25;
    @Configure(category = ConfigHandler.BIOMES)
    public boolean              resetAllTerrain     = false;
    @Configure(category = ConfigHandler.BIOMES)
    public List<String>         customBiomeMappings = Lists.newArrayList();

    @Configure(category = ConfigHandler.BLOCKENTITY)
    public List<String> teblacklist             = Lists.newArrayList();
    @Configure(category = ConfigHandler.BLOCKENTITY)
    public List<String> blockblacklist          = Lists.newArrayList(new String[] { "minecraft:bedrock" });
    @Configure(category = ConfigHandler.BLOCKENTITY)
    public boolean      autoBlacklistErroredTEs = true;
    @Configure(category = ConfigHandler.MISC)
    public boolean      debug                   = true;

    @Configure(category = ConfigHandler.CLIENT)
    public int mouseFloodCtrl = 30;

    public ConfigHandler()
    {
        super(ThutCore.MODID);
    }

    @Override
    public void onUpdated()
    {
        ExplosionCustom.MAX_RADIUS = this.explosionRadius;
        ExplosionCustom.AFFECTINAIR = this.affectAir;
        if (this.explosionRate.size() == 2) ExplosionCustom.MAXPERTICK = this.explosionRate.toArray(new Integer[2]);
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
        for (final String s : this.blockblacklist)
            IBlockEntity.BLOCKBLACKLIST.add(new ResourceLocation(s));
        TerrainSegment.biomeCheckers.removeIf(t -> t instanceof ConfigTerrainChecker);
        ConfigTerrainBuilder.process(this.customBiomeMappings);
    }
}
