package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features;

public class Ultra_Tree01 extends Tree {

    //TODO Re-figure this out?
//	public static final BaseTreeFeatureConfig ULTRA_TREE01_CONFIG = (new BaseTreeFeatureConfig.Builder(
//		   new SimpleBlockStateProvider(BlockInit.ULTRA_LOGUB01.get().getDefaultState()),
//		   new SimpleBlockStateProvider(BlockInit.ULTRA_LEAVEUB01.get().getDefaultState()),
//		   new PineFoliagePlacer(3,0)))
//			.baseHeight(4)
//			.heightRandA(3)
//			.heightRandB(2)
//			.foliageHeight(3)
//			.ignoreVines()
//			.setSapling((IPlantable) BlockInit.ULTRA_SAPLING_UB01.get()).build();

	@Override
	protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getTreeFeature(final Random randomIn, final boolean b) {
		return Features.OAK;
	}

}
