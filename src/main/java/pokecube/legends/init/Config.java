package pokecube.legends.init;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.core.database.Database;
import pokecube.gimmicks.dynamax.DynamaxRaid;
import pokecube.gimmicks.terastal.TerastalRaid;
import pokecube.legends.Reference;
import pokecube.legends.conditions.custom.LegendaryConditions;
import pokecube.legends.entity.WormholeEntity;
import pokecube.legends.spawns.WormholeSpawns;
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
    public int respawnLegendDelay = 36000;
    @Configure(category = "general", comment = "Should ores generate. [Default: true]")
    public boolean generateOres = true;
    @Configure(category = "general", comment = "Arceus will protect these structures.")
    public List<String> arceus_protected_structures = Lists.newArrayList();

    // Meteor adjustments
    @Configure(category = "meteors", comment = "Threshold for meteor explosions to leave lava. [Default: 50]")
    public double meteorPowerThreshold = 20;
    @Configure(category = "meteors", comment = "Chance for meteors to spawn. [Default: 0.15]")
    public double meteorChanceForAny = 0.15;

    // Raids
    @Configure(category = "raids", comment = "Duration of raids in ticks. [Default: 3000]")
    public int raidDuration = 3000;
    @Configure(category = "raids", comment = "Chance for a raid to reset. [Default: 0.1]")
    public double raidResetChance = 0.1;
    @Configure(category = "raids", comment = "Chance for a reset raid to be rare. [Default: 0.1]")
    public double rareRaidChance = 0.1;

    // Mirage Spot(Hoopa Ring)
    @Configure(category = "mirage", comment = "Portals will reset randomly based on this value, higher values result in longer times between use. [Default: 24000]")
    public int ticksPerPortalReset = 24000;
    @Configure(category = "mirage", comment = "Minimum level for Hoopa to generate the portal. Numbers below the minimum will be reset to the default. [Min: 40, Max: 100] [Default: 50]")
    public int levelCreatePortal = 50;
    @Configure(category = "mirage", comment = "The time you need to wait for Hoopa to generate a new portal. [Default: 8400]")
    public int ticksPerPortalSpawn = 8400;
    @Configure(category = "mirage", comment = "The time for the portal to disappear in ticks. 1 Tick = 20 seconds. [Default: 1200]")
    public int ticksPortalDespawn = 1200;

    // Ultra Space
    @Configure(category = "ultraspace", comment = "Requires the Ultra Key to consume Cosmic Dust. [Default: true]")
    public boolean ultraKeyRequireFuel = true;
    @Configure(category = "ultraspace", comment = "Amount of Cosmic Dust for the Ultra Key to consume. [Default: 5]")
    public int ultraKeyRequiredFuelAmount = 5;
    @Configure(category = "ultraspace", comment = "Cooldown for the Ultra Key in ticks. [Default: 200]")
    public int ultraKeyCooldown = 200;

    // Distortic World
    @Configure(category = "distortic", comment = "Cooldown for the mirror in ticks. [Default: 800]")
    public int mirrorCooldown = 800;

    // Wormholes
    @Configure(category = "wormholes", comment = "Wormholes break when they exceed this much energy. [Default: 1000000]")
    public int maxWormholeEnergy = 1000000;
    @Configure(category = "wormholes", comment = "Wormholes gain this much energy every tick. [Default: 1000]")
    public int wormholeEnergyPerTick = 1000;
    @Configure(category = "wormholes", comment = "Wormholes gain this much energy for each mob that passed through. [Default: 100000]")
    public int wormholeEntityPerTP = 10000;
    @Configure(category = "wormholes", comment = "Minimum number of ticks before you can use a wormhole after using one. [Default: 100]")
    public int wormholeReUseDelay = 100;
    @Configure(category = "wormholes", comment = "Chance per tick of a wormhole spawning randomly near a player. [Default: 0.00001]")
    public double randomWormholeChance = 0.00001;
    @Configure(category = "wormholes", comment = "Maximum distance from a player of a random wormhole spawn. [Default: 64]")
    public double randomWormholeDistance = 64;
    @Configure(category = "wormholes", comment = "Minimum between two random wormhole spawns. [Default: 128]")
    public double randomWormholeSpacing = 128;
    @Configure(category = "wormholes", comment = "Probability that a wormhole spawns where any entity teleports from. [Default: 0.01]")
    public double teleWormholeChanceNormal = 0.01;
    @Configure(category = "wormholes", comment = "Similar to teleWormholeChanceNormal, but for entities tagged with pokecube_legends:space_worm. [Default: 64]")
    public double teleWormholeChanceWorms = 0.75;
    @Configure(category = "wormholes", comment = "Wormholes will not link to these worlds.")
    public List<String> wormhole_destination_blacklist = Lists.newArrayList("pokecube:secret_base",
            "compactmachines:compact_world", "dimdungeons:build_dimension", "minecraft:the_end", "minecraft:the_nether");
    @Configure(category = "wormholes", comment = "Wormholes will not link to these worlds.")
    public List<String> wormhole_destination_weights = Lists.newArrayList("pokecube_legends:ultraspace->90",
            "pokecube_world:overworld->5", "minecraft:overworld->5", "pokecube_legends:distorted_world->5");

    private final LegendaryConditions conditions = new LegendaryConditions();

    private boolean conditionsReged = false;

    public final Set<String> PROTECTED_STRUCTURES = Sets.newHashSet();

    public final Map<String, List<PokedexEntry>> STRUCTURE_ENTRIES = Maps.newHashMap();
    public final Map<String, String> STRUCTURE_PERMS = Maps.newHashMap();

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

        WormholeEntity.maxWormholeEnergy = this.maxWormholeEnergy;
        WormholeEntity.wormholeEnergyPerTick = this.wormholeEnergyPerTick;
        WormholeEntity.wormholeEntityPerTP = this.wormholeEntityPerTP;
        WormholeEntity.wormholeReUseDelay = this.wormholeReUseDelay;

        WormholeSpawns.randomWormholeChance = this.randomWormholeChance;
        WormholeSpawns.randomWormholeDistance = this.randomWormholeDistance;
        WormholeSpawns.randomWormholeSpacing = this.randomWormholeSpacing;

        WormholeSpawns.teleWormholeChanceNormal = this.teleWormholeChanceNormal;
        WormholeSpawns.teleWormholeChanceWorms = this.teleWormholeChanceWorms;

        DynamaxRaid.RAID_DURATION = this.raidDuration;
        TerastalRaid.RAID_DURATION = this.raidDuration;
        
        WormholeEntity.WEIGHTED_DIM_MAP.clear();
        WormholeEntity.NO_HOLES.clear();

        this.wormhole_destination_blacklist.forEach(s -> WormholeEntity.NO_HOLES
                .add(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(s))));
        this.wormhole_destination_weights.forEach(s -> {
            if (!s.contains("->"))
            {
                PokecubeAPI.LOGGER.error("Formatting error for {}, it should be of form \"<dimid>-><weight>\"");
                return;
            }
            final String[] args = s.split("->");
            if (args.length != 2)
            {
                PokecubeAPI.LOGGER.error("Formatting error for {}, it should be of form \"<dimid>-><weight>\"");
                return;
            }
            final ResourceKey<Level> key = ResourceKey.create(Registry.DIMENSION_REGISTRY,
                    new ResourceLocation(args[0]));
            final Float value = Float.valueOf(args[1]);
            WormholeEntity.WEIGHTED_DIM_MAP.put(key, value);
        });

        final List<String> temples = Lists.newArrayList(
        //@formatter:off
            "pokecube_legends:castle_n",
            "pokecube_legends:celebi_temple",
            "pokecube_legends:elite_four",
            "pokecube_legends:groudon_temple",
            "pokecube_legends:ho-oh_temple",
            "pokecube_legends:keldeo_place",
            "pokecube_legends:kubfu_dojo",
            "pokecube_legends:kubfu_dark",
            "pokecube_legends:kubfu_water",
            "pokecube_legends:kyogre_temple",
            "pokecube_legends:legendary_tree",
            "pokecube_legends:lugia_tower",
            "pokecube_legends:mirage_pyramid",
            "pokecube_legends:nature_place",
            "pokecube_legends:necrozma_tower",
            "pokecube_legends:regice_temple->regice",
            "pokecube_legends:regidrago_temple->regidrago",
            "pokecube_legends:regieleki_temple->regieleki",
            "pokecube_legends:regigigas_temple->regigigas",
            "pokecube_legends:regirock_temple->regirock",
            "pokecube_legends:registeel_temple->registeel",
            "pokecube_legends:sky_pillar",
            "pokecube_legends:space_temple",
            "pokecube_legends:tapu_center",
            "pokecube_legends:xerneas_tree",
            "pokecube_legends:yveltal_ruins",
            "pokecube_legends:zacian_temple",
            "pokecube_legends:zamazenta_temple"
        //@formatter:on
        );

        temples.addAll(arceus_protected_structures);

        // Some hardcoded values here for now
        this.PROTECTED_STRUCTURES.clear();
        this.STRUCTURE_ENTRIES.clear();

        temples.forEach(s -> {
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

        if (this.ultraKeyRequireFuel == true)
            if (this.ultraKeyRequiredFuelAmount <= 1 || this.ultraKeyRequiredFuelAmount >= 30) this.ultraKeyRequiredFuelAmount = 5;

        if (this.mirrorCooldown <= 300) this.mirrorCooldown = 800;

        if (this.levelCreatePortal <= 39) this.levelCreatePortal = 20;
    }
}
