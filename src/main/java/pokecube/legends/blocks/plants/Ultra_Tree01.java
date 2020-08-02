package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliageplacer.BlobFoliagePlacer;
import net.minecraftforge.common.IPlantable;
import pokecube.legends.init.BlockInit;

public class Ultra_Tree01 extends Tree {

	public static final TreeFeatureConfig ULTRA_TREE01_CONFIG = (new TreeFeatureConfig.Builder(
		   new SimpleBlockStateProvider(BlockInit.ULTRA_LOGUB01.get().getDefaultState()),
		   new SimpleBlockStateProvider(BlockInit.ULTRA_LEAVEUB01.get().getDefaultState()), 
		   new BlobFoliagePlacer(2,3)))
			.baseHeight(7)
			.heightRandA(3)
			.foliageHeight(3)
			.ignoreVines()
			.trunkHeight(7)
			.heightRandB(2)
			.setSapling((IPlantable) BlockInit.ULTRA_SAPLING_UB01.get()).build();

	@Override
	protected ConfiguredFeature<TreeFeatureConfig, ?> getTreeFeature(Random randomIn, boolean b) {
		return Feature.NORMAL_TREE.withConfiguration(ULTRA_TREE01_CONFIG);
	}
	
}
