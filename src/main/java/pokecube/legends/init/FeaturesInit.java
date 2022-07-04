package pokecube.legends.init;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;
import pokecube.legends.worldgen.WorldgenFeatures;

public class FeaturesInit
{
    public static final String ID_ULTRA = Reference.ID + ":ultraspace";
    public static final String ID_DISTO = Reference.ID + ":distorted_world";

    private static final ResourceLocation IDLOC_ULTRA = new ResourceLocation(FeaturesInit.ID_ULTRA);
    private static final ResourceLocation IDLOC_DISTO = new ResourceLocation(FeaturesInit.ID_DISTO);

    // Dimensions
    public static final ResourceKey<Level> ULTRASPACE_KEY = ResourceKey.create(Registry.DIMENSION_REGISTRY,
            FeaturesInit.IDLOC_ULTRA);

    public static final ResourceKey<Level> DISTORTEDWORLD_KEY = ResourceKey.create(Registry.DIMENSION_REGISTRY,
            FeaturesInit.IDLOC_DISTO);

    // Biomes
    public static final ResourceKey<Biome> AQUAMARINE_CAVES = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "aquamarine_caves"));
    public static final ResourceKey<Biome> AZURE_BADLANDS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "azure_badlands"));
    public static final ResourceKey<Biome> BLINDING_DELTAS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "blinding_deltas"));
    public static final ResourceKey<Biome> BURNT_BEACH = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "burnt_beach"));
    public static final ResourceKey<Biome> CORRUPTED_CAVES = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "corrupted_caves"));
    public static final ResourceKey<Biome> CRYSTALLIZED_BEACH = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "crystallized_beach"));
    public static final ResourceKey<Biome> DEAD_OCEAN = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "dead_ocean"));
    public static final ResourceKey<Biome> DEEP_DEAD_OCEAN = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "deep_dead_ocean"));
    public static final ResourceKey<Biome> DEEP_FROZEN_DEAD_OCEAN = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "deep_frozen_dead_ocean"));
    public static final ResourceKey<Biome> DEAD_RIVER = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "dead_river"));
    public static final ResourceKey<Biome> DEEP_FROZEN_POLLUTED_OCEAN = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "deep_frozen_polluted_ocean"));
    public static final ResourceKey<Biome> DEEP_POLLUTED_OCEAN = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "deep_polluted_ocean"));
    public static final ResourceKey<Biome> DRIED_BLINDING_DELTAS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "dried_blinding_deltas"));
    public static final ResourceKey<Biome> DRIPSTONE_CAVES = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "dripstone_caves"));
    public static final ResourceKey<Biome> ERODED_AZURE_BADLANDS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "eroded_azure_badlands"));
    public static final ResourceKey<Biome> FORBIDDEN_GROVE = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "forbidden_grove"));
    public static final ResourceKey<Biome> FORBIDDEN_MEADOW = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "forbidden_highlands"));
    public static final ResourceKey<Biome> FORBIDDEN_TAIGA = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "forbidden_taiga"));
    public static final ResourceKey<Biome> FROZEN_DEAD_OCEAN = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "frozen_dead_ocean"));
    public static final ResourceKey<Biome> FROZEN_DEAD_RIVER = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "frozen_dead_river"));
    public static final ResourceKey<Biome> FROZEN_PEAKS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "frozen_peaks"));
    public static final ResourceKey<Biome> FROZEN_POLLUTED_OCEAN = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "frozen_polluted_ocean"));
    public static final ResourceKey<Biome> FROZEN_POLLUTED_RIVER = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "frozen_polluted_river"));
    public static final ResourceKey<Biome> FUNGAL_FLOWER_FOREST = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "fungal_flower_forest"));
    public static final ResourceKey<Biome> FUNGAL_FOREST = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "fungal_forest"));
    public static final ResourceKey<Biome> FUNGAL_PLAINS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "fungal_plains"));
    public static final ResourceKey<Biome> FUNGAL_SUNFLOWER_PLAINS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "fungal_sunflower_plains"));
    public static final ResourceKey<Biome> JAGGED_PEAKS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "jagged_peaks"));
    public static final ResourceKey<Biome> MAGMATIC_BLINDING_DELTAS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "magmatic_blinding_deltas"));
    public static final ResourceKey<Biome> METEORITE_SPIKES = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "meteorite_spikes"));
    public static final ResourceKey<Biome> MIRAGE_DESERT = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "mirage_desert"));
    public static final ResourceKey<Biome> OLD_GROWTH_FORBIDDEN_TAIGA = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "old_growth_forbidden_taiga"));
    public static final ResourceKey<Biome> POLLUTED_OCEAN = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "polluted_ocean"));
    public static final ResourceKey<Biome> POLLUTED_RIVER = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "polluted_river"));
    public static final ResourceKey<Biome> ROCKY_MIRAGE_DESERT = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "rocky_mirage_desert"));
    public static final ResourceKey<Biome> SHATTERED_BLINDING_DELTAS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "shattered_blinding_deltas"));
    public static final ResourceKey<Biome> SHATTERED_TAINTED_BARRENS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "shattered_tainted_barrens"));
    public static final ResourceKey<Biome> SNOWY_CRYSTALLIZED_BEACH = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "snowy_crystallized_beach"));
    public static final ResourceKey<Biome> SNOWY_FORBIDDEN_TAIGA = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "snowy_forbidden_taiga"));
    public static final ResourceKey<Biome> SNOWY_FUNGAL_PLAINS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "snowy_fungal_plains"));
    public static final ResourceKey<Biome> SNOWY_SLOPES = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "snowy_slopes"));
    public static final ResourceKey<Biome> SPARSE_TEMPORAL_JUNGLE = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "sparse_temporal_jungle"));
    public static final ResourceKey<Biome> TAINTED_BARRENS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "tainted_barrens"));
    public static final ResourceKey<Biome> TEMPORAL_BAMBOO_JUNGLE = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "temporal_bamboo_jungle"));
    public static final ResourceKey<Biome> TEMPORAL_JUNGLE = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "temporal_jungle"));
    public static final ResourceKey<Biome> ULTRA_STONY_PEAKS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "ultra_stony_peaks"));
    public static final ResourceKey<Biome> ULTRA_STONY_SHORE = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "ultra_stony_shore"));
    public static final ResourceKey<Biome> WINDSWEPT_FORBIDDEN_TAIGA = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "windswept_forbidden_taiga"));
    public static final ResourceKey<Biome> WINDSWEPT_TEMPORAL_JUNGLE = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "windswept_temporal_jungle"));
    public static final ResourceKey<Biome> WOODED_AZURE_BADLANDS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "wooded_azure_badlands"));
    public static final ResourceKey<Biome> VOLCANIC_BLINDING_DELTAS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "volcanic_blinding_deltas"));

    public static final ResourceKey<Biome> DISTORTED_LANDS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "distorted_lands"));
    public static final ResourceKey<Biome> SMALL_DISTORTED_ISLANDS = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(Reference.ID, "small_distorted_islands"));

    public static final class Configs
    {
        public static final Supplier<WeightedStateProvider> FORBIDDEN_VEGETATION_PROVIDER = ()->new WeightedStateProvider(
                SimpleWeightedRandomList.<BlockState>builder().add(PlantsInit.GOLDEN_GRASS.get().defaultBlockState(), 1)
                        .add(PlantsInit.GOLDEN_FERN.get().defaultBlockState(), 4));
        public static final Supplier<WeightedStateProvider> CORRUPTED_VEGETATION_PROVIDER = ()->new WeightedStateProvider(
                SimpleWeightedRandomList.<BlockState>builder()
                        .add(PlantsInit.CORRUPTED_GRASS.get().defaultBlockState(), 70)
                        .add(PlantsInit.TAINTED_ROOTS.get().defaultBlockState(), 45)
                        .add(PlantsInit.TAINTED_LILY_PAD.get().defaultBlockState(), 25)
                        .add(PlantsInit.PINK_TAINTED_LILY_PAD.get().defaultBlockState(), 10));

        public static final RegistryObject<ConfiguredFeature<?, ?>> FORBIDDEN_VEGETATION;
        public static final RegistryObject<ConfiguredFeature<?, ?>> FORBIDDEN_VEGETATION_BONEMEAL;

        public static final RegistryObject<ConfiguredFeature<?, ?>> TAINTED_VEGETATION;
        public static final RegistryObject<ConfiguredFeature<?, ?>> TAINTED_VEGETATION_BONEMEAL;

        public static final RegistryObject<ConfiguredFeature<?, ?>> SINGLE_PIECE_OF_DISTORTIC_GRASS;

        public static final RegistryObject<PlacedFeature> DISTORTIC_GRASS_BONEMEAL;

        private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_RUBY_BURIED_FEATURE;
        private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_RUBY_LARGE_FEATURE;
        private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_RUBY_SMALL_FEATURE;

        private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_SAPPHIRE_BURIED_FEATURE;
        private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_SAPPHIRE_LARGE_FEATURE;
        private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_SAPPHIRE_SMALL_FEATURE;

        private static final RegistryObject<PlacedFeature> ORE_RUBY_PLACEMENT;
        private static final RegistryObject<PlacedFeature> ORE_RUBY_BURIED_PLACEMENT;
        private static final RegistryObject<PlacedFeature> ORE_RUBY_LARGE_PLACEMENT;

        private static final RegistryObject<PlacedFeature> ORE_SAPPHIRE_PLACEMENT;
        private static final RegistryObject<PlacedFeature> ORE_SAPPHIRE_BURIED_PLACEMENT;
        private static final RegistryObject<PlacedFeature> ORE_SAPPHIRE_LARGE_PLACEMENT;

        static
        {
            // Ruby Ores

            ORE_RUBY_BURIED_FEATURE = PokecubeLegends.CONFIGURED_FEATURES.register("ruby_ore_buried",
                    () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getRubyList(), 8, 1.0f)));
            ORE_RUBY_LARGE_FEATURE = PokecubeLegends.CONFIGURED_FEATURES.register("ruby_ore_large",
                    () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getRubyList(), 12, 0.7f)));
            ORE_RUBY_SMALL_FEATURE = PokecubeLegends.CONFIGURED_FEATURES.register("ruby_ore",
                    () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getRubyList(), 4, 0.5f)));

            ORE_RUBY_PLACEMENT = PokecubeLegends.PLACED_FEATURES.register("ruby_ore",
                    () -> new PlacedFeature(ORE_RUBY_SMALL_FEATURE.getHolder().get(),
                            List.of(CountPlacement.of(7), InSquarePlacement.spread(), HeightRangePlacement
                                    .triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(100)),
                                    BiomeFilter.biome())));
            ORE_RUBY_BURIED_PLACEMENT = PokecubeLegends.PLACED_FEATURES.register("ruby_ore_buried",
                    () -> new PlacedFeature(ORE_RUBY_BURIED_FEATURE.getHolder().get(),
                            List.of(CountPlacement.of(4), InSquarePlacement.spread(), HeightRangePlacement
                                    .triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(100)),
                                    BiomeFilter.biome())));
            ORE_RUBY_LARGE_PLACEMENT = PokecubeLegends.PLACED_FEATURES.register("ruby_ore_large",
                    () -> new PlacedFeature(ORE_RUBY_LARGE_FEATURE.getHolder().get(),
                            List.of(CountPlacement.of(9), InSquarePlacement.spread(), HeightRangePlacement
                                    .triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(100)),
                                    BiomeFilter.biome())));

            // Sapphire Ores

            ORE_SAPPHIRE_BURIED_FEATURE = PokecubeLegends.CONFIGURED_FEATURES.register("sapphire_ore_buried",
                    () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getSapphireList(), 8, 1.0f)));
            ORE_SAPPHIRE_LARGE_FEATURE = PokecubeLegends.CONFIGURED_FEATURES.register("sapphire_ore_large",
                    () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getSapphireList(), 12, 0.7f)));
            ORE_SAPPHIRE_SMALL_FEATURE = PokecubeLegends.CONFIGURED_FEATURES.register("sapphire_ore",
                    () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getSapphireList(), 4, 0.5f)));

            ORE_SAPPHIRE_PLACEMENT = PokecubeLegends.PLACED_FEATURES.register("sapphire_ore",
                    () -> new PlacedFeature(ORE_SAPPHIRE_SMALL_FEATURE.getHolder().get(),
                            List.of(CountPlacement.of(7), InSquarePlacement.spread(), HeightRangePlacement
                                    .triangle(VerticalAnchor.aboveBottom(-64), VerticalAnchor.aboveBottom(90)),
                                    BiomeFilter.biome())));
            ORE_SAPPHIRE_BURIED_PLACEMENT = PokecubeLegends.PLACED_FEATURES.register("sapphire_ore_buried",
                    () -> new PlacedFeature(ORE_SAPPHIRE_BURIED_FEATURE.getHolder().get(),
                            List.of(CountPlacement.of(4), InSquarePlacement.spread(), HeightRangePlacement
                                    .triangle(VerticalAnchor.aboveBottom(-64), VerticalAnchor.aboveBottom(90)),
                                    BiomeFilter.biome())));
            ORE_SAPPHIRE_LARGE_PLACEMENT = PokecubeLegends.PLACED_FEATURES.register("sapphire_ore_large",
                    () -> new PlacedFeature(ORE_SAPPHIRE_LARGE_FEATURE.getHolder().get(),
                            List.of(CountPlacement.of(9), InSquarePlacement.spread(), HeightRangePlacement
                                    .triangle(VerticalAnchor.aboveBottom(-64), VerticalAnchor.aboveBottom(90)),
                                    BiomeFilter.biome())));

            // Vegetations
            FORBIDDEN_VEGETATION = PokecubeLegends.CONFIGURED_FEATURES.register("forbidden_vegetation",
                    () -> new ConfiguredFeature<>(WorldgenFeatures.ULTRASPACE_VEGETATION.get(),
                            new NetherForestVegetationConfig(FORBIDDEN_VEGETATION_PROVIDER.get(), 8, 4)));

            FORBIDDEN_VEGETATION_BONEMEAL = PokecubeLegends.CONFIGURED_FEATURES.register(
                    "forbidden_vegetation_bonemeal",
                    () -> new ConfiguredFeature<>(WorldgenFeatures.ULTRASPACE_VEGETATION.get(),
                            new NetherForestVegetationConfig(FORBIDDEN_VEGETATION_PROVIDER.get(), 3, 1)));
            TAINTED_VEGETATION = PokecubeLegends.CONFIGURED_FEATURES.register("tainted_barrens_vegetation",
                    () -> new ConfiguredFeature<>(WorldgenFeatures.ULTRASPACE_VEGETATION.get(),
                            new NetherForestVegetationConfig(CORRUPTED_VEGETATION_PROVIDER.get(), 8, 4)));
            TAINTED_VEGETATION_BONEMEAL = PokecubeLegends.CONFIGURED_FEATURES.register(
                    "tainted_barrens_vegetation_bonemeal",
                    () -> new ConfiguredFeature<>(WorldgenFeatures.ULTRASPACE_VEGETATION.get(),
                            new NetherForestVegetationConfig(CORRUPTED_VEGETATION_PROVIDER.get(), 3, 1)));
            SINGLE_PIECE_OF_DISTORTIC_GRASS = PokecubeLegends.CONFIGURED_FEATURES.register(
                    "single_piece_of_distortic_grass",
                    () -> new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(
                            BlockStateProvider.simple(PlantsInit.DISTORTIC_GRASS.get().defaultBlockState()))));

            DISTORTIC_GRASS_BONEMEAL = PokecubeLegends.PLACED_FEATURES.register("distortic_grass_bonemeal",
                    () -> new PlacedFeature(SINGLE_PIECE_OF_DISTORTIC_GRASS.getHolder().get(),
                            List.of(PlacementUtils.isEmpty())));

        }

        private static final Predicate<ResourceKey<Biome>> make_ores_check = k -> PokecubeLegends.config.generateOres;

        private static void onBiomeLoading(BiomeLoadingEvent event)
        {
            ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, event.getName());
            if (make_ores_check.test(key))
            {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                        ORE_RUBY_PLACEMENT.getHolder().get());
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                        ORE_RUBY_BURIED_PLACEMENT.getHolder().get());
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                        ORE_RUBY_LARGE_PLACEMENT.getHolder().get());

                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                        ORE_SAPPHIRE_PLACEMENT.getHolder().get());
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                        ORE_SAPPHIRE_BURIED_PLACEMENT.getHolder().get());
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                        ORE_SAPPHIRE_LARGE_PLACEMENT.getHolder().get());
            }
        }

        public static void init()
        {}
    }

    private static final List<OreConfiguration.TargetBlockState> getRubyList()
    {
        return List.of(
                OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES,
                        BlockInit.RUBY_ORE.get().defaultBlockState()),
                OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES,
                        BlockInit.DEEPSLATE_RUBY_ORE.get().defaultBlockState()));
    }

    private static final List<OreConfiguration.TargetBlockState> getSapphireList()
    {
        return List.of(
                OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES,
                        BlockInit.SAPPHIRE_ORE.get().defaultBlockState()),
                OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES,
                        BlockInit.DEEPSLATE_SAPPHIRE_ORE.get().defaultBlockState()));
    }

    public static void init(IEventBus bus)
    {
        MinecraftForge.EVENT_BUS.addListener(Configs::onBiomeLoading);
        Configs.init();
    }

    public class PlantPlacements
    {
        public static final RegistryObject<PlacedFeature> PATCH_GOLDEN_GRASS = PokecubeLegends.PLACED_FEATURES.register(
                "forbidden_bonemeal",
                () -> new PlacedFeature(FeaturesInit.Configs.FORBIDDEN_VEGETATION_BONEMEAL.getHolder().get(),
                        List.of(PlacementUtils.isEmpty())));

        public static final RegistryObject<PlacedFeature> PATCH_CORRUPTED_GRASS = PokecubeLegends.PLACED_FEATURES
                .register("tainted_barrens_bonemeal",
                        () -> new PlacedFeature(FeaturesInit.Configs.TAINTED_VEGETATION_BONEMEAL.getHolder().get(),
                                List.of(PlacementUtils.isEmpty())));
    }
}
