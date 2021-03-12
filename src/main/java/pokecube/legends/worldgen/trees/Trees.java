package pokecube.legends.worldgen.trees;

import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureSpread;
import net.minecraft.world.gen.feature.TwoLayerFeature;
import net.minecraft.world.gen.foliageplacer.AcaciaFoliagePlacer;
import net.minecraft.world.gen.foliageplacer.BlobFoliagePlacer;
import net.minecraft.world.gen.foliageplacer.JungleFoliagePlacer;
import net.minecraft.world.gen.foliageplacer.SpruceFoliagePlacer;
import net.minecraft.world.gen.trunkplacer.FancyTrunkPlacer;
import net.minecraft.world.gen.trunkplacer.StraightTrunkPlacer;
import pokecube.legends.init.BlockInit;

public class Trees
{
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> ULTRA_TREE01;
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> ULTRA_TREE02;
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> ULTRA_TREE03;
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> ULTRA_TREE04;
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> ULTRA_TREE05;
    
    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> DISTORTIC_TREE;

    public static BaseTreeFeatureConfig getUltra01()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, differnt block state providers
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

                // This is how the tree trunk work, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(4, 2, 0),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three valies below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(1, 0, 1))
                .ignoreVines()
                .build();
        //@formatter:on
    }
    
    public static BaseTreeFeatureConfig getUltra02()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, differnt block state providers
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

                // This is how the tree trunk work, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(6, 4, 0),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three valies below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(2, 0, 2))
                .ignoreVines()
                .build();
        //@formatter:on
    }
    
    public static BaseTreeFeatureConfig getUltra03()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, differnt block state providers
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

                // This is how the tree trunk work, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(6, 3, 0),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three valies below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(1, 0, 1))
                .ignoreVines()
                .build();
        //@formatter:on
    }

    public static BaseTreeFeatureConfig getUltra04()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, differnt block state providers
                // can allow for randomization in the log
                new SimpleBlockStateProvider(BlockInit.CORRUPTED_LOG.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleBlockStateProvider(BlockInit.CORRUPTED_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new AcaciaFoliagePlacer(FeatureSpread.fixed(3), FeatureSpread.fixed(0)),
                
                //new BlobFoliagePlacer(FeatureSpread.fixed(2), FeatureSpread.fixed(0), 3),
                
                // This is how the tree trunk work, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new FancyTrunkPlacer(10, 6, 0),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three valies below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(1, 0, 2))
                .ignoreVines()
                .build();
        //@formatter:on
    }
    
    public static BaseTreeFeatureConfig getUltra05()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, differnt block state providers
                // can allow for randomization in the log
                new SimpleBlockStateProvider(BlockInit.MIRAGE_LOG.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleBlockStateProvider(BlockInit.MIRAGE_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new BlobFoliagePlacer(FeatureSpread.fixed(1), FeatureSpread.fixed(0), 2),

                // This is how the tree trunk work, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(13, 3, 0),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three valies below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(1, 0, 1))
                .ignoreVines()
                .build();
        //@formatter:on
    }
    
    public static BaseTreeFeatureConfig getDist01()
    {
        return new BaseTreeFeatureConfig.Builder(
        //@formatter:off
                // This line specifies what is the base log, differnt block state providers
                // can allow for randomization in the log
                new SimpleBlockStateProvider(BlockInit.DISTORTIC_LOG.get().defaultBlockState()),

                // This one is similar, but for the leaves
                new SimpleBlockStateProvider(BlockInit.DISTORTIC_LEAVES.get().defaultBlockState()),

                // This is how the leaves are arranged, this is the default for oak, there
                // are also AcaciaFoliagePlacer, DarkOak, Jungle, MegaPine, Pine and Spruce available
                // more can also probably be coded if needed
                // The FeatureSpread.fixed(2) is "base of 2, spread of 0", and FeatureSpread.fixed(0)
                // is "base of 0, spread of 0", in this case, it determines the shape and size of the blob.
                new BlobFoliagePlacer(FeatureSpread.fixed(3), FeatureSpread.fixed(0), 3),

                // This is how the tree trunk work, there are also DarkOak, Fancy,
                // Forky, Giant, MegaJungle available
                new StraightTrunkPlacer(7, 6, 0),

                // I am not certain exactly how this works, but there is also a threeLayer feature
                // available, it is used by dark oak, see Features.DARK_OAK to see how it is used.
                // Different trees use a variety of the three valies below, usually ranging from
                // 0 to 2, this example is from basic oak trees, but it can vary for different ones
                new TwoLayerFeature(1, 0, 1))
                .ignoreVines()
                .build();
        //@formatter:on
    }
    
    public static void register()
    {
        Trees.ULTRA_TREE01 = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:ultra_tree01", Trees.ULTRA_TREE01 = Feature.TREE.configured(Trees
                        .getUltra01()));
        Trees.ULTRA_TREE02 = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:ultra_tree02", Trees.ULTRA_TREE02 = Feature.TREE.configured(Trees
                        .getUltra02()));
        Trees.ULTRA_TREE03 = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:ultra_tree03", Trees.ULTRA_TREE03 = Feature.TREE.configured(Trees
                        .getUltra03()));
        Trees.ULTRA_TREE04 = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:corrupted_tree", Trees.ULTRA_TREE04 = Feature.TREE.configured(Trees
                        .getUltra04()));
        Trees.ULTRA_TREE05 = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:mirage_tree", Trees.ULTRA_TREE05 = Feature.TREE.configured(Trees
                        .getUltra05()));
        Trees.DISTORTIC_TREE = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE,
                "pokecube_legends:distortic_tree", Trees.DISTORTIC_TREE = Feature.TREE.configured(Trees
                        .getDist01()));
    }

}
