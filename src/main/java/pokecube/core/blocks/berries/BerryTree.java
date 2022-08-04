package pokecube.core.blocks.berries;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import pokecube.core.PokecubeCore;
import pokecube.legends.init.BlockInit;

public class BerryTree {

    private static final BeehiveDecorator BEEHIVE_0002 = new BeehiveDecorator(0.002F);
    private static final BeehiveDecorator BEEHIVE_002 = new BeehiveDecorator(0.02F);
    private static final BeehiveDecorator BEEHIVE_005 = new BeehiveDecorator(0.05F);

    public static final BlockPredicate BERRY_TREE_PREDICATE = BlockPredicate.matchesTag(BlockTags.DIRT, new BlockPos(0, -1, 0));

    public static TreeConfiguration.TreeConfigurationBuilder getLeppaTree()
    {
        PokecubeCore.LOGGER.info("Generating Berry Blocks");
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(BlockInit.INVERTED_LOG.get().defaultBlockState()),
                new StraightTrunkPlacer(6, 4, 0),
                BlockStateProvider.simple(BlockInit.INVERTED_LEAVES.get().defaultBlockState()),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1))
                .ignoreVines().decorators(ImmutableList.of(BEEHIVE_005));
    }
}
