package pokecube.legends.worldgen;

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.BasaltColumnsFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.Reference;
import pokecube.legends.worldgen.features.DistortedIslandsFeature;
import pokecube.legends.worldgen.features.DistorticStoneBouldersFeature;
import pokecube.legends.worldgen.features.DistorticVinesFeature;

public class WorldgenFeatures<FC extends FeatureConfiguration> extends ForgeRegistryEntry<Feature<?>> 
{
//    public static final DeferredRegister<SurfaceBuilder<?>> SURFACE_BUILDERS = DeferredRegister.create(
//            ForgeRegistries.SURFACE_BUILDERS, Reference.ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(
            ForgeRegistries.FEATURES, Reference.ID);

//    private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String p_65808_, F p_65809_) {
//       return Registry.register(Registry.FEATURE, p_65808_, p_65809_);
//    }

//    public static final RegistryObject<SurfaceBuilder<?>> BURNT_DESERT = WorldgenFeatures.SURFACE_BUILDERS.register("burnt_desert_builder",
//            () -> new BurntDesertSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
//    public static final RegistryObject<SurfaceBuilder<?>> MIRAGE_DESERT = WorldgenFeatures.SURFACE_BUILDERS.register("mirage_desert_builder",
//            () -> new MirageDesertSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
//    public static final RegistryObject<SurfaceBuilder<?>> BLINDING_DELTAS = WorldgenFeatures.SURFACE_BUILDERS.register("blinding_deltas_builder",
//            () -> new BlindingDeltasSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
//    public static final RegistryObject<SurfaceBuilder<?>> TAINTED_BARRENS = WorldgenFeatures.SURFACE_BUILDERS.register("tainted_barrens_builder",
//            () -> new TaintedBarrensSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));

//    public static final RegistryObject<Feature<?>> BASALT_COLUMNS = WorldgenFeatures.FEATURES.register("basalt_columns_feature",
//            () -> new BasaltColumnsFeature(ColumnFeatureConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> CRYS_SANDSTONE_BOULDERS = WorldgenFeatures.FEATURES.register("crystallized_sandstone_boulders_feature",
//            () -> new CrystallizedSandstoneBouldersFeature(ColumnFeatureConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> DEAD_CORAL_CLAW = WorldgenFeatures.FEATURES.register("dead_coral_claw_feature",
//            () -> new DeadCoralClawFeature(NoneFeatureConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> DEAD_CORAL_MUSHROOM = WorldgenFeatures.FEATURES.register("dead_coral_mushroom_feature",
//            () -> new DeadCoralMushroomFeature(NoneFeatureConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> DEAD_CORAL_TREE = WorldgenFeatures.FEATURES.register("dead_coral_tree_feature",
//            () -> new DeadCoralTreeFeature(NoneFeatureConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> DELTA = WorldgenFeatures.FEATURES.register("delta_feature",
//            () -> new DeltaFeature(DeltaFeatureConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> DESERT_ROCK = WorldgenFeatures.FEATURES.register("desert_rock_feature",
//            () -> new DesertRockFeature(BlockStateConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> DISK = WorldgenFeatures.FEATURES.register("disk_feature",
//            () -> new DiskFeature(DiskConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> DISK_BASE = WorldgenFeatures.FEATURES.register("disk_base_feature",
//            () -> new DiskBaseFeature(DiskConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> DISTORTED_ISLANDS = WorldgenFeatures.FEATURES.register("distorted_islands_feature",
            () -> new DistortedIslandsFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> DISTORTIC_STONE_BOULDERS = WorldgenFeatures.FEATURES.register("distortic_stone_boulders_feature",
            () -> new DistorticStoneBouldersFeature(ColumnFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<?>> DISTORTIC_VINES = WorldgenFeatures.FEATURES.register("distortic_vines_feature",
            () -> new DistorticVinesFeature(NoneFeatureConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> FORBIDDEN_TAIGA_VEGETATION = WorldgenFeatures.FEATURES.register("forsaken_taiga_vegetation_feature",
//            () -> new ForestVegetationFeature(BlockPileConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> LAKE = WorldgenFeatures.FEATURES.register("lake_feature",
//            () -> new LakeFeature(BlockStateConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> TEMPORAL_BAMBOO = WorldgenFeatures.FEATURES.register("temporal_bamboo_feature",
//            () -> new TemporalBambooFeature(ProbabilityFeatureConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> TAINTED_KELP= WorldgenFeatures.FEATURES.register("tainted_kelp_feature",
//            () -> new TaintedKelpFeature(NoneFeatureConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> TAINTED_SEAGRASS = WorldgenFeatures.FEATURES.register("tainted_seagrass_feature",
//            () -> new TaintedSeagrassFeature(ProbabilityFeatureConfiguration.CODEC));
//    public static final RegistryObject<Feature<?>> STRING_OF_PEARLS = WorldgenFeatures.FEATURES.register("string_of_pearls_feature",
//            () -> new StringOfPearlsFeature(NoneFeatureConfiguration.CODEC));
//    public static final Feature<ColumnFeatureConfiguration> DISTORTIC_STONE_BOULDERS2 = register("pokecube_legends:distortic_stone_boulders_feature", 
//    		new DistorticStoneBouldersFeature(ColumnFeatureConfiguration.CODEC));
//    
//    public static final PlacedFeature ROCK_DISTORTIC_STONE_PLACEMENT = PlacementUtils.register("pokecube_legends:rock_distortic_stone", 
//    		WorldgenFeatures.ROCK_DISTORTIC_STONE_FEATURE.placed(CountPlacement.of(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()));
//    public static final PlacedFeature RANDOM_DISTORTIC_STONE_BOULDERS_PLACEMENT = PlacementUtils.register("trees_flower_forest", 
//    		WorldgenFeatures.RANDOM_DISTORTIC_STONE_BOULDERS_FEATURE.placed(treePlacement(PlacementUtils.countExtra(6, 0.1F, 1))));
//    public static final PlacedFeature SMALL_DISTORTIC_STONE_BOULDERS_PLACEMENT = PlacementUtils.register("pokecube_legends:small_distortic_stone_boulders", 
//    		WorldgenFeatures.SMALL_DISTORTIC_STONE_BOULDERS_FEATURE.placed(CountOnEveryLayerPlacement.of(4), BiomeFilter.biome()));
//
//    public static final ConfiguredFeature<ColumnFeatureConfiguration, ?> SMALL_DISTORTIC_STONE_BOULDERS_FEATURE = FeatureUtils.register("pokecube_legends:small_distortic_stone_boulders", 
//    		WorldgenFeatures.DISTORTIC_STONE_BOULDERS2.configured(new ColumnFeatureConfiguration(ConstantInt.of(1), UniformInt.of(1, 4))));
//    public static final ConfiguredFeature<RandomFeatureConfiguration, ?> RANDOM_DISTORTIC_STONE_BOULDERS_FEATURE = 
//    		FeatureUtils.register("pokecube_legends:random_distortic_stone_boulders", Feature.RANDOM_SELECTOR.configured(
//    				new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(ROCK_DISTORTIC_STONE_PLACEMENT, 0.015F)), ROCK_DISTORTIC_STONE_PLACEMENT)));
//    public static final ConfiguredFeature<BlockStateConfiguration, ?> ROCK_DISTORTIC_STONE_FEATURE = FeatureUtils.register("pokecube_legends:rock_distortic_stone", 
//    		Feature.FOREST_ROCK.configured(new BlockStateConfiguration(BlockInit.DISTORTIC_STONE.get().defaultBlockState())));

    public static void init(final IEventBus bus)
    {
//        WorldgenFeatures.SURFACE_BUILDERS.register(bus);
        WorldgenFeatures.FEATURES.register(bus);
    }

//    public ConfiguredFeature<FC, ?> configured(FC p_65816_) {
//       return new ConfiguredFeature<>(this, p_65816_);
//    }
}
