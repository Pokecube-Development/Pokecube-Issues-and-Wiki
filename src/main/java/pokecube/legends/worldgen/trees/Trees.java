package pokecube.legends.worldgen.trees;

import java.util.OptionalInt;

import com.google.common.collect.ImmutableList;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Features;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import pokecube.legends.init.BlockInit;

public class Trees
{
    public static ConfiguredFeature<TreeConfiguration, ?> INVERTED_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> INVERTED_TREE_FANCY;
    public static ConfiguredFeature<TreeConfiguration, ?> TEMPORAL_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> MEGA_TEMPORAL_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> AGED_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> MEGA_AGED_PINE_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> MEGA_AGED_SPRUCE_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> CORRUPTED_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> MIRAGE_TREE;
    public static ConfiguredFeature<TreeConfiguration, ?> DISTORTIC_TREE;

    public static final class States
    {
        public static final BlockState ULTRA_JUNGLE_GRASS = BlockInit.JUNGLE_GRASS.get().defaultBlockState();
        public static final BlockState ULTRA_AGED_GRASS   = BlockInit.AGED_GRASS.get().defaultBlockState();
        // static {
        // ULTRA_JUNGLE_GRASS =
        // BlockInit.ULTRA_JUNGLE_GRASS.get().defaultBlockState();
        // ULTRA_AGED_GRASS =
        // BlockInit.ULTRA_AGED_GRASS.get().defaultBlockState();
        // }
    }

    public static TreeConfiguration getInvertedTree()
    {
        return new TreeConfiguration.TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleStateProvider(BlockInit.INVERTED_LOG.get().defaultBlockState()),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(6, 4, 0),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.INVERTED_LEAVES.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.INVERTED_SAPLING.get().defaultBlockState()),

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
                .ignoreVines()
                .build();
        //@formatter:on
    }

    public static TreeConfiguration getInvertedTreeFancy()
    {
        return new TreeConfiguration.TreeConfigurationBuilder(
        //@formatter:off
            // This line specifies what is the base log, different block state providers
            // can allow for randomization in the log
            new SimpleStateProvider(BlockInit.INVERTED_LOG.get().defaultBlockState()),

            // This is how the tree trunk works, there are also DarkOak, Fancy,
            // Forky, Giant, MegaJungle available
            new FancyTrunkPlacer(3, 11, 0),

            // This one is similar, but for the leaves
            new SimpleStateProvider(BlockInit.INVERTED_LEAVES.get().defaultBlockState()),

            // This one is similar, but for the leaves
            new SimpleStateProvider(BlockInit.INVERTED_SAPLING.get().defaultBlockState()),

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
            .ignoreVines()
            .build();
        //@formatter:on
    }

    public static TreeConfiguration getTemporalTree()
    {
        return new TreeConfiguration.TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleStateProvider(BlockInit.TEMPORAL_LOG.get().defaultBlockState()),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(8, 8, 0),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.TEMPORAL_LEAVES.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.TEMPORAL_SAPLING.get().defaultBlockState()),

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
                .decorators(ImmutableList.of(TrunkVineDecorator.INSTANCE, LeaveVineDecorator.INSTANCE,
                    Features.Decorators.BEEHIVE_0002))
                .ignoreVines()
                .build();
        //@formatter:on
    }

    public static TreeConfiguration getMegaTemporalTree()
    {
        return new TreeConfiguration.TreeConfigurationBuilder(
        //@formatter:off
            // This line specifies what is the base log, different block state providers
            // can allow for randomization in the log
            new SimpleStateProvider(BlockInit.TEMPORAL_LOG.get().defaultBlockState()),

            // This is how the tree trunk works, there are also DarkOak, Fancy,
            // Forky, Giant, MegaJungle available
            new MegaJungleTrunkPlacer(12, 4, 24),

            // This one is similar, but for the leaves
            new SimpleStateProvider(BlockInit.TEMPORAL_LEAVES.get().defaultBlockState()),

            // This one is similar, but for the leaves
            new SimpleStateProvider(BlockInit.TEMPORAL_SAPLING.get().defaultBlockState()),

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
            .decorators(ImmutableList.of(TrunkVineDecorator.INSTANCE, LeaveVineDecorator.INSTANCE,
                Features.Decorators.BEEHIVE_0002, new AlterGroundDecorator(new SimpleStateProvider(States.ULTRA_JUNGLE_GRASS))))
            .build();
        //@formatter:on
    }

    public static TreeConfiguration getAgedTree()
    {
        return new TreeConfiguration.TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleStateProvider(BlockInit.AGED_LOG.get().defaultBlockState()),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(8, 6, 0),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.AGED_LEAVES.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.AGED_SAPLING.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new SpruceFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 2),
                    UniformInt.of(1, 3)),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayersFeatureSize(1, 0, 1))
                .ignoreVines()
                .build();
        //@formatter:on
    }

    public static TreeConfiguration getMegaAgedPineTree()
    {
        return new TreeConfiguration.TreeConfigurationBuilder(
        //@formatter:off
            // This line specifies what is the base log, different block state providers
            // can allow for randomization in the log
            new SimpleStateProvider(BlockInit.AGED_LOG.get().defaultBlockState()),

            // This is how the tree trunk works, there are also DarkOak, Fancy,
            // Forky, Giant, MegaJungle available
            new GiantTrunkPlacer(13, 2, 14),

            // This one is similar, but for the leaves
            new SimpleStateProvider(BlockInit.AGED_LEAVES.get().defaultBlockState()),

            // This one is similar, but for the leaves
            new SimpleStateProvider(BlockInit.AGED_SAPLING.get().defaultBlockState()),

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
            .decorators(ImmutableList.of(Features.Decorators.BEEHIVE_0002, new AlterGroundDecorator(new SimpleStateProvider(States.ULTRA_AGED_GRASS))))
            .build();
        //@formatter:on
    }

    public static TreeConfiguration getMegaAgedSpruceTree()
    {
        return new TreeConfiguration.TreeConfigurationBuilder(
        //@formatter:off
            // This line specifies what is the base log, different block state providers
            // can allow for randomization in the log
            new SimpleStateProvider(BlockInit.AGED_LOG.get().defaultBlockState()),

            // This is how the tree trunk works, there are also DarkOak, Fancy,
            // Forky, Giant, MegaJungle available
            new GiantTrunkPlacer(13, 2, 14),

            // This one is similar, but for the leaves
            new SimpleStateProvider(BlockInit.AGED_LEAVES.get().defaultBlockState()),

            // This one is similar, but for the leaves
            new SimpleStateProvider(BlockInit.AGED_SAPLING.get().defaultBlockState()),

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
            .decorators(ImmutableList.of(Features.Decorators.BEEHIVE_0002, new AlterGroundDecorator(new SimpleStateProvider(States.ULTRA_AGED_GRASS))))
            .build();
        //@formatter:on
    }

    public static TreeConfiguration getCorruptedTree()
    {
        return new TreeConfiguration.TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleStateProvider(BlockInit.CORRUPTED_LOG.get().defaultBlockState()),

                //new BlobFoliagePlacer(FeatureSpread.fixed(2), FeatureSpread.fixed(0), 3),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new FancyTrunkPlacer(10, 6, 0),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.CORRUPTED_LEAVES.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.CORRUPTED_SAPLING.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new AcaciaFoliagePlacer(ConstantInt.of(1), ConstantInt.of(3)),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayersFeatureSize(1, 0, 2))
                .ignoreVines()
                .build();
        //@formatter:on
    }

    public static TreeConfiguration getMirageTree()
    {
        return new TreeConfiguration.TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleStateProvider(BlockInit.MIRAGE_LOG.get().defaultBlockState()),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(15, 7, 10),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.MIRAGE_LEAVES.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.MIRAGE_SAPLING.get().defaultBlockState()),

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
                .ignoreVines()
                .build();
        //@formatter:on
    }

    public static TreeConfiguration getDistorticTree()
    {
        return new TreeConfiguration.TreeConfigurationBuilder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleStateProvider(BlockInit.DISTORTIC_LOG.get().defaultBlockState()),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(13, 10, 0),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.DISTORTIC_LEAVES.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleStateProvider(BlockInit.DISTORTIC_SAPLING.get().defaultBlockState()),

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
                .ignoreVines()
                .build();
        //@formatter:on
    }

    public static void register()
    {
        Trees.INVERTED_TREE = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:inverted_tree", Trees.INVERTED_TREE = Feature.TREE.configured(Trees.getInvertedTree()
                        .withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_0002))));
        Trees.INVERTED_TREE_FANCY = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:inverted_fancy_tree", Trees.INVERTED_TREE_FANCY = Feature.TREE.configured(Trees
                        .getInvertedTreeFancy().withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_0002))));

        Trees.TEMPORAL_TREE = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:temporal_tree", Trees.TEMPORAL_TREE = Feature.TREE.configured(Trees
                        .getTemporalTree()));
        Trees.MEGA_TEMPORAL_TREE = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:mega_temporal_tree", Trees.MEGA_TEMPORAL_TREE = Feature.TREE.configured(Trees
                        .getMegaTemporalTree()));

        Trees.AGED_TREE = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:aged_tree", Trees.AGED_TREE = Feature.TREE.configured(Trees.getAgedTree()
                        .withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_0002))));
        Trees.MEGA_AGED_PINE_TREE = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:mega_aged_pine_tree", Trees.MEGA_AGED_PINE_TREE = Feature.TREE.configured(Trees
                        .getMegaAgedPineTree()));
        Trees.MEGA_AGED_SPRUCE_TREE = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:mega_aged_spruce_tree", Trees.MEGA_AGED_SPRUCE_TREE = Feature.TREE.configured(Trees
                        .getMegaAgedSpruceTree()));

        Trees.CORRUPTED_TREE = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:corrupted_tree", Trees.CORRUPTED_TREE = Feature.TREE.configured(Trees
                        .getCorruptedTree()));

        Trees.MIRAGE_TREE = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:mirage_tree", Trees.MIRAGE_TREE = Feature.TREE.configured(Trees.getMirageTree()));

        Trees.DISTORTIC_TREE = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:distortic_tree", Trees.DISTORTIC_TREE = Feature.TREE.configured(Trees
                        .getDistorticTree()));
    }
}
