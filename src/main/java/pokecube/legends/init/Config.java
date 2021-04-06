package pokecube.legends.init;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.legends.Reference;
import pokecube.legends.conditions.LegendaryConditions;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class Config extends ConfigData
{
    // Enable Condition Legendary
    @Configure(category = "general")
    public boolean enabledcondition = true;

    @Configure(category = "general", comment = "Temples cannot be defiled by players. [Default: true]")
    public boolean protectTemples = true;

    @Configure(category = "general", comment = "If legends can only spawn once. [Default: false]")
    public boolean singleUseLegendSpawns = false;
    @Configure(category = "general", comment = "Delay in ticks for legends to respawn. [Default: 36000]")
    public int     respawnLegendDelay    = 36000;
    @Configure(category = "general", comment = "Should ores generate. [Default: true]")
    public boolean generateOres          = true;

    // Meteor adjustments
    @Configure(category = "meteors", comment = "Size and power of meteors. Anything above 100 is not recommended. [Default: 20]")
    public double meteorPowerThreshold = 20;
    @Configure(category = "meteors", comment = "Chance for meteors to spawn. [Default: 0.15]")
    public double meteorChanceForAny   = 0.15;
    @Configure(category = "meteors", comment = "Chance for meteors to spawn with Cosmic Dust Ores. [Default: 0.25]")
    public double meteorChanceForDust  = 0.25;

    // Raids
    @Configure(category = "raids", comment = "Duration of raids in ticks. [Default: 3000]")
    public int    raidDuration    = 3000;
    @Configure(category = "raids", comment = "Chance for a raid to reset. [Default: 0.1]")
    public double raidResetChance = 0.1;
    @Configure(category = "raids", comment = "Chance for a reset raid to be rare. [Default: 0.1]")
    public double rareRaidChance  = 0.1;

    // Mirage Spot(Hoopa Ring)
    @Configure(category = "mirage", comment = "Portals will reset randomly based on this value, higher values result in longer times between use. [Default: 24000]")
    public int ticksPerPortalReset = 24000;
    @Configure(category = "mirage", comment = "Minimum level for Hoopa to generate the portal. Numbers below the minimum will be reset to the default. [Min: 40, Max: 100] [Default: 50]")
    public int levelCreatePortal   = 50;
    @Configure(category = "mirage", comment = "The time you need to wait for Hoopa to generate a new portal. [Default: 8400]")
    public int ticksPerPortalSpawn = 8400;
    @Configure(category = "mirage", comment = "The time for the portal to disappear in ticks. 1 Tick = 20 seconds. [Default: 1200]")
    public int ticksPortalDespawn  = 1200;

    // Ultra Space
    @Configure(category = "ultraspace", comment = "Allows the Ultra key to consume fuel. [Default: true]")
    public boolean enableUltraKeyConsume = true;
    @Configure(category = "ultraspace", comment = "Amount of fuel for the Ultra Key to consume. [Default: 5]")
    public int ultraKeyConsumeAmount = 5;

    // Distortic World
    @Configure(category = "distortic", comment = "Cooldown for the mirror in ticks. [Default: 800]")
    public int mirrorCooldown = 800;

    private final LegendaryConditions conditions = new LegendaryConditions();

    private boolean conditionsReged = false;

    public final Set<String> PROTECTED_STRUCTURES = Sets.newHashSet();

    public final Map<String, List<PokedexEntry>> STRUCTURE_ENTRIES = Maps.newHashMap();

    public boolean loaded = false;

    public Config()
    {
        super(Reference.ID);
    }

    @Override
    public void onUpdated()
    {
        if (!this.loaded) return;
        if (this.enabledcondition && !this.conditionsReged)
        {
            this.conditions.init();
            this.conditionsReged = true;
        }

        final List<String> temples = Lists.newArrayList(
        //@formatter:off
            "pokecube_legends:regice_temple->regice",
            "pokecube_legends:regirock_temple->regirock",
            "pokecube_legends:registeel_temple->registeel",
            "pokecube_legends:regidrago_temple->regidrago",
            "pokecube_legends:regieleki_temple->regieleki",
            "pokecube_legends:regigigas_temple->regigigas",
            "pokecube_legends:celebi_temple",
            "pokecube_legends:lugia_tower",
            "pokecube_legends:hooh_tower",
            "pokecube_legends:zacian_temple",
            "pokecube_legends:zamazenta_temple",
            "pokecube_legends:space_temple",
            "pokecube_legends:groudon_temple",
            "pokecube_legends:kyogre_temple",
            "pokecube_legends:sky_pillar",
            "pokecube_legends:kubfu_dark",
            "pokecube_legends:kubfu_water",
            "pokecube_legends:keldeo_place",
            "pokecube_legends:nature_place",
            "pokecube_legends:legendy_tree",
            "pokecube_legends:xerneas_place",
            "pokecube_legends:yveltal_place",
            "pokecube_legends:castle_n",
            "pokecube_legends:elite_four",
            "pokecube_legends:tapus_temple",
            "pokecube_legends:ultra_pyramid",
            "pokecube_legends:necrozma_tower"
        //@formatter:on
        );

        // Some hardcoded values here for now
        this.PROTECTED_STRUCTURES.clear();
        this.STRUCTURE_ENTRIES.clear();

        temples.forEach(s ->
        {
            if (!s.contains("->")) this.PROTECTED_STRUCTURES.add(s);
            else
            {
                String[] args = s.split("->");
                if (args.length != 2) return;
                final String key = args[0];
                final String value = args[1];
                this.PROTECTED_STRUCTURES.add(key);
                final List<PokedexEntry> entries = Lists.newArrayList();
                if (value.contains(","))
                {
                    args = value.split(",");
                    for (final String val : args)
                    {
                        final PokedexEntry entry = Database.getEntry(val);
                        if (entry != null) entries.add(entry);
                    }
                }
                else
                {
                    final PokedexEntry entry = Database.getEntry(value);
                    if (entry != null) entries.add(entry);
                }
                this.STRUCTURE_ENTRIES.put(key, entries);
            }
        });

        if (this.enableUltraKeyConsume == true) if (this.ultraKeyConsumeAmount <= 1 || this.ultraKeyConsumeAmount >= 30)
            this.ultraKeyConsumeAmount = 5;

        if (this.mirrorCooldown <= 300) this.mirrorCooldown = 800;

        if (this.levelCreatePortal <= 39) this.levelCreatePortal = 20;
    }
}
