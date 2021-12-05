package pokecube.legends.worldgen.trees;

import java.util.OptionalInt;

import com.google.common.collect.ImmutableList;

import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration.TreeConfigurationBuilder;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.PineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.trees.treedecorators.LeavesStringOfPearlsDecorator;
import pokecube.legends.worldgen.trees.treedecorators.TrunkStringOfPearlsDecorator;

public class Trees
{
    private static final BeehiveDecorator BEEHIVE_0002 = new BeehiveDecorator(0.002F);

    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS = DeferredRegister
            .create(ForgeRegistries.TREE_DECORATOR_TYPES, Reference.ID);

    public static final RegistryObject<TreeDecoratorType<?>> LEAVES_STRING_OF_PEARLS = TREE_DECORATORS.register(
            "leaves_string_of_pearls_decorator", () -> new TreeDecoratorType<>(LeavesStringOfPearlsDecorator.CODEC));
    public static final RegistryObject<TreeDecoratorType<?>> TRUNK_STRING_OF_PEARLS = TREE_DECORATORS.register(
            "trunk_string_of_pearls_decorator", () -> new TreeDecoratorType<>(TrunkStringOfPearlsDecorator.CODEC));

    public static ConfiguredFeature<TreeConfiguration, ?> INVERTED_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> INVERTED_TREE_FANCY;
    public static ConfiguredFeature<TreeConfiguration, ?> TEMPORAL_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> MEGA_TEMPORAL_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> AGED_PINE_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> AGED_SPRUCE_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> MEGA_AGED_PINE_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> MEGA_AGED_SPRUCE_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> CORRUPTED_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> MIRAGE_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> DISTORTIC_TREE;

    public static PlacedFeature INVERTED_TREE_FEATURE;
    public static PlacedFeature INVERTED_TREE_FANCY_FEATURE;
    public static PlacedFeature TEMPORAL_TREE_FEATURE;
    public static PlacedFeature MEGA_TEMPORAL_TREE_FEATURE;
    public static PlacedFeature AGED_PINE_TREE_FEATURE;
    public static PlacedFeature AGED_SPRUCE_TREE_FEATURE;
    public static PlacedFeature MEGA_AGED_PINE_TREE_FEATURE;
    public static PlacedFeature MEGA_AGED_SPRUCE_TREE_FEATURE;
    public static PlacedFeature CORRUPTED_TREE_FEATURE;
    public static PlacedFeature MIRAGE_TREE_FEATURE;
    public static PlacedFeature DISTORTIC_TREE_FEATURE;

    public static final class States
    {
        public static final BlockState ULTRA_JUNGLE_GRASS = BlockInit.JUNGLE_GRASS.get().defaultBlockState();
        public static final BlockState ULTRA_AGED_GRASS = BlockInit.AGED_GRASS.get().defaultBlockState();
    }

    public static void init(final IEventBus bus)
    {
        TREE_DECORATORS.register(bus);
        // Register this as a low priority, so that the tree decorator exists
        // before we try to add it to the trees themseleves.
        bus.addGenericListener(Feature.class, EventPriority.LOWEST, Trees::registerConfigured);
    }

    public static TreeConfigurationBuilder getInvertedTree()
    {
        return new TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                BlockStateProvider.simple(BlockInit.INVERTED_LOG.get().defaultBlockState()),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forking, Giant, MegaJungle available
                new StraightTrunkPlacer(6, 4, 0),

                // This one is similar, but for the leaves
                BlockStateProvider.simple(BlockInit.INVERTED_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayersFeatureSize(1, 0, 1))
                .ignoreVines();
        //@formatter:on
    }

    public static TreeConfigurationBuilder getInvertedTreeFancy()
    {
        return new TreeConfigurationBuilder(
        //@formatter:off
            // This line specifies what is the base log, different block state providers
            // can allow for randomization in the log
            BlockStateProvider.simple(BlockInit.INVERTED_LOG.get().defaultBlockState()),

            // This is how the tree trunk works, there are also DarkOak, Fancy,
            // Forking, Giant, MegaJungle available
            new FancyTrunkPlacer(3, 11, 0),

            // This one is similar, but for the leaves
            BlockStateProvider.simple(BlockInit.INVERTED_LEAVES.get().defaultBlockState()),

            // This is how the leaves are arranged, this is the default for oak, there
            // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
            // more can also probably be coded if needed
            // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
            // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
            new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4),

            // I am not certain exactly how this works, but there is also a threeLayer feature
            // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
            // Different trees use a variety of the three values below, usually ranging from
            // 0 to 2, this example is from basic oak trees, but it can vary for different ones
            new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4)))
            .ignoreVines();
        //@formatter:on
    }

    public static TreeConfigurationBuilder getTemporalTree()
    {
        return new TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                BlockStateProvider.simple(BlockInit.TEMPORAL_LOG.get().defaultBlockState()),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forking, Giant, MegaJungle available
                new StraightTrunkPlacer(6, 8, 0),

                // This one is similar, but for the leaves
                BlockStateProvider.simple(BlockInit.TEMPORAL_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayersFeatureSize(2, 0, 2))
                .decorators(ImmutableList.of(TrunkStringOfPearlsDecorator.INSTANCE, LeavesStringOfPearlsDecorator.INSTANCE,
                    BEEHIVE_0002))
                .ignoreVines();
        //@formatter:on
    }

    public static TreeConfigurationBuilder getMegaTemporalTree()
    {
        return new TreeConfigurationBuilder(
        //@formatter:off
            // This line specifies what is the base log, different block state providers
            // can allow for randomization in the log
            BlockStateProvider.simple(BlockInit.TEMPORAL_LOG.get().defaultBlockState()),

            // This is how the tree trunk works, there are also DarkOak, Fancy,
            // Forking, Giant, MegaJungle available
            new MegaJungleTrunkPlacer(12, 4, 24),

            // This one is similar, but for the leaves
            BlockStateProvider.simple(BlockInit.TEMPORAL_LEAVES.get().defaultBlockState()),

            // This is how the leaves are arranged, this is the default for oak, there
            // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
            // more can also probably be coded if needed
            // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
            // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
            new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),

            // I am not certain exactly how this works, but there is also a threeLayer feature
            // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
            // Different trees use a variety of the three values below, usually ranging from
            // 0 to 2, this example is from basic oak trees, but it can vary for different ones
            new TwoLayersFeatureSize(1, 1, 2))
            .decorators(ImmutableList.of(TrunkStringOfPearlsDecorator.INSTANCE, LeavesStringOfPearlsDecorator.INSTANCE,
                BEEHIVE_0002, new AlterGroundDecorator(BlockStateProvider.simple(States.ULTRA_JUNGLE_GRASS))));
        //@formatter:on
    }

    public static TreeConfigurationBuilder getAgedPineTree()
    {
        return new TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                BlockStateProvider.simple(BlockInit.AGED_LOG.get().defaultBlockState()),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forking, Giant, MegaJungle available
                new ForkingTrunkPlacer(7, 5, 0),

                // This one is similar, but for the leaves
                BlockStateProvider.simple(BlockInit.AGED_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new PineFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), UniformInt.of(3, 5)),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayersFeatureSize(1, 0, 1))
                .ignoreVines();
        //@formatter:on
    }

    public static TreeConfigurationBuilder getAgedSpruceTree()
    {
        return new TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                BlockStateProvider.simple(BlockInit.AGED_LOG.get().defaultBlockState()),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forking, Giant, MegaJungle available
                new ForkingTrunkPlacer(8, 6, 0),

                // This one is similar, but for the leaves
                BlockStateProvider.simple(BlockInit.AGED_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new SpruceFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 2), UniformInt.of(1, 3)),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayersFeatureSize(1, 0, 1))
                .ignoreVines();
        //@formatter:on
    }

    public static TreeConfigurationBuilder getMegaAgedPineTree()
    {
        return new TreeConfigurationBuilder(
        //@formatter:off
            // This line specifies what is the base log, different block state providers
            // can allow for randomization in the log
            BlockStateProvider.simple(BlockInit.AGED_LOG.get().defaultBlockState()),

            // This is how the tree trunk works, there are also DarkOak, Fancy,
            // Forking, Giant, MegaJungle available
            new GiantTrunkPlacer(13, 2, 14),

            // This one is similar, but for the leaves
            BlockStateProvider.simple(BlockInit.AGED_LEAVES.get().defaultBlockState()),

            // This is how the leaves are arranged, this is the default for oak, there
            // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
            // more can also probably be coded if needed
            // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
            // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
            new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(3, 8)),

            // I am not certain exactly how this works, but there is also a threeLayer feature
            // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
            // Different trees use a variety of the three values below, usually ranging from
            // 0 to 2, this example is from basic oak trees, but it can vary for different ones
            new TwoLayersFeatureSize(1, 1, 2))
            .decorators(ImmutableList.of(BEEHIVE_0002, new AlterGroundDecorator(BlockStateProvider.simple(States.ULTRA_AGED_GRASS))))
            ;
        //@formatter:on
    }

    public static TreeConfigurationBuilder getMegaAgedSpruceTree()
    {
        return new TreeConfigurationBuilder(
        //@formatter:off
            // This line specifies what is the base log, different block state providers
            // can allow for randomization in the log
            BlockStateProvider.simple(BlockInit.AGED_LOG.get().defaultBlockState()),

            // This is how the tree trunk works, there are also DarkOak, Fancy,
            // Forking, Giant, MegaJungle available
            new GiantTrunkPlacer(13, 2, 14),

            // This one is similar, but for the leaves
            BlockStateProvider.simple(BlockInit.AGED_LEAVES.get().defaultBlockState()),

            // This is how the leaves are arranged, this is the default for oak, there
            // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
            // more can also probably be coded if needed
            // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
            // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
            new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(8, 13)),

            // I am not certain exactly how this works, but there is also a threeLayer feature
            // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
            // Different trees use a variety of the three values below, usually ranging from
            // 0 to 2, this example is from basic oak trees, but it can vary for different ones
            new TwoLayersFeatureSize(1, 1, 2))
            .decorators(ImmutableList.of(BEEHIVE_0002, new AlterGroundDecorator(BlockStateProvider.simple(States.ULTRA_AGED_GRASS))))
            ;
        //@formatter:on
    }

    public static TreeConfigurationBuilder getCorruptedTree()
    {
        return new TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                BlockStateProvider.simple(BlockInit.CORRUPTED_LOG.get().defaultBlockState()),

                //new BlobFoliagePlacer(FeatureSpread.fixed(2), FeatureSpread.fixed(0), 3),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forking, Giant, MegaJungle available
                new ForkingTrunkPlacer(6, 2, 3),

                // This one is similar, but for the leaves
                BlockStateProvider.simple(BlockInit.CORRUPTED_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new AcaciaFoliagePlacer(ConstantInt.of(3), ConstantInt.of(1)),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayersFeatureSize(1, 0, 2))
                .ignoreVines();
        //@formatter:on
    }

    public static TreeConfigurationBuilder getMirageTree()
    {
        return new TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                BlockStateProvider.simple(BlockInit.MIRAGE_LOG.get().defaultBlockState()),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forking, Giant, MegaJungle available
                new StraightTrunkPlacer(10, 5, 15),

                // This one is similar, but for the leaves
                BlockStateProvider.simple(BlockInit.MIRAGE_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new MegaJungleFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), 3),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayersFeatureSize(1, 1, 2))
                .ignoreVines();
        //@formatter:on
    }

    public static TreeConfigurationBuilder getDistorticTree()
    {
        return new TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                BlockStateProvider.simple(BlockInit.DISTORTIC_LOG.get().defaultBlockState()),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forking, Giant, MegaJungle available
                new StraightTrunkPlacer(14, 2, 10),

                // This one is similar, but for the leaves
                BlockStateProvider.simple(BlockInit.DISTORTIC_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new FancyFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0), 6),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayersFeatureSize(1, 0, 1))
                .ignoreVines();
        //@formatter:on
    }

    private static void registerPlacements()
    {
        Trees.INVERTED_TREE_FEATURE = PlacementUtils.register("pokecube_legends:inverted_tree",
                INVERTED_TREE.filteredByBlockSurvival(BlockInit.INVERTED_SAPLING.get()));
        Trees.INVERTED_TREE_FANCY_FEATURE = PlacementUtils.register("pokecube_legends:inverted_fancy_tree",
                INVERTED_TREE_FANCY.filteredByBlockSurvival(BlockInit.INVERTED_SAPLING.get()));
        Trees.TEMPORAL_TREE_FEATURE = PlacementUtils.register("pokecube_legends:temporal_tree",
                TEMPORAL_TREE.filteredByBlockSurvival(BlockInit.TEMPORAL_SAPLING.get()));
        Trees.MEGA_TEMPORAL_TREE_FEATURE = PlacementUtils.register("pokecube_legends:mega_temporal_tree",
                MEGA_TEMPORAL_TREE.filteredByBlockSurvival(BlockInit.TEMPORAL_SAPLING.get()));
        Trees.AGED_PINE_TREE_FEATURE = PlacementUtils.register("pokecube_legends:aged_pine_tree",
                AGED_PINE_TREE.filteredByBlockSurvival(BlockInit.AGED_SAPLING.get()));
        Trees.AGED_SPRUCE_TREE_FEATURE = PlacementUtils.register("pokecube_legends:aged_spruce_tree",
                AGED_SPRUCE_TREE.filteredByBlockSurvival(BlockInit.AGED_SAPLING.get()));
        Trees.MEGA_AGED_PINE_TREE_FEATURE = PlacementUtils.register("pokecube_legends:mega_aged_pine_tree",
                MEGA_AGED_PINE_TREE.filteredByBlockSurvival(BlockInit.AGED_SAPLING.get()));
        Trees.MEGA_AGED_SPRUCE_TREE_FEATURE = PlacementUtils.register("pokecube_legends:mega_aged_spruce_tree",
                MEGA_AGED_SPRUCE_TREE.filteredByBlockSurvival(BlockInit.AGED_SAPLING.get()));
        Trees.CORRUPTED_TREE_FEATURE = PlacementUtils.register("pokecube_legends:corrupted_tree",
                CORRUPTED_TREE.filteredByBlockSurvival(BlockInit.CORRUPTED_SAPLING.get()));
        Trees.MIRAGE_TREE_FEATURE = PlacementUtils.register("pokecube_legends:mirage_tree",
                MIRAGE_TREE.filteredByBlockSurvival(BlockInit.MIRAGE_SAPLING.get()));
        Trees.DISTORTIC_TREE_FEATURE = PlacementUtils.register("pokecube_legends:distortic_tree",
                DISTORTIC_TREE.filteredByBlockSurvival(BlockInit.DISTORTIC_SAPLING.get()));
    }

    private static void registerConfigured(final RegistryEvent.Register<Feature<?>> event)
    {
        Trees.INVERTED_TREE = FeatureUtils.register("pokecube_legends:inverted_tree",
                Feature.TREE.configured(Trees.getInvertedTree().decorators(ImmutableList.of(BEEHIVE_0002)).build()));
        Trees.INVERTED_TREE_FANCY = FeatureUtils.register("pokecube_legends:inverted_fancy_tree", Feature.TREE
                .configured(Trees.getInvertedTreeFancy().decorators(ImmutableList.of(BEEHIVE_0002)).build()));

        Trees.TEMPORAL_TREE = FeatureUtils.register("pokecube_legends:temporal_tree",
                Feature.TREE.configured(Trees.getTemporalTree().build()));
        Trees.MEGA_TEMPORAL_TREE = FeatureUtils.register("pokecube_legends:mega_temporal_tree",
                Feature.TREE.configured(Trees.getMegaTemporalTree().build()));

        Trees.AGED_PINE_TREE = FeatureUtils.register("pokecube_legends:aged_pine_tree",
                Feature.TREE.configured(Trees.getAgedPineTree().decorators(ImmutableList.of(BEEHIVE_0002)).build()));
        Trees.AGED_SPRUCE_TREE = FeatureUtils.register("pokecube_legends:aged_spruce_tree",
                Feature.TREE.configured(Trees.getAgedSpruceTree().decorators(ImmutableList.of(BEEHIVE_0002)).build()));
        Trees.MEGA_AGED_PINE_TREE = FeatureUtils.register("pokecube_legends:mega_aged_pine_tree",
                Feature.TREE.configured(Trees.getMegaAgedPineTree().build()));
        Trees.MEGA_AGED_SPRUCE_TREE = FeatureUtils.register("pokecube_legends:mega_aged_spruce_tree",
                Feature.TREE.configured(Trees.getMegaAgedSpruceTree().build()));

        Trees.CORRUPTED_TREE = FeatureUtils.register("pokecube_legends:corrupted_tree",
                Feature.TREE.configured(Trees.getCorruptedTree().build()));

        Trees.MIRAGE_TREE = FeatureUtils.register("pokecube_legends:mirage_tree",
                Feature.TREE.configured(Trees.getMirageTree().build()));

        Trees.DISTORTIC_TREE = FeatureUtils.register("pokecube_legends:distortic_tree",
                Feature.TREE.configured(Trees.getDistorticTree().build()));
    }
}
