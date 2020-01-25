package pokecube.legends.worldgen.gencustom;

import java.util.List;
import java.util.Random;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import pokecube.legends.worldgen.genlayer.GenLayerFix;

public class BiomeProviderCustom extends BiomeProvider
{
	private GenLayer genBiomes;
	private GenLayer biomeIndexLayer;
	private BiomeCache biomeCache;

	public BiomeProviderCustom() 
	{
		this.biomeCache = new BiomeCache(this);
	}

	public BiomeProviderCustom(long seed) 
	{
		this.biomeCache = new BiomeCache(this);
		GenLayer[] agenlayer = GenLayerFix.createWorld(seed);
		this.genBiomes = agenlayer[0];
		this.biomeIndexLayer = agenlayer[1];
	}

	public BiomeProviderCustom(World world) 
	{
		this(world.getSeed());
	}

	@Override
	public Biome getBiome(BlockPos pos) 
	{
		return this.getBiome(pos, null);
	}

	@Override
	public Biome getBiome(BlockPos pos, Biome defaultBiome) 
	{
		return this.biomeCache.getBiome(pos.getX(), pos.getZ(), defaultBiome);
	}

	@Override
	public Biome[] getBiomesForGeneration(Biome[] par1ArrayOfBiome, int par2, int par3, int par4, int par5) 
	{
		IntCache.resetIntCache();
		if (par1ArrayOfBiome == null || par1ArrayOfBiome.length < par4 * par5) 
		{
			par1ArrayOfBiome = new Biome[par4 * par5];
		}
		
		int[] aint = this.genBiomes.getInts(par2, par3, par4, par5);
		try 
		{
			for (int i = 0; i < par4 * par5; ++i) 
			{
				par1ArrayOfBiome[i] = Biome.getBiome(aint[i]);
			}
			return par1ArrayOfBiome;
		} 
		
		catch (Throwable throwable) 
		{
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("RawBiomeBlock");
			crashreportcategory.addCrashSection("biomes[] size", par1ArrayOfBiome.length);
			crashreportcategory.addCrashSection("x", par2);
			crashreportcategory.addCrashSection("z", par3);
			crashreportcategory.addCrashSection("w", par4);
			crashreportcategory.addCrashSection("h", par5);
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public Biome[] getBiomes(Biome[] oldBiomeList, int x, int z, int width, int depth) 
	{
		return this.getBiomes(oldBiomeList, x, z, width, depth, true);
	}

	@Override
	public Biome[] getBiomes(Biome[] listToReuse, int x, int y, int width, int length, boolean cacheFlag) 
	{
		IntCache.resetIntCache();
		if (listToReuse == null || listToReuse.length < width * length) 
		{
			listToReuse = new Biome[width * length];
		}
		
		if (cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (y & 15) == 0) 
		{
			Biome[] aBiome1 = this.biomeCache.getCachedBiomes(x, y);
			System.arraycopy(aBiome1, 0, listToReuse, 0, width * length);
			return listToReuse;
		} 
		
		else 
		{
			int[] aint = this.biomeIndexLayer.getInts(x, y, width, length);
			for (int i = 0; i < width * length; ++i) 
			{
				listToReuse[i] = Biome.getBiome(aint[i]);
			}
			return listToReuse;
		}
	}

	@Override
	public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
		IntCache.resetIntCache();
		int i = x - radius >> 2;
		int j = z - radius >> 2;
		int k = x + radius >> 2;
		int l = z + radius >> 2;
		int i1 = k - i + 1;
		int j1 = l - j + 1;
		int[] aint = this.genBiomes.getInts(i, j, i1, j1);
		try {
			for (int k1 = 0; k1 < i1 * j1; ++k1) {
				Biome biome = Biome.getBiome(aint[k1]);
				if (!allowed.contains(biome)) {
					return false;
				}
			}
			return true;
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Layer");
			crashreportcategory.addCrashSection("Layer", this.genBiomes.toString());
			crashreportcategory.addCrashSection("x", x);
			crashreportcategory.addCrashSection("z", z);
			crashreportcategory.addCrashSection("radius", radius);
			crashreportcategory.addCrashSection("allowed", allowed);
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public BlockPos findBiomePosition(int x, int z, int range, @SuppressWarnings("rawtypes") List biomes, Random random) {
		IntCache.resetIntCache();
		int l = x - range >> 2;
		int i1 = z - range >> 2;
		int j1 = x + range >> 2;
		int k1 = z + range >> 2;
		int l1 = j1 - l + 1;
		int i2 = k1 - i1 + 1;
		int[] aint = this.genBiomes.getInts(l, i1, l1, i2);
		BlockPos blockpos = null;
		int j2 = 0;
		for (int k2 = 0; k2 < l1 * i2; ++k2) {
			int l2 = l + k2 % l1 << 2;
			int i3 = i1 + k2 / l1 << 2;
			Biome biome = Biome.getBiome(aint[k2]);
			if (biomes.contains(biome) && (blockpos == null || random.nextInt(j2 + 1) == 0)) {
				blockpos = new BlockPos(l2, 0, i3);
				++j2;
			}
		}
		return blockpos;
	}

	@Override
	public void cleanupCache() {
		this.biomeCache.cleanupCache();
	}
}