package pokecube.legends.worldgen.structuregen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeDesert;
import net.minecraft.world.biome.BiomeEnd;
import net.minecraft.world.biome.BiomeForest;
import net.minecraft.world.biome.BiomeHell;
import net.minecraft.world.biome.BiomeHills;
import net.minecraft.world.biome.BiomeOcean;
import net.minecraft.world.biome.BiomePlains;
import net.minecraft.world.biome.BiomeRiver;
import net.minecraft.world.biome.BiomeSavanna;
import net.minecraft.world.biome.BiomeSnow;
import net.minecraft.world.biome.BiomeSwamp;
import net.minecraft.world.biome.BiomeTaiga;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.DimensionInit;
import pokecube.legends.worldgen.biomes.UltraUB1;
import pokecube.legends.worldgen.biomes.UltraUB2;
import pokecube.legends.worldgen.biomes.UltraUB3;
import pokecube.legends.worldgen.biomes.UltraUB4;

public class WorldGenCustomStrucute implements IWorldGenerator
{	

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) 
	{
	  final  int ultraspace = DimensionInit.ultraspaceDimensionID;
	   switch(world.provider.getDimension())
		{
		case 1:
			this.generateStructure(new WorldGenStructure("space_temple"), world, random, chunkX, chunkZ, 400, Blocks.END_STONE, BiomeEnd.class);
			
			break;
		case 0:
			this.generateStructure(new WorldGenStructure("celebi_temple"), world, random, chunkX, chunkZ, 600, Blocks.GRASS, BiomeForest.class);
			this.generateStructure(new WorldGenStructure("hooh_tower"), world, random, chunkX, chunkZ, 600, Blocks.GRASS, BiomePlains.class);
			this.generateStructure(new WorldGenStructure("keldeo_place"), world, random, chunkX, chunkZ, 600, Blocks.GRASS, BiomeHills.class);
			this.generateStructure(new WorldGenStructure("zacian_temple"), world, random, chunkX, chunkZ, 600, Blocks.WATER, BiomeRiver.class);
			this.generateStructure(new WorldGenStructure("zamazenta_temple"), world, random, chunkX, chunkZ, 600, Blocks.WATER, BiomeRiver.class);
			this.generateStructure(new WorldGenStructure("kyogre_temple"), world, random, chunkX, chunkZ, 800, Blocks.WATER, BiomeOcean.class);
			this.generateStructure(new WorldGenStructure("lugia_tower"), world, random, chunkX, chunkZ, 600, Blocks.GRASS, BiomeSavanna.class);
			this.generateStructure(new WorldGenStructure("nature_place"), world, random, chunkX, chunkZ, 600, Blocks.GRASS, BiomeSwamp.class);
			this.generateStructure(new WorldGenStructure("regis_temple"), world, random, chunkX, chunkZ, 600, Blocks.GRASS, BiomeSnow.class);
			this.generateStructure(new WorldGenStructure("xerneas_place"), world, random, chunkX, chunkZ, 600, Blocks.GRASS, BiomeTaiga.class);
			this.generateStructure(new WorldGenStructure("yveltal_temple"), world, random, chunkX, chunkZ, 600, Blocks.SAND, BiomeDesert.class);
			
			break;
		case -1:
			this.generateStructure(new WorldGenStructure("groudon_temple"), world, random, chunkX, chunkZ, 400, Blocks.NETHERRACK, BiomeHell.class);
			
			break;
		 default:
		     if(world.provider.getDimension() == ultraspace)
		     {
		            //Biome 1
		            this.generateStructure(new WorldGenStructure("mush_1"), world, random, chunkX, chunkZ, 5, BlockInit.ULTRA_GRASSMUSS, UltraUB1.class);
		            this.generateStructure(new WorldGenStructure("mush_2"), world, random, chunkX, chunkZ, 7, BlockInit.ULTRA_GRASSMUSS, UltraUB1.class);
		            this.generateStructure(new WorldGenStructure("mush_3"), world, random, chunkX, chunkZ, 11, BlockInit.ULTRA_GRASSMUSS, UltraUB1.class);
		            this.generateStructure(new WorldGenStructure("mush_4"), world, random, chunkX, chunkZ, 33, BlockInit.ULTRA_GRASSMUSS, UltraUB1.class);
		            this.generateStructure(new WorldGenStructure("mush_5"), world, random, chunkX, chunkZ, 36, BlockInit.ULTRA_GRASSMUSS, UltraUB1.class);
		            this.generateStructure(new WorldGenStructure("mush_6"), world, random, chunkX, chunkZ, 64, BlockInit.ULTRA_GRASSMUSS, UltraUB1.class);
		            this.generateStructure(new WorldGenStructure("mush_7"), world, random, chunkX, chunkZ, 69, BlockInit.ULTRA_GRASSMUSS, UltraUB1.class);
		            this.generateStructure(new WorldGenStructure("mush_8"), world, random, chunkX, chunkZ, 84, BlockInit.ULTRA_GRASSMUSS, UltraUB1.class);
		            this.generateStructure(new WorldGenStructure("mush_big"), world, random, chunkX, chunkZ, 206, BlockInit.ULTRA_GRASSMUSS, UltraUB1.class);
		            this.generateStructure(new WorldGenStructure("mush_extra"), world, random, chunkX, chunkZ, 350, BlockInit.ULTRA_GRASSMUSS, UltraUB1.class);

		            this.generateStructure(new WorldGenStructure("cristal_1"), world, random, chunkX, chunkZ, 56, BlockInit.ULTRA_GRASSMUSS, UltraUB1.class);
		            
		            //Biome 2
		            this.generateStructure(new WorldGenStructure("jungle_1"), world, random, chunkX, chunkZ, 2, BlockInit.ULTRA_GRASSJUN, UltraUB2.class);
		            this.generateStructure(new WorldGenStructure("jungle_2"), world, random, chunkX, chunkZ, 4, BlockInit.ULTRA_GRASSJUN, UltraUB2.class);
		            this.generateStructure(new WorldGenStructure("stone1"), world, random, chunkX, chunkZ, 86, BlockInit.ULTRA_GRASSJUN, UltraUB2.class);
		            
		            this.generateStructure(new WorldGenStructure("cristal_1"), world, random, chunkX, chunkZ, 56, BlockInit.ULTRA_GRASSJUN, UltraUB2.class);
		            
		            //Biome 3
		            this.generateStructure(new WorldGenStructure("desert_1"), world, random, chunkX, chunkZ, 12, BlockInit.ULTRA_SAND, UltraUB3.class);
		            this.generateStructure(new WorldGenStructure("desert_2"), world, random, chunkX, chunkZ, 7, BlockInit.ULTRA_SAND, UltraUB3.class);
		            this.generateStructure(new WorldGenStructure("desert_3"), world, random, chunkX, chunkZ, 8, BlockInit.ULTRA_SAND, UltraUB3.class);
		            this.generateStructureFly(new WorldGenStructure("mush_float"), world, random, chunkX, chunkZ, 96, BlockInit.ULTRA_SAND, UltraUB3.class);
		            
		            this.generateStructure(new WorldGenStructure("spike_1"), world, random, chunkX, chunkZ, 56, BlockInit.ULTRA_SAND, UltraUB3.class);
		            this.generateStructure(new WorldGenStructure("cristal_1"), world, random, chunkX, chunkZ, 56, BlockInit.ULTRA_SAND, UltraUB3.class);

		            //Biome 4
		            this.generateStructure(new WorldGenStructure("build_1"), world, random, chunkX, chunkZ, 380, BlockInit.ULTRA_COBBLES, UltraUB4.class);
		            this.generateStructure(new WorldGenStructure("build_2"), world, random, chunkX, chunkZ, 258, BlockInit.ULTRA_COBBLES, UltraUB4.class);
		            this.generateStructure(new WorldGenStructure("build_3"), world, random, chunkX, chunkZ, 458, BlockInit.ULTRA_COBBLES, UltraUB4.class);
		            this.generateStructure(new WorldGenStructure("build_4"), world, random, chunkX, chunkZ, 358, BlockInit.ULTRA_COBBLES, UltraUB4.class);
		            
		            this.generateStructure(new WorldGenStructure("cristal_1"), world, random, chunkX, chunkZ, 58, BlockInit.ULTRA_COBBLES, UltraUB4.class);
		     }
		    break;
		}
	}
	
	//Generate normal Structures
	private void generateStructure(WorldGenerator generator, World world, Random random, int chunkX, int chunkZ, int chance, Block topBlock, Class<?>... classes)
	{
		ArrayList<Class<?>> classesList = new ArrayList<Class<?>>(Arrays.asList(classes));
		
		int x = (chunkX * 16) + random.nextInt(15);
		int z = (chunkZ * 16) + random.nextInt(15);
		int y = calculateGenerationHeight(world, x, z, topBlock) - 2;
		BlockPos pos = new BlockPos(x,y,z);
		
		Class<?> biome = world.provider.getBiomeForCoords(pos).getClass();
		
		if(world.getWorldType() != WorldType.FLAT)
		{
			if(classesList.contains(biome))
			{
				if(random.nextInt(chance) == 0)
				{
					generator.generate(world, random, pos);
				}
			}
		}
	}
	
	//Generate Float Strutures
	private void generateStructureFly(WorldGenerator generator, World world, Random random, int chunkX, int chunkZ, int chance, Block topBlock, Class<?>... classes)
	{
		ArrayList<Class<?>> classesList = new ArrayList<Class<?>>(Arrays.asList(classes));
		
		int x = (chunkX * 16) + random.nextInt(15);
		int z = (chunkZ * 16) + random.nextInt(15);
		int y = calculateGenerationHeight(world, x, z, topBlock) + 23;
		BlockPos pos = new BlockPos(x,y,z);
		
		Class<?> biome = world.provider.getBiomeForCoords(pos).getClass();
		
		if(world.getWorldType() != WorldType.FLAT)
		{
			if(classesList.contains(biome))
			{
				if(random.nextInt(chance) == 0)
				{
					generator.generate(world, random, pos);
				}
			}
		}
	}
	
	/*//Generate Underground Structures
	@SuppressWarnings("unchecked")
	private void generateStructureUnderground(WorldGenerator generator, World world, Random random, int chunkX, int chunkZ, int chance, Block topBlock, Class<?>... classes)
	{
		ArrayList<Class<?>> classesList = new ArrayList<Class<?>>(Arrays.asList(classes));
		
		int x = (chunkX * 16) + random.nextInt(15);
		int z = (chunkZ * 16) + random.nextInt(15);
		int y = calculateGenerationHeight(world, x, z, topBlock) - 5;
		BlockPos pos = new BlockPos(x,y,z);
		
		Class<?> biome = world.provider.getBiomeForCoords(pos).getClass();
		
		if(world.getWorldType() != WorldType.FLAT)
		{
			if(classesList.contains(biome))
			{
				if(random.nextInt(chance) == 0)
				{
					generator.generate(world, random, pos);
				}
			}
		}
	}*/
	
	private static int calculateGenerationHeight(World world, int x, int z, Block topBlock)
	{
		int y = world.getHeight();
		boolean foundGround = false;
		
		while(!foundGround && y-- >= 0)
		{
			Block block = world.getBlockState(new BlockPos(x,y,z)).getBlock();
			foundGround = block == topBlock;
		}
		
		return y;
	}
}
