package pokecube.legends.worldgen.trees;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.foliageplacer.*;
import net.minecraft.world.gen.treedecorator.AlterGroundTreeDecorator;
import net.minecraft.world.gen.treedecorator.CocoaTreeDecorator;
import net.minecraft.world.gen.treedecorator.LeaveVineTreeDecorator;
import net.minecraft.world.gen.treedecorator.TrunkVineTreeDecorator;
import net.minecraft.world.gen.trunkplacer.FancyTrunkPlacer;
import net.minecraft.world.gen.trunkplacer.MegaJungleTrunkPlacer;
import net.minecraft.world.gen.trunkplacer.StraightTrunkPlacer;
import pokecube.legends.init.BlockInit;

import java.util.OptionalInt;

public class Trees
{
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> INVERTED_TREE;
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> INVERTED_TREE_FANCY;
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> TEMPORAL_TREE;
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> MEGA_TEMPORAL_TREE;
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> AGED_TREE;
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> CORRUPTED_TREE;
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> MIRAGE_TREE;
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> DISTORTIC_TREE;

    public static final class States {
        public static final BlockState ULTRA_JUNGLE_GRASS;
        static {
            ULTRA_JUNGLE_GRASS = BlockInit.ULTRA_JUNGLE_GRASS.get().defaultBlockState();
        }
    }

    public static BaseTreeFeatureConfig getInvertedTree()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleBlockStateProvider(BlockInit.INVERTED_LOG.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleBlockStateProvider(BlockInit.INVERTED_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new BlobFoliagePlacer(FeatureSpread.fixed(2), FeatureSpread.fixed(0), 3),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(6, 4, 0),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(1, 0, 1))
                .ignoreVines()
                .build();
        //@formatter:on
    }

    public static BaseTreeFeatureConfig getInvertedTreeFancy()
    {
        return new BaseTreeFeatureConfig.Builder(
            //@formatter:off
            // This line specifies what is the base log, different block state providers
            // can allow for randomization in the log
            new SimpleBlockStateProvider(BlockInit.INVERTED_LOG.get().defaultBlockState()),

            // This one is similar, but for the leaves
            new SimpleBlockStateProvider(BlockInit.INVERTED_LEAVES.get().defaultBlockState()),

            // This is how the leaves are arranged, this is the default for oak, there
            // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
            // more can also probably be coded if needed
            // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
            // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
            new FancyFoliagePlacer(FeatureSpread.fixed(2), FeatureSpread.fixed(4), 4),

            // This is how the tree trunk works, there are also DarkOak, Fancy,
            // Forky, Giant, MegaJungle available
            new FancyTrunkPlacer(3, 11, 0),

            // I am not certain exactly how this works, but there is also a threeLayer feature
            // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
            // Different trees use a variety of the three values below, usually ranging from
            // 0 to 2, this example is from basic oak trees, but it can vary for different ones
            new TwoLayerFeature(0, 0, 0, OptionalInt.of(4)))
            .ignoreVines()
            .build();
        //@formatter:on
    }
    
    public static BaseTreeFeatureConfig getTemporalTree()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleBlockStateProvider(BlockInit.TEMPORAL_LOG.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleBlockStateProvider(BlockInit.TEMPORAL_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new JungleFoliagePlacer(FeatureSpread.fixed(2), FeatureSpread.fixed(0), 3),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(8, 8, 0),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(2, 0, 2))
                .decorators(ImmutableList.of(TrunkVineTreeDecorator.INSTANCE, LeaveVineTreeDecorator.INSTANCE))
                .ignoreVines()
                .build();
        //@formatter:on
    }

    public static BaseTreeFeatureConfig getMegaTemporalTree()
    {
        return new BaseTreeFeatureConfig.Builder(
            //@formatter:off
            // This line specifies what is the base log, different block state providers
            // can allow for randomization in the log
            new SimpleBlockStateProvider(BlockInit.TEMPORAL_LOG.get().defaultBlockState()),

            // This one is similar, but for the leaves
            new SimpleBlockStateProvider(BlockInit.TEMPORAL_LEAVES.get().defaultBlockState()),

            // This is how the leaves are arranged, this is the default for oak, there
            // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
            // more can also probably be coded if needed
            // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
            // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
            new JungleFoliagePlacer(FeatureSpread.fixed(2), FeatureSpread.fixed(0), 3),

            // This is how the tree trunk works, there are also DarkOak, Fancy,
            // Forky, Giant, MegaJungle available
            new MegaJungleTrunkPlacer(12, 4, 24),

            // I am not certain exactly how this works, but there is also a threeLayer feature
            // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
            // Different trees use a variety of the three values below, usually ranging from
            // 0 to 2, this example is from basic oak trees, but it can vary for different ones
            new TwoLayerFeature(1, 1, 2))
            .decorators(ImmutableList.of(TrunkVineTreeDecorator.INSTANCE, LeaveVineTreeDecorator.INSTANCE,
                new AlterGroundTreeDecorator(new SimpleBlockStateProvider(States.ULTRA_JUNGLE_GRASS))))
            .build();
        //@formatter:on
    }
    
    public static BaseTreeFeatureConfig getAgedTree()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleBlockStateProvider(BlockInit.AGED_LOG.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleBlockStateProvider(BlockInit.AGED_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new SpruceFoliagePlacer(FeatureSpread.fixed(2), FeatureSpread.fixed(0), FeatureSpread.fixed(3)),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(6, 3, 0),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(1, 0, 1))
                .ignoreVines()
                .build();
        //@formatter:on
    }

    public static BaseTreeFeatureConfig getCorruptedTree()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleBlockStateProvider(BlockInit.CORRUPTED_LOG.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleBlockStateProvider(BlockInit.CORRUPTED_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new AcaciaFoliagePlacer(FeatureSpread.fixed(3), FeatureSpread.fixed(1)),
                
                //new BlobFoliagePlacer(FeatureSpread.fixed(2), FeatureSpread.fixed(0), 3),
                
                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new FancyTrunkPlacer(10, 6, 0),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(1, 0, 2))
                .ignoreVines()
                .build();
        //@formatter:on
    }
    
    public static BaseTreeFeatureConfig getMirageTree()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleBlockStateProvider(BlockInit.MIRAGE_LOG.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleBlockStateProvider(BlockInit.MIRAGE_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new JungleFoliagePlacer(FeatureSpread.fixed(3), FeatureSpread.fixed(0), 3),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(15, 7, 10),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(1, 1, 2))
                .ignoreVines()
                .build();
        //@formatter:on
    }
    
    public static BaseTreeFeatureConfig getDistorticTree()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, different block state providers
                // can allow for randomization in the log
                new SimpleBlockStateProvider(BlockInit.DISTORTIC_LOG.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleBlockStateProvider(BlockInit.DISTORTIC_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new FancyFoliagePlacer(FeatureSpread.fixed(4), FeatureSpread.fixed(0), 6),

                // This is how the tree trunk works, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(13, 10, 0),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three values below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(1, 0, 1))
                .ignoreVines()
                .build();
        //@formatter:on
    }
    
    public static void register()
    {
        Trees.INVERTED_TREE = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:ultra_tree01", Trees.INVERTED_TREE = Feature.TREE.configured(Trees
                        .getInvertedTree().withDecorators(ImmutableList.of(Features.Placements.BEEHIVE_0002))));
        Trees.INVERTED_TREE_FANCY = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
            "pokecube_legends:ultra_tree01", Trees.INVERTED_TREE_FANCY = Feature.TREE.configured(Trees
                .getInvertedTreeFancy().withDecorators(ImmutableList.of(Features.Placements.BEEHIVE_0002))));

        Trees.TEMPORAL_TREE = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:ultra_tree02", Trees.TEMPORAL_TREE = Feature.TREE.configured(Trees
                        .getTemporalTree()));
        Trees.MEGA_TEMPORAL_TREE = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
            "pokecube_legends:ultra_tree02", Trees.MEGA_TEMPORAL_TREE = Feature.TREE.configured(Trees
                .getMegaTemporalTree()));

        Trees.AGED_TREE = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:ultra_tree03", Trees.AGED_TREE = Feature.TREE.configured(Trees
                        .getAgedTree()));

        Trees.CORRUPTED_TREE = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:corrupted_tree", Trees.CORRUPTED_TREE = Feature.TREE.configured(Trees
                        .getCorruptedTree()));

        Trees.MIRAGE_TREE = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:mirage_tree", Trees.MIRAGE_TREE = Feature.TREE.configured(Trees
                        .getMirageTree()));

        Trees.DISTORTIC_TREE = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:distortic_tree", Trees.DISTORTIC_TREE = Feature.TREE.configured(Trees
                        .getDistorticTree()));
    }
}
