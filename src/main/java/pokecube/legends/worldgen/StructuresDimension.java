package pokecube.legends.worldgen;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class StructuresDimension 
{	
	public void SpawnInit(FMLCommonSetupEvent event) {
		setupStructureGround("mirror_1", 10000);
		setupStructureGround("mirror_2", 10000);
		setupStructureGround("mirror_3", 25000);
		setupStructureGround("dist_tree", 45000);
		
		setupStructureSky("island_1", 18000);
		setupStructureSky("island_2", 19000);
		setupStructureSky("island_3", 22000);
		setupStructureSky("island_4", 30000);
	}
	
	public void setupStructureGround(final String name, final int chance) {
		Feature<NoFeatureConfig> feature = new Feature<NoFeatureConfig>(NoFeatureConfig::deserialize) {
	@Override
	public boolean place(IWorld world, ChunkGenerator<?> generator, Random random, BlockPos pos, NoFeatureConfig config) {
		int ci = (pos.getX() >> 4) << 4;
		int ck = (pos.getZ() >> 4) << 4;
		DimensionType dimensionType = world.getDimension().getType();
		boolean dimensionCriteria = false;
		if (dimensionType == ModDimensions.DIMENSION_TYPE_DW)
			dimensionCriteria = true;
		if (!dimensionCriteria)
			return false;
		if ((random.nextInt(1000000) + 1) <= chance) {
			int count = random.nextInt(1) + 1;
			for (int a = 0; a < count; a++) {
				int i = ci + random.nextInt(16);
				int k = ck + random.nextInt(16);
				int j = world.getHeight(Type.WORLD_SURFACE_WG, i, k);
				j -= 1;
				BlockState blockAt = world.getBlockState(new BlockPos(i, j, k));
				boolean blockCriteria = false;
				if (blockAt.getBlock() == BlockInit.DISTORTIC_GRASS.get().getDefaultState().getBlock())
					blockCriteria = true;
				if (!blockCriteria)
					continue;
				Rotation rotation = Rotation.values()[random.nextInt(3)];
				Mirror mirror = Mirror.values()[random.nextInt(2)];
				BlockPos spawnTo = new BlockPos(i, j + 0, k);
				Template template = ((ServerWorld) world.getWorld()).getSaveHandler().getStructureTemplateManager()
						.getTemplateDefaulted(new ResourceLocation(Reference.ID, name));
				if (template == null)
					return false;
				template.addBlocksToWorld(world, spawnTo, new PlacementSettings().setRotation(rotation).setRandom(random).setMirror(mirror)
						.addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK).setChunk(null).setIgnoreEntities(false));
			}
		}
		return true;
	}
};
		for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
			biome.addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, feature.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
					.withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
		}
	}
	
	public void setupStructureSky(final String name, final int chance) {
		Feature<NoFeatureConfig> feature = new Feature<NoFeatureConfig>(NoFeatureConfig::deserialize) {
	@Override
	public boolean place(IWorld world, ChunkGenerator<?> generator, Random random, BlockPos pos, NoFeatureConfig config) {
		int ci = (pos.getX() >> 4) << 4;
		int ck = (pos.getZ() >> 4) << 4;
		DimensionType dimensionType = world.getDimension().getType();
		boolean dimensionCriteria = false;
		if (dimensionType == ModDimensions.DIMENSION_TYPE_DW)
			dimensionCriteria = true;
		if (!dimensionCriteria)
			return false;
		if ((random.nextInt(1000000) + 1) <= chance) {
			int count = random.nextInt(1) + 1;
			for (int a = 0; a < count; a++) {
				int i = ci + random.nextInt(16);
				int k = ck + random.nextInt(16);
				int j = world.getHeight(Type.WORLD_SURFACE_WG, i, k);
				j += random.nextInt(32) + 8;
				Rotation rotation = Rotation.values()[random.nextInt(3)];
				Mirror mirror = Mirror.values()[random.nextInt(2)];
				BlockPos spawnTo = new BlockPos(i, j + 18, k);
				Template template = ((ServerWorld) world.getWorld()).getSaveHandler().getStructureTemplateManager()
						.getTemplateDefaulted(new ResourceLocation(Reference.ID, name));
				if (template == null)
					return false;
				template.addBlocksToWorld(world, spawnTo, new PlacementSettings().setRotation(rotation).setRandom(random).setMirror(mirror)
						.addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK).setChunk(null).setIgnoreEntities(false));
			}
		}
		return true;
	}
};
		for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
			boolean biomeCriteria = false;
			if (ForgeRegistries.BIOMES.getKey(biome).equals(new ResourceLocation("pokecube_legends:distortic_world")))
				biomeCriteria = true;
			if (!biomeCriteria)
				continue;
			biome.addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, feature.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
					.withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
		}
	}
}
