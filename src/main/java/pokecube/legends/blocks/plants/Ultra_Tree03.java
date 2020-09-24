package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliageplacer.SpruceFoliagePlacer;
import net.minecraftforge.common.IPlantable;
import pokecube.legends.init.BlockInit;

public class Ultra_Tree03 extends Tree {

	public static final TreeFeatureConfig ULTRA_TREE03_CONFIG = (new TreeFeatureConfig.Builder(
		   new SimpleBlockStateProvider(BlockInit.ULTRA_LOGUB03.get().getDefaultState()),
		   new SimpleBlockStateProvider(BlockInit.ULTRA_LEAVEUB03.get().getDefaultState()), 
		   new SpruceFoliagePlacer(3,1))).baseHeight(7).heightRandA(3).foliageHeight(6).ignoreVines()
			.setSapling((IPlantable) BlockInit.ULTRA_SAPLING_UB03.get()).build();

	@Override
	protected ConfiguredFeature<TreeFeatureConfig, ?> getTreeFeature(Random randomIn, boolean b) {
		return Feature.NORMAL_TREE.withConfiguration(ULTRA_TREE03_CONFIG);
	}
	
}
