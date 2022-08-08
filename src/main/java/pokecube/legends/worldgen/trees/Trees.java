package pokecube.legends.worldgen.trees;

import java.util.OptionalInt;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.Registry;
import net.minecraft.util.random.SimpleWeightedRandomList;
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
import net.minecraft.world.level.levelgen.feature.foliageplacers.RandomSpreadFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.BendingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
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
    private static final BeehiveDecorator BEEHIVE_002 = new BeehiveDecorator(0.02F);
    private static final BeehiveDecorator BEEHIVE_005 = new BeehiveDecorator(0.05F);

    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS = DeferredRegister
            .create(ForgeRegistries.TREE_DECORATOR_TYPES, Reference.ID);
    public static final DeferredRegister<ConfiguredFeature<?, ?>> TREE_FEATURES = DeferredRegister
            .create(Registry.CONFIGURED_FEATURE_REGISTRY, Reference.ID);

    public static final RegistryObject<TreeDecoratorType<?>> LEAVES_STRING_OF_PEARLS = TREE_DECORATORS
            .register("leaves_string_of_pearls", () -> new TreeDecoratorType<>(LeavesStringOfPearlsDecorator.CODEC));
    public static final RegistryObject<TreeDecoratorType<?>> TRUNK_STRING_OF_PEARLS = TREE_DECORATORS
            .register("trunk_string_of_pearls", () -> new TreeDecoratorType<>(TrunkStringOfPearlsDecorator.CODEC));

    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> AGED_PINE_TREE;
    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> AGED_SPRUCE_TREE;
    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> MEGA_AGED_PINE_TREE;
    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> MEGA_AGED_SPRUCE_TREE;
    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> CORRUPTED_TREE;
    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> DISTORTIC_TREE;
    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> DYNA_TREE;
    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> INVERTED_TREE;
    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> INVERTED_TREE_FANCY;
    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> MIRAGE_TREE;
    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> TEMPORAL_TREE;
    public static RegistryObject<ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>>> MEGA_TEMPORAL_TREE;
     

    public static final class States
    {
        public static final BlockState JUNGLE_PODZOL = BlockInit.JUNGLE_PODZOL.get().defaultBlockState();
        public static final BlockState AGED_PODZOL = BlockInit.AGED_PODZOL.get().defaultBlockState();
    }

    public static void init(final IEventBus bus)
    {
        TREE_DECORATORS.register(bus);
        TREE_FEATURES.register(bus);
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
                new StraightTrunkPlacer(7, 5, 0),

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
                .ignoreVines().decorators(ImmutableList.of(BEEHIVE_005));
        //@formatter:on
    }

    public static TreeConfigurationBuilder getAgedSpruceTree()
    {
        return new TreeConfigurationBuilder(BlockStateProvider.simple(BlockInit.AGED_LOG.get().defaultBlockState()),
                new StraightTrunkPlacer(8, 6, 0),
                BlockStateProvider.simple(BlockInit.AGED_LEAVES.get().defaultBlockState()),
                new SpruceFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 2), UniformInt.of(1, 3)),
                new TwoLayersFeatureSize(1, 0, 1)).ignoreVines()
                        .decorators(ImmutableList.of(BEEHIVE_005,
                                new AlterGroundDecorator(BlockStateProvider.simple(States.AGED_PODZOL))));
    }

    public static TreeConfigurationBuilder getMegaAgedPineTree()
    {
        return new TreeConfigurationBuilder(BlockStateProvider.simple(BlockInit.AGED_LOG.get().defaultBlockState()),
                new GiantTrunkPlacer(13, 2, 14),
                BlockStateProvider.simple(BlockInit.AGED_LEAVES.get().defaultBlockState()),
                new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(3, 8)),
                new TwoLayersFeatureSize(1, 1, 2))
                        .decorators(ImmutableList.of(BEEHIVE_005,
                                new AlterGroundDecorator(BlockStateProvider.simple(States.AGED_PODZOL))));
    }

    public static TreeConfigurationBuilder getMegaAgedSpruceTree()
    {
        return new TreeConfigurationBuilder(BlockStateProvider.simple(BlockInit.AGED_LOG.get().defaultBlockState()),
                new GiantTrunkPlacer(13, 2, 14),
                BlockStateProvider.simple(BlockInit.AGED_LEAVES.get().defaultBlockState()),
                new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(8, 13)),
                new TwoLayersFeatureSize(1, 1, 2));
    }

    public static TreeConfigurationBuilder getCorruptedTree()
    {
        return new TreeConfigurationBuilder(
                BlockStateProvider.simple(BlockInit.CORRUPTED_LOG.get().defaultBlockState()),
                new ForkingTrunkPlacer(6, 2, 3),
                BlockStateProvider.simple(BlockInit.CORRUPTED_LEAVES.get().defaultBlockState()),
                new AcaciaFoliagePlacer(ConstantInt.of(3), ConstantInt.of(1)), new TwoLayersFeatureSize(1, 0, 2))
                        .ignoreVines().dirt(BlockStateProvider.simple(BlockInit.ROOTED_CORRUPTED_DIRT.get()))
                        .forceDirt();
    }

    public static TreeConfigurationBuilder getDistorticTree()
    {
        return new TreeConfigurationBuilder(
                BlockStateProvider.simple(BlockInit.DISTORTIC_LOG.get().defaultBlockState()),
                new StraightTrunkPlacer(14, 2, 10),
                BlockStateProvider.simple(BlockInit.DISTORTIC_LEAVES.get().defaultBlockState()),
                new FancyFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0), 6), new TwoLayersFeatureSize(1, 0, 1))
                        .ignoreVines();
    }

    public static TreeConfigurationBuilder getDynaTree()
    {
        return new TreeConfigurationBuilder(BlockStateProvider.simple(BlockInit.AGED_LOG.get()),
                new BendingTrunkPlacer(6, 2, 2, 5, UniformInt.of(1, 2)),
                new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(BlockInit.DYNA_LEAVES_RED.get().defaultBlockState(), 3)
                        .add(BlockInit.DYNA_LEAVES_PINK.get().defaultBlockState(), 1)
                        .add(BlockInit.DYNA_LEAVES_PASTEL_PINK.get().defaultBlockState(), 1)),
                new RandomSpreadFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 50),
                new TwoLayersFeatureSize(1, 0, 1)).decorators(ImmutableList.of(BEEHIVE_002))
                        .dirt(BlockStateProvider.simple(BlockInit.ROOTED_MUSHROOM_DIRT.get())).forceDirt();
    }

    public static TreeConfigurationBuilder getInvertedTree()
    {
        return new TreeConfigurationBuilder(BlockStateProvider.simple(BlockInit.INVERTED_LOG.get().defaultBlockState()),
                new StraightTrunkPlacer(6, 4, 0),
                BlockStateProvider.simple(BlockInit.INVERTED_LEAVES.get().defaultBlockState()),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1))
                        .ignoreVines().decorators(ImmutableList.of(BEEHIVE_005));
    }

    public static TreeConfigurationBuilder getInvertedTreeFancy()
    {
        return new TreeConfigurationBuilder(BlockStateProvider.simple(BlockInit.INVERTED_LOG.get().defaultBlockState()),
                new FancyTrunkPlacer(3, 11, 0),
                BlockStateProvider.simple(BlockInit.INVERTED_LEAVES.get().defaultBlockState()),
                new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4),
                new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))).ignoreVines()
                        .decorators(ImmutableList.of(BEEHIVE_005));
    }

    public static TreeConfigurationBuilder getMirageTree()
    {
        return new TreeConfigurationBuilder(BlockStateProvider.simple(BlockInit.MIRAGE_LOG.get().defaultBlockState()),
                new StraightTrunkPlacer(10, 5, 15),
                BlockStateProvider.simple(BlockInit.MIRAGE_LEAVES.get().defaultBlockState()),
                new MegaJungleFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 1, 2))
                        .ignoreVines();
    }

    public static TreeConfigurationBuilder getTemporalTree()
    {
        return new TreeConfigurationBuilder(BlockStateProvider.simple(BlockInit.TEMPORAL_LOG.get().defaultBlockState()),
                new StraightTrunkPlacer(6, 8, 0),
                BlockStateProvider.simple(BlockInit.TEMPORAL_LEAVES.get().defaultBlockState()),
                new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(2, 0, 2))
                        .decorators(ImmutableList.of(TrunkStringOfPearlsDecorator.INSTANCE,
                                LeavesStringOfPearlsDecorator.INSTANCE, BEEHIVE_0002))
                        .ignoreVines();
    }

    public static TreeConfigurationBuilder getMegaTemporalTree()
    {
        return new TreeConfigurationBuilder(BlockStateProvider.simple(BlockInit.TEMPORAL_LOG.get().defaultBlockState()),
                new MegaJungleTrunkPlacer(12, 4, 24),
                BlockStateProvider.simple(BlockInit.TEMPORAL_LEAVES.get().defaultBlockState()),
                new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 1, 2))
                        .decorators(ImmutableList.of(TrunkStringOfPearlsDecorator.INSTANCE,
                                LeavesStringOfPearlsDecorator.INSTANCE, BEEHIVE_0002,
                                new AlterGroundDecorator(BlockStateProvider.simple(States.JUNGLE_PODZOL))));
    }
    

    static 
    {
        Trees.AGED_PINE_TREE = TREE_FEATURES.register("aged_pine_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getAgedPineTree().build()));
        Trees.AGED_SPRUCE_TREE = TREE_FEATURES.register("aged_spruce_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getAgedSpruceTree().build()));
        Trees.MEGA_AGED_PINE_TREE = TREE_FEATURES.register("mega_aged_pine_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getMegaAgedPineTree().build()));
        Trees.MEGA_AGED_SPRUCE_TREE = TREE_FEATURES.register("mega_aged_spruce_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getMegaAgedSpruceTree().build()));
        
        Trees.CORRUPTED_TREE = TREE_FEATURES.register("corrupted_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getCorruptedTree().build()));
        Trees.DISTORTIC_TREE = TREE_FEATURES.register("distortic_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getDistorticTree().build()));
        Trees.DYNA_TREE = TREE_FEATURES.register("dyna_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getDynaTree().build()));
        Trees.MIRAGE_TREE = TREE_FEATURES.register("mirage_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getMirageTree().build()));

        Trees.INVERTED_TREE = TREE_FEATURES.register("inverted_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getInvertedTree().build()));
        Trees.INVERTED_TREE_FANCY = TREE_FEATURES.register("inverted_fancy_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getInvertedTreeFancy().build()));

        Trees.TEMPORAL_TREE = TREE_FEATURES.register("temporal_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getTemporalTree().build()));
        Trees.MEGA_TEMPORAL_TREE = TREE_FEATURES.register("mega_temporal_tree",  ()->new ConfiguredFeature<>(Feature.TREE, Trees.getMegaTemporalTree().build()));
        
    }
}
