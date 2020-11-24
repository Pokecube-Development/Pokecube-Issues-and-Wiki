package pokecube.legends.worldgen.biomes;

import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;
import pokecube.legends.init.BlockInit;

public class DistorticBiomeFeautures {
	private static final BlockState WATER = BlockInit.DISTORTIC_MIRROR.get().getDefaultState();
	
	public static void addLakes(Biome biomeIn) {
	      biomeIn.addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, Feature.LAKE.withConfiguration(new BlockStateFeatureConfig(WATER)).withPlacement(Placement.WATER_LAKE.configure(new ChanceConfig(4))));
	}
}
