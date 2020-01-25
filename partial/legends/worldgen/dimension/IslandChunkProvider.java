package pokecube.legends.worldgen.dimension;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.genlayer.MapGenCave;
import net.minecraftforge.event.terraingen.TerrainGen;

public class IslandChunkProvider implements IChunkGenerator
{
		//Block that replaces End Stone
		protected static final IBlockState MAIN_BLOCK = BlockInit.ULTRA_STONE.getDefaultState();
	    protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
	    
	    //List of all the mobs you want to spawn
	    protected List<SpawnListEntry> spawnableCreatureList = Lists.<SpawnListEntry>newArrayList();
		
	    //Keep all these the same
	    private static final int SEALEVEL = 63;
	    private final Random rand;
	    private final World world;
	    private final boolean mapFeaturesEnabled;
		private Biome[] biomesForGeneration;
	    private int chunkX = 0, chunkZ = 0;
	    private final NoiseGeneratorSimplex islandNoise;
		private final NoiseGeneratorOctaves perlinnoise1;
		private final NoiseGeneratorOctaves perlinnoise2;
		private final NoiseGeneratorOctaves perlinnoise3;
		private final NoiseGeneratorPerlin height;
	    private double[] buffer;
	    private double[] depthbuff = new double[256];
	    double[] p, a, b;
	    private WorldGenerator farIslands;
	    private MapGenBase caveGenerator = new MapGenCave();
	    
		public IslandChunkProvider(World world, boolean mapFeaturesEnabled, long seed, BlockPos spawn)
	    {
	    	world.setSeaLevel(SEALEVEL);
	        this.world = world;
	        this.mapFeaturesEnabled = mapFeaturesEnabled;
	        this.rand = new Random(seed);
	        postTerrainGenEvents();
	         
	        this.perlinnoise1 = new NoiseGeneratorOctaves(this.rand, 17);
			this.perlinnoise2 = new NoiseGeneratorOctaves(this.rand, 17);
			this.perlinnoise3 = new NoiseGeneratorOctaves(this.rand, 8);
			this.height = new NoiseGeneratorPerlin(this.rand, 7);
			this.islandNoise = new NoiseGeneratorSimplex(this.rand);
			this.farIslands = new WorldGenerator() {
				public boolean generate(World worldIn, Random rand, BlockPos position) {
					float f = (float) (rand.nextInt(4) + 5);
					for (int i = 0; f > 1.5F; --i) {
						for (int j = MathHelper.floor(-f); j <= MathHelper.ceil(f); ++j) {
							for (int k = MathHelper.floor(-f); k <= MathHelper.ceil(f); ++k) {
								if ((float) (j * j + k * k) <= (f + 1.0F) * (f + 1.0F)) {
									this.setBlockAndNotifyAdequately(worldIn, position.add(j, i, k), MAIN_BLOCK);
								}
							}
						}
						f = (float) ((double) f - ((double) rand.nextInt(2) + 0.5D));
					}
					return true;
				}
			};
	    }
	    
	    protected void postTerrainGenEvents()
	    {
	        caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, EventType.CAVE);
	    }
	    
	    private double[] getHeights(double[] buffer, int x, int z, int sx, int sy, int sz)
	    {
	    	if (buffer == null) {
				buffer = new double[sx * sy * sz];
			}
			double d0 = 684.412D;
			double d1 = 684.412D;
			d0 = d0 * 2.0D;
			this.p = this.perlinnoise3.generateNoiseOctaves(this.p, x, 0, z, sx, sy, sz, d0 / 80.0D, d1 / 160.0D, d0 / 80.0D);
			this.a = this.perlinnoise1.generateNoiseOctaves(this.a, x, 0, z, sx, sy, sz, d0, d1, d0);
			this.b = this.perlinnoise2.generateNoiseOctaves(this.b, x, 0, z, sx, sy, sz, d0, d1, d0);
			int i = x / 2;
			int j = z / 2;
			int k = 0;
			for (int l = 0; l < sx; ++l) {
				for (int i1 = 0; i1 < sz; ++i1) {
					float f = this.getIslandHeight(i, j, l, i1);
					for (int j1 = 0; j1 < sy; ++j1) {
						double d2;
						double d3 = this.a[k] / 512.0D;
						double d4 = this.b[k] / 512.0D;
						double d5 = (this.p[k] / 10.0D + 1.0D) / 2.0D;
						if (d5 < 0) {
							d2 = d3;
						} else if (d5 > 1.0D) {
							d2 = d4;
						} else {
							d2 = d3 + (d4 - d3) * d5;
						}
						d2 = d2 - 8.0D;
						d2 = d2 + (double) f;
						int k1 = 2;
						if (j1 > sy / 2 - k1) {
							double d6 = (float) (j1 - (sy / 2 - k1)) / 64.0F;
							d6 = MathHelper.clamp(d6, 0, 1.0D);
							d2 = d2 * (1.0D - d6) + -3000.0D * d6;
						}
						k1 = 8;
						if (j1 < k1) {
							double d7 = (float) (k1 - j1) / ((float) k1 - 1.0F);
							d2 = d2 * (1.0D - d7) + -30.0D * d7;
						}
						buffer[k] = d2;
						++k;
					}
				}
			}
			return buffer;
	    }
	    
		private void setBlocksInChunk(int x, int z, ChunkPrimer primer)
	    {
	        int i = 2;
	        int j = 3;
	        int k = 33;
	        int l = 3;
	        this.buffer = this.getHeights(this.buffer, x * i, z * i, j, k, l);
	        for (int i1 = 0; i1 < 2; ++i1)
	        {
	            for (int j1 = 0; j1 < 2; ++j1)
	            {
	                for (int k1 = 0; k1 < 32; ++k1)
	                {
	                	double d0 = 0.25D;
						final int i4 = (i1 * l + j1) * k;
						final int i5 = ((i1 + 1) * l + j1 + 1) * k;
						final int i6 = (i1 * l + j1 + 1) * k;
						final int i7 = ((i1 + 1) * l + j1) * k;
						double d1 = this.buffer[i4 + k1];
						double d2 = this.buffer[i6 + k1];
						double d3 = this.buffer[i7 + k1];
						double d4 = this.buffer[i5 + k1];
						double d5 = (this.buffer[i4 + k1 + 1] - d1) * d0;
						double d6 = (this.buffer[i6 + k1 + 1] - d2) * d0;
						double d7 = (this.buffer[i7 + k1 + 1] - d3) * d0;
						double d8 = (this.buffer[i5 + k1 + 1] - d4) * d0;
						for (int l1 = 0; l1 < 4; ++l1) {
							double d9 = 0.125D;
							double d10 = d1;
							double d11 = d2;
							double d12 = (d3 - d1) * d9;
							double d13 = (d4 - d2) * d9;
							for (int i2 = 0; i2 < 8; ++i2) {
								double d14 = 0.125D;
								double d15 = d10;
								double d16 = (d11 - d10) * d14;
								for (int j2 = 0; j2 < 8; ++j2) {
									IBlockState iblockstate = AIR;
									if (d15 > 0) {
										iblockstate = MAIN_BLOCK;
									}
									int k2 = i2 + i1 * 8;
									int l2 = l1 + k1 * 4;
									int i3 = j2 + j1 * 8;
									primer.setBlockState(k2, l2, i3, iblockstate);
									d15 += d16;
								}
								d10 += d12;
								d11 += d13;
							}
							d1 += d5;
							d2 += d6;
							d3 += d7;
							d4 += d8;
	                    }
	                }
	            }
	        }
	    }

		public void buildSurfaces(ChunkPrimer primer)
	    {
	        if (!ForgeEventFactory.onReplaceBiomeBlocks(this, this.chunkX, this.chunkZ, primer, this.world)) return;
	        for (int i = 0; i < 16; ++i)
	        {
	            for (int j = 0; j < 16; ++j)
	            {
	                int l = -1;
	                IBlockState iblockstate = MAIN_BLOCK;
	                IBlockState iblockstate1 = MAIN_BLOCK;

	                for (int i1 = 127; i1 >= 0; --i1)
	                {
	                    IBlockState iblockstate2 = primer.getBlockState(i, i1, j);

	                    if (iblockstate2.getMaterial() == Material.AIR)
	                    {
	                        l = -1;
	                    }
	                    else if (iblockstate2.getBlock() == Blocks.STONE)
	                    {
	                        if (l == -1)
	                        {
	                            l = 1;

	                            if (i1 >= 0)
	                            {
	                                primer.setBlockState(i, i1, j, iblockstate);
	                            }
	                            else
	                            {
	                                primer.setBlockState(i, i1, j, iblockstate1);
	                            }
	                        }
	                        else if (l > 0)
	                        {
	                            --l;
	                            primer.setBlockState(i, i1, j, iblockstate1);
	                        }
	                    }
	                }
	            }
	        }
	    }

	    @Override
	    public Chunk generateChunk(int x, int z)
	    {
	    	this.rand.setSeed((long) x * 341845128712L + (long) z * 132897327541L);
			ChunkPrimer chunkprimer = new ChunkPrimer();
			this.setBlocksInChunk(x, z, chunkprimer);
			this.biomesForGeneration = this.world.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, x * 16, z * 16, 16, 16);
			this.replaceBiomeBlocks(x, z, chunkprimer, this.biomesForGeneration);
			caveGenerator.generate(this.world, x, z, chunkprimer);
	        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
	        byte[] abyte = chunk.getBiomeArray();
	        for (int i = 0; i < abyte.length; ++i){
	            abyte[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
	        }
	        chunk.generateSkylightMap();
	        return chunk;
	    }

		private float getIslandHeightValue(int x, int z, int part3, int part4)
	    {
			float f = (float) (x * 2 + part3);
			float f1 = (float) (z * 2 + part4);
			float f2 = 100.0F - MathHelper.sqrt(f * f + f1 * f1) * 8.0F;
			if (f2 > 80.0F) {
				f2 = 80.0F;
			}
			if (f2 < -100.0F) {
				f2 = -100.0F;
			}
			for (int i = -12; i <= 12; ++i) {
				for (int j = -12; j <= 12; ++j) {
					long k = x + i;
					long l = z + j;
					if (k * k + l * l > 4096L && this.islandNoise.getValue((double) k, (double) l) < -0.8999999761581421D) {
						float f3 = (MathHelper.abs((float) k) * 3439.0F + MathHelper.abs((float) l) * 147.0F) % 13.0F + 9.0F;
						f = (float) (part3 - i * 2);
						f1 = (float) (part4 - j * 2);
						float f4 = 100.0F - MathHelper.sqrt(f * f + f1 * f1) * f3;
						if (f4 > 80.0F) {
							f4 = 80.0F;
						}
						if (f4 < -100.0F) {
							f4 = -100.0F;
						}
						if (f4 > f2) {
							f2 = f4;
						}
					}
				}
			}
			return f2;
	    }

		public boolean isIslandChunk(int x, int z)
	    {
	        return (long)x * (long)x + (long)z * (long)z > 4096L && this.getIslandHeightValue(x, z, 1, 1) >= 0.0F;
	    }

	    @SuppressWarnings("unused")
		@Override
		public void populate(int x, int z) {
			BlockFalling.fallInstantly = true;
			net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, false);
			BlockPos blockpos = new BlockPos(x * 16, 0, z * 16);
			
			//Strutures
			if (this.mapFeaturesEnabled)
			{
	        }
			long i = (long) x * (long) x + (long) z * (long) z;
			float f = this.getIslandHeight(x, z, 1, 1);
			if (f < -10.0F && this.rand.nextInt(6) == 0) {
				this.farIslands.generate(this.world, this.rand,
						blockpos.add(this.rand.nextInt(16) + 8, 55 + this.rand.nextInt(16), this.rand.nextInt(16) + 8));
				if (this.rand.nextInt(4) == 0)
					this.farIslands.generate(this.world, this.rand,
							blockpos.add(this.rand.nextInt(16) + 8, 55 + this.rand.nextInt(16), this.rand.nextInt(16) + 8));
			}
			this.world.getBiome(blockpos.add(16, 0, 16)).decorate(this.world, this.world.rand, blockpos);
			net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, x, z, false);
			BlockFalling.fallInstantly = false;
		}
	    
	    private void replaceBiomeBlocks(int x, int z, ChunkPrimer primer, Biome[] biomesIn) {
	    	if (!net.minecraftforge.event.ForgeEventFactory.onReplaceBiomeBlocks(this, x, z, primer, this.world))
				return;
	    	double d0 = 0.03125;
			this.depthbuff = this.height.getRegion(this.depthbuff, (double) (x * 16), (double) (z * 16), 16, 16, d0 * 2.0, d0 * 2.0, 1.0);
			for (int i = 0; i < 16; ++i) {
				for (int j = 0; j < 16; ++j) {
					Biome Biome = biomesIn[j + i * 16];
					generateBiomeTerrain(this.world, this.rand, primer, x * 16 + i, z * 16 + j, this.depthbuff[j + i * 16], Biome);
				}
			}
		}
	    
	    private float getIslandHeight(int x, int z, int par3, int par4) {
			float f = (float) (x * 2 + par3);
			float f1 = (float) (z * 2 + par4);
			float f2 = 100.0F - MathHelper.sqrt(f * f + f1 * f1) * 8.0F;
			if (f2 > 80.0F) {
				f2 = 80.0F;
			}
			if (f2 < -100.0F) {
				f2 = -100.0F;
			}
			for (int i = -12; i <= 12; ++i) {
				for (int j = -12; j <= 12; ++j) {
					long k = x + i;
					long l = z + j;
					if (k * k + l * l > 4096L && islandNoise.getValue((double) k, (double) l) < -0.8999999761581421D) {
						float f3 = (MathHelper.abs((float) k) * 3439.0F + MathHelper.abs((float) l) * 147.0F) % 13.0F + 9.0F;
						f = (float) (par3 - i * 2);
						f1 = (float) (par4 - j * 2);
						float f4 = 100.0F - MathHelper.sqrt(f * f + f1 * f1) * f3;
						if (f4 > 80.0F) {
							f4 = 80.0F;
						}
						if (f4 < -100.0F) {
							f4 = -100.0F;
						}
						if (f4 > f2) {
							f2 = f4;
						}
					}
				}
			}
			return f2;
		}
	    
	    @SuppressWarnings("unused")
		private void generateBiomeTerrain(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal, Biome biome) {
			int i = SEALEVEL;
			IBlockState iblockstate = biome.topBlock;
			IBlockState iblockstate1 = biome.fillerBlock;
			int j = -1;
			int k = (int) (noiseVal / 3.0 + 3.0 + rand.nextDouble() * 0.25);
			int l = x & 15;
			int i1 = z & 15;
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
			for (int j1 = 255; j1 >= 0; --j1) {
				IBlockState iblockstate2 = chunkPrimerIn.getBlockState(i1, j1, l);
				if (iblockstate2.getMaterial() == Material.AIR) {
					j = -1;
				} else if (iblockstate2.getBlock() == MAIN_BLOCK.getBlock()) {
					if (j == -1) {
						if (k <= 0) {
							iblockstate = AIR;
							iblockstate1 = MAIN_BLOCK;
						} else if (j1 >= i - 4 && j1 <= i + 1) {
							iblockstate = biome.topBlock;
							iblockstate1 = biome.fillerBlock;
						}
						j = k;
						if (j1 >= i - 1) {
							chunkPrimerIn.setBlockState(i1, j1, l, iblockstate);
						} else if (j1 < i - 7 - k) {
							iblockstate = AIR;
							iblockstate1 = MAIN_BLOCK;
						} else {
							chunkPrimerIn.setBlockState(i1, j1, l, iblockstate1);
						}
					} else if (j > 0) {
						--j;
						chunkPrimerIn.setBlockState(i1, j1, l, iblockstate1);
						if (j == 0 && iblockstate1.getBlock() == Blocks.SAND) {
							j = rand.nextInt(4) + Math.max(0, j1 - SEALEVEL);
							iblockstate1 = MAIN_BLOCK;
						}
					}
				}
			}
		}

	    @Override
	    public boolean generateStructures(Chunk chunkIn, int x, int z)
	    {
	        return true;
	    }

	    @Override
	    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
	    {
	        return spawnableCreatureList;
	    }

	    @Nullable
	    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
	    {
	        return null;
	    }

	    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
	    {
	        return false;
	    }

	    @Override
	    public void recreateStructures(Chunk chunkIn, int x, int z)
	    {
	    }
}