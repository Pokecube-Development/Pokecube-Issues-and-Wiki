package pokecube.legends.worldgen;

import net.minecraft.world.level.levelgen.feature.BasaltColumnsFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.Reference;
import pokecube.legends.worldgen.features.AquamarineClusterFeature;
import pokecube.legends.worldgen.features.AquamarineCrystalFeature;
import pokecube.legends.worldgen.features.CrystallizedSandstoneBouldersFeature;
import pokecube.legends.worldgen.features.DeadCoralClawFeature;
import pokecube.legends.worldgen.features.DeadCoralMushroomFeature;
import pokecube.legends.worldgen.features.DeadCoralTreeFeature;
import pokecube.legends.worldgen.features.DistortedIslandsFeature;
import pokecube.legends.worldgen.features.DistorticStoneBouldersFeature;
import pokecube.legends.worldgen.features.DistorticVinesFeature;
import pokecube.legends.worldgen.features.LargeUnrefinedAquamarine;
import pokecube.legends.worldgen.features.MeteoriteSpikeFeature;
import pokecube.legends.worldgen.features.PollutedIcebergFeature;
import pokecube.legends.worldgen.features.PollutedSnowAndFreezeFeature;
import pokecube.legends.worldgen.features.RockFeature;
import pokecube.legends.worldgen.features.StringOfPearlsFeature;
import pokecube.legends.worldgen.features.SurfaceFossilFeature;
import pokecube.legends.worldgen.features.TaintedKelpFeature;
import pokecube.legends.worldgen.features.TaintedSeagrassFeature;
import pokecube.legends.worldgen.features.TemporalBambooFeature;
import pokecube.legends.worldgen.features.UltraspaceDeltaFeature;
import pokecube.legends.worldgen.features.UltraspaceVegetationFeature;

public class WorldgenFeatures
{
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(
            ForgeRegistries.FEATURES, Reference.ID);

    public static final RegistryObject<Feature<?>> AQUAMARINE_CLUSTER = WorldgenFeatures.FEATURES.register("aquamarine_cluster_feature",
            () -> new AquamarineClusterFeature(DripstoneClusterConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> AQUAMARINE_CRYSTAL = WorldgenFeatures.FEATURES.register("aquamarine_crystal_feature",
            () -> new AquamarineCrystalFeature(PointedDripstoneConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> BASALT_COLUMNS = WorldgenFeatures.FEATURES.register("basalt_columns_feature",
            () -> new BasaltColumnsFeature(ColumnFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> CRYS_SANDSTONE_BOULDERS = WorldgenFeatures.FEATURES.register("crystallized_sandstone_boulders_feature",
            () -> new CrystallizedSandstoneBouldersFeature(ColumnFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> DEAD_CORAL_CLAW = WorldgenFeatures.FEATURES.register("dead_coral_claw_feature",
            () -> new DeadCoralClawFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> DEAD_CORAL_MUSHROOM = WorldgenFeatures.FEATURES.register("dead_coral_mushroom_feature",
            () -> new DeadCoralMushroomFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> DEAD_CORAL_TREE = WorldgenFeatures.FEATURES.register("dead_coral_tree_feature",
            () -> new DeadCoralTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> ULTRASPACE_DELTA = WorldgenFeatures.FEATURES.register("delta_feature",
            () -> new UltraspaceDeltaFeature(DeltaFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> DISTORTED_ISLANDS = WorldgenFeatures.FEATURES.register("distorted_islands_feature",
            () -> new DistortedIslandsFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> DISTORTIC_STONE_BOULDERS = WorldgenFeatures.FEATURES.register("distortic_stone_boulders_feature",
            () -> new DistorticStoneBouldersFeature(ColumnFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> DISTORTIC_VINES = WorldgenFeatures.FEATURES.register("distortic_vines_feature",
            () -> new DistorticVinesFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> LARGE_UNREFINED_AQUAMARINE = WorldgenFeatures.FEATURES.register("large_unrefined_aquamarine_feature",
            () -> new LargeUnrefinedAquamarine(LargeDripstoneConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> METEORITE_SPIKES = WorldgenFeatures.FEATURES.register("meteorite_spike_feature",
            () -> new MeteoriteSpikeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> POLLUTED_ICEBERG = WorldgenFeatures.FEATURES.register("polluted_iceberg_feature",
            () -> new PollutedIcebergFeature(BlockStateConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> POLLUTED_SNOW_FREEZE = WorldgenFeatures.FEATURES.register("freeze_top_layer_polluted_feature",
            () -> new PollutedSnowAndFreezeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> ROCK = WorldgenFeatures.FEATURES.register("rock_feature",
            () -> new RockFeature(BlockStateConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> SURFACE_FOSSILS = WorldgenFeatures.FEATURES.register("surface_fossil_feature",
            () -> new SurfaceFossilFeature(FossilFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> TEMPORAL_BAMBOO = WorldgenFeatures.FEATURES.register("temporal_bamboo_feature",
            () -> new TemporalBambooFeature(ProbabilityFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> TAINTED_KELP= WorldgenFeatures.FEATURES.register("tainted_kelp_feature",
            () -> new TaintedKelpFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> TAINTED_SEAGRASS = WorldgenFeatures.FEATURES.register("tainted_seagrass_feature",
            () -> new TaintedSeagrassFeature(ProbabilityFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> STRING_OF_PEARLS = WorldgenFeatures.FEATURES.register("string_of_pearls_feature",
            () -> new StringOfPearlsFeature(NoneFeatureConfiguration.CODEC));
    
    public static final RegistryObject<Feature<NetherForestVegetationConfig>> ULTRASPACE_VEGETATION = WorldgenFeatures.FEATURES.register("ultraspace_vegetation_feature",
            () -> new UltraspaceVegetationFeature(NetherForestVegetationConfig.CODEC));

    public static void init(final IEventBus bus)
    {
        WorldgenFeatures.FEATURES.register(bus);
    }
}
