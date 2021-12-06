package pokecube.legends.worldgen.surface_builders;

import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.surfacebuilders.NetherCappedSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import pokecube.legends.init.BlockInit;

public class BlindingDeltasSurfaceBuilder extends NetherCappedSurfaceBuilder {
	private static final BlockState DARKSTONE = BlockInit.ULTRA_DARKSTONE.get().defaultBlockState();
	private static final BlockState BLACKSTONE = Blocks.BLACKSTONE.defaultBlockState();
	private static final ImmutableList<BlockState> FLOOR_BLOCK_STATES = ImmutableList.of(DARKSTONE, BLACKSTONE);
	private static final ImmutableList<BlockState> CEILING_BLOCK_STATES = ImmutableList.of(DARKSTONE);

	public BlindingDeltasSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> config) {
		super(config);
	}

	public ImmutableList<BlockState> getFloorBlockStates() {
		return FLOOR_BLOCK_STATES;
	}

	public ImmutableList<BlockState> getCeilingBlockStates() {
		return CEILING_BLOCK_STATES;
	}

	public BlockState getPatchBlockState() {
		return DARKSTONE;
	}
	
	@Override
	public void apply(Random random, ChunkAccess chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState block, 
			BlockState fluid, int seaLevel, int num, long seed, SurfaceBuilderBaseConfiguration config) 
	{
		this.apply(random, chunk, biome, x, z, startHeight, noise, block, fluid, config.getTopMaterial(), config.getUnderMaterial(), config.getUnderwaterMaterial(), seaLevel, num);
	}
	
	public void apply(Random random, ChunkAccess chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState block, 
			BlockState fluid, BlockState top, BlockState middle, BlockState bottom, int seaLevel, int num) 
	{
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int i = (int)(noise / 3.0D + 3.0D + random.nextDouble() * 0.25D);
		if (i == 0) 
		{
			boolean flag = false;
			for(int j = startHeight; j >= num; --j) 
			{
				pos.set(x, j, z);
				BlockState blockstate = chunk.getBlockState(pos);
				if (blockstate.isAir()) 
				{
					flag = false;
				} else if (blockstate.is(block.getBlock())) 
				{
					if (!flag) {
						BlockState blockstate1;
						if (j >= seaLevel) 
						{
							blockstate1 = Blocks.AIR.defaultBlockState();
						} else if (j == seaLevel - 1) 
						{
							blockstate1 = biome.getTemperature(pos) < 0.15F ? Blocks.ICE.defaultBlockState() : fluid;
						} else if (j >= seaLevel - (7 + i)) 
						{
							blockstate1 = block;
						} else 
						{
							blockstate1 = bottom;
						}
						chunk.setBlockState(pos, blockstate1, false);
					}
					flag = true;
				}
			}
		} else 
		{
			BlockState blockstate3 = middle;
			int k = -1;

			for(int l = startHeight; l >= num; --l) 
			{
				pos.set(x, l, z);
				BlockState blockstate4 = chunk.getBlockState(pos);
				if (blockstate4.isAir()) 
				{
					k = -1;
				} else if (blockstate4.is(block.getBlock())) 
				{
					if (k == -1) 
					{
						k = i;
						BlockState blockstate2;
						if (l >= seaLevel + 2)
						{
							blockstate2 = top;
						} else if (l >= seaLevel - 1) 
						{
							blockstate3 = middle;
							blockstate2 = top;
						} else if (l >= seaLevel - 4) 
						{
							blockstate3 = middle;
							blockstate2 = middle;
						} else if (l >= seaLevel - (7 + i)) 
						{
							blockstate2 = blockstate3;
						} else 
						{
							blockstate3 = block;
							blockstate2 = bottom;
						}
						chunk.setBlockState(pos, blockstate2, false);
					} else if (k > 0) 
					{
						--k;
						chunk.setBlockState(pos, blockstate3, false);
						if (k == 0 && blockstate3.is(BlockInit.ULTRA_DARKSTONE.get()) && i > 1) 
						{
							k = random.nextInt(4) + Math.max(0, l - seaLevel);
							blockstate3 = BlockInit.ULTRA_DARKSTONE.get().defaultBlockState();
						}
					}
	            }
	        }
		}
	}
}