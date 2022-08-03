package pokecube.world.gen.features;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration.TreeConfigurationBuilder;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.legends.init.BlockInit;
import pokecube.world.PokecubeWorld;
import thut.api.terrain.BiomeDatabase;

import static pokecube.legends.worldgen.trees.Trees.TREE_FEATURES;

public class FeaturesInit
{
    public static void init(IEventBus bus)
    {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, FeaturesInit::onBiomeLoading);
    }

    final static List<OreConfiguration.TargetBlockState> getOres()
    {
        return List.of(
                OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES,
                        PokecubeItems.FOSSIL_ORE.get().defaultBlockState()),
                OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES,
                        PokecubeItems.DEEPSLATE_FOSSIL_ORE.get().defaultBlockState()));
    };

    private static final BeehiveDecorator BEEHIVE_0002 = new BeehiveDecorator(0.002F);
    private static final BeehiveDecorator BEEHIVE_002 = new BeehiveDecorator(0.02F);
    private static final BeehiveDecorator BEEHIVE_005 = new BeehiveDecorator(0.05F);

    private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_FOSSIL_SMALL_FEATURE;
    private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_FOSSIL_LARGE_FEATURE;
    private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_FOSSIL_BURIED_FEATURE;

    public static final RegistryObject<PlacedFeature> PLACED_SMALL_FOSSIL;
    public static final RegistryObject<PlacedFeature> PLACED_LARGE_FOSSIL;
    public static final RegistryObject<PlacedFeature> PLACED_BURIED_FOSSIL;

    public static RegistryObject<ConfiguredFeature<?, ?>> TREE_LEPPA_FEATURE;
    public static final RegistryObject<PlacedFeature> PLACED_TREE_LEPPA;

    public static final BlockPredicate BERRY_TREE_PREDICATE = BlockPredicate.matchesTag(BlockTags.DIRT, new BlockPos(0, -1, 0));
    public static final List<PlacementModifier> BERRY_TREE_FILTER_DECORATOR = List.of(BlockPredicateFilter.forPredicate(BERRY_TREE_PREDICATE));

    public static List<PlacementModifier> treePlacement(PlacementModifier modifier) {
        PokecubeCore.LOGGER.info("Generating Berry Trees Placement");
        return treePlacementBase(modifier).add(BlockPredicateFilter.forPredicate(BERRY_TREE_PREDICATE)).build();
    }

    static
    {
        ORE_FOSSIL_SMALL_FEATURE = PokecubeWorld.CONFIGURED_FEATURES.register("fossil_ore",
                () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getOres(), 4, 0.5f)));
        ORE_FOSSIL_LARGE_FEATURE = PokecubeWorld.CONFIGURED_FEATURES.register("fossil_ore_large",
                () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getOres(), 12, 0.7f)));
        ORE_FOSSIL_BURIED_FEATURE = PokecubeWorld.CONFIGURED_FEATURES.register("fossil_ore_buried",
                () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(getOres(), 8, 1.0f)));

        PLACED_SMALL_FOSSIL = PokecubeWorld.PLACED_FEATURES.register("fossil_ore",
                () -> new PlacedFeature(ORE_FOSSIL_SMALL_FEATURE.getHolder().get(),
                        List.of(CountPlacement.of(5), InSquarePlacement.spread(), HeightRangePlacement
                                .triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(380)),
                                BiomeFilter.biome())));
        PLACED_LARGE_FOSSIL = PokecubeWorld.PLACED_FEATURES.register("fossil_ore_large",
                () -> new PlacedFeature(ORE_FOSSIL_LARGE_FEATURE.getHolder().get(),
                        List.of(RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), HeightRangePlacement
                                .triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(380)),
                                BiomeFilter.biome())));
        PLACED_BURIED_FOSSIL = PokecubeWorld.PLACED_FEATURES.register("fossil_ore_buried",
                () -> new PlacedFeature(ORE_FOSSIL_BURIED_FEATURE.getHolder().get(),
                        List.of(CountPlacement.of(3), InSquarePlacement.spread(), HeightRangePlacement
                                .triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(380)),
                                BiomeFilter.biome())));

        TREE_LEPPA_FEATURE = TREE_FEATURES.register("leppa_tree",  () -> new ConfiguredFeature<>(Feature.TREE, getLeppaTree().build()));
        PLACED_TREE_LEPPA = PokecubeWorld.PLACED_FEATURES.register("tree_leppa",
                () -> new PlacedFeature(TREE_LEPPA_FEATURE.getHolder().get(),
                        treePlacement(PlacementUtils.countExtra(1, 0.1F, 1))));
//        PLACED_TREE_LEPPA = PlacementUtils.register("tree_leppa",
//                TREE_LEPPA_FEATURE.getHolder().get(), BERRY_TREE_FILTER_DECORATOR);
    }


    private static ImmutableList.Builder<PlacementModifier> treePlacementBase(PlacementModifier modifier) {
        return ImmutableList.<PlacementModifier>builder().add(modifier).add(InSquarePlacement.spread()).add(VegetationPlacements.TREE_THRESHOLD).add(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR).add(BiomeFilter.biome());
    }

    public static TreeConfigurationBuilder getLeppaTree()
    {
        PokecubeCore.LOGGER.info("Generating Berry Blocks");
        return new TreeConfigurationBuilder(BlockStateProvider.simple(BlockInit.INVERTED_LOG.get().defaultBlockState()),
                new StraightTrunkPlacer(6, 4, 0),
                BlockStateProvider.simple(BlockInit.INVERTED_LEAVES.get().defaultBlockState()),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1))
                .ignoreVines().decorators(ImmutableList.of(BEEHIVE_005));
    }

    private static final Predicate<ResourceKey<Biome>> ores_biome_check = k -> PokecubeCore.getConfig().generateFossils
            && (BiomeDatabase.contains(k, "mesa") || BiomeDatabase.contains(k, "ocean")
                    || BiomeDatabase.contains(k, "river") || BiomeDatabase.contains(k, "sandy"));

    private static final Predicate<ResourceKey<Biome>> trees_biome_check = k -> PokecubeCore.getConfig().generateFossils
            && (Biomes.FLOWER_FOREST.equals(true));

    public static void addLeppaTrees(BiomeGenerationSettings.Builder builder) {
        PokecubeCore.LOGGER.info("Generating Berry Trees");
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, TREES_LEPPA);
    }

    public static final Holder<ConfiguredFeature<RandomFeatureConfiguration, ?>> TREES_LEPPA_FEATURE = FeatureUtils.register("trees_leppa",
            Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(PLACED_TREE_LEPPA.getHolder().get(), 0.8F)),
                    TreePlacements.OAK_CHECKED));
    public static final Holder<PlacedFeature> TREES_LEPPA = PlacementUtils.register("trees_leppa", TREES_LEPPA_FEATURE,
            VegetationPlacements.treePlacement(PlacementUtils.countExtra(1, 0.1F, 1)));

    private static void onBiomeLoading(BiomeLoadingEvent event)
    {
        ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, event.getName());
        if (ores_biome_check.test(key))
        {
            event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                    PLACED_SMALL_FOSSIL.getHolder().get());
            event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                    PLACED_LARGE_FOSSIL.getHolder().get());
            event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES,
                    PLACED_BURIED_FOSSIL.getHolder().get());
        }
        if (trees_biome_check.test(key))
        {
            event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                    PLACED_TREE_LEPPA.getHolder().get());
            addLeppaTrees(new BiomeGenerationSettings.Builder());
        }
    }

}
