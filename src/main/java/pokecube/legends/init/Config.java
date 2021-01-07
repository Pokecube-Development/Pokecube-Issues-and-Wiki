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
    // Enabla Condition Legendary
    @Configure(category = "general")
    public boolean enabledcondition = true;

    @Configure(category = "general", comment = "If true, temples cannot be defiled by players.")
    public boolean protectTemples = true;

    @Configure(category = "general")
    public boolean singleUseLegendSpawns = false;
    @Configure(category = "general")
    public int     respawnLegendDelay    = 36000;
    @Configure(category = "general")
    public boolean generateOres          = true;

    // Meteor adjustments
    @Configure(category = "meteors")
    public double meteorPowerThreshold = 20;
    @Configure(category = "meteors")
    public double meteorChanceForAny   = 0.01;
    @Configure(category = "meteors")
    public double meteorChanceForDust  = 0.25;

    // Raids
    @Configure(category = "raids")
    public int raidDuration = 3000;

    // Mirage Spot(Hoppa Ring)
    @Configure(category = "mirage")
    public boolean enabledmirage       = true;
    @Configure(category = "mirage")
    public double  mirageRespawnChance = 0.01;

    // Ultra Space
    @Configure(category = "ultraspace")
    public boolean enabledkeyusecombustible = true;
    @Configure(category = "ultraspace")
    public int     itemCombustiveStack      = 5;

    // Distortic World
    @Configure(category = "distortic")
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
            "pokecube_legends:elite_four"
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

        if (this.enabledkeyusecombustible == true) if (this.itemCombustiveStack <= 1 || this.itemCombustiveStack >= 30)
            this.itemCombustiveStack = 5;

        if (this.mirrorCooldown <= 300) this.mirrorCooldown = 800;
    }
}
