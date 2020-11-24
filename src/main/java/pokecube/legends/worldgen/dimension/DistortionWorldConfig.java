package pokecube.legends.worldgen.dimension;

import java.util.Set;
import java.util.function.LongFunction;
import java.util.HashSet;
import java.util.Arrays;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.EndGenerationSettings;
import net.minecraft.world.gen.IExtendedNoiseRandom;
import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.LazyAreaLayerContext;
import net.minecraft.world.gen.area.IAreaFactory;
import net.minecraft.world.gen.area.LazyArea;
import net.minecraft.world.gen.carver.CaveWorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.layer.IslandLayer;
import net.minecraft.world.gen.layer.Layer;
import net.minecraft.world.gen.layer.ZoomLayer;
import net.minecraft.world.gen.layer.traits.IC0Transformer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.world.gen.GenerationStage;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.init.BlockInit;

public class DistortionWorldConfig
{
    private static Set<Biome> dimensionBiomes;
    private static Biome[]    biomeArr;

    public void init(final FMLCommonSetupEvent event)
    {
        DistortionWorldConfig.dimensionBiomes = Sets.newHashSet(
                ForgeRegistries.BIOMES.getValue(new ResourceLocation("pokecube_legends:distortic_world")));
        DistortionWorldConfig.biomeArr = DistortionWorldConfig.dimensionBiomes.toArray(new Biome[0]);
    }

    public static class DistortedBiomeProvider extends BiomeProvider
    {
        private final Layer                 genBiomes;

        public DistortedBiomeProvider(final World world)
        {
        	super(new HashSet<Biome>(Arrays.asList(DistortionWorldConfig.biomeArr)));
			this.genBiomes = getBiomeLayer(world.getSeed());
            for (Biome biome : this.biomes) {
				biome.addCarver(GenerationStage.Carving.AIR, Biome.createCarver(new CaveWorldCarver(ProbabilityConfig::deserialize, 256) {
					{
						carvableBlocks = ImmutableSet.of(BlockInit.DISTORTIC_STONE.get().getDefaultState().getBlock(),
								biome.getSurfaceBuilder().getConfig().getTop().getBlock(),
								biome.getSurfaceBuilder().getConfig().getUnder().getBlock());
					}
				}, new ProbabilityConfig(0.14285715f)));
			}

        }

        private Layer getBiomeLayer(long seed) {
			LongFunction<IExtendedNoiseRandom<LazyArea>> contextFactory = l -> new LazyAreaLayerContext(25, seed, l);
			IAreaFactory<LazyArea> parentLayer = IslandLayer.INSTANCE.apply(contextFactory.apply(1));
			IAreaFactory<LazyArea> biomeLayer = (new DistortedBiomeLayer()).apply(contextFactory.apply(200), parentLayer);
			biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1000), biomeLayer);
			biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1001), biomeLayer);
			biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1002), biomeLayer);
			biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1003), biomeLayer);
			biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1004), biomeLayer);
			biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1005), biomeLayer);
			return new Layer(biomeLayer);
		}

        @Override
        public Biome getNoiseBiome(final int x, final int y, final int z)
        {
            return this.genBiomes.func_215738_a(x, z);
        }
    }

    public static class DistortedBiomeLayer implements IC0Transformer
    {

        @SuppressWarnings("deprecation")
        @Override
        public int apply(final INoiseRandom context, final int value)
        {
            return Registry.BIOME.getId(DistortionWorldConfig.biomeArr[context.random(DistortionWorldConfig.biomeArr.length)]);
        }
    }

    public static class DistortedChunkGenerator extends DistorticWorldChunkGenerator
    {
        public DistortedChunkGenerator(final IWorld world, final BiomeProvider provider)
        {
            super(world, provider, new EndGenerationSettings()
            {
                @Override
                public BlockState getDefaultBlock()
                {
                    return BlockInit.DISTORTIC_STONE.get().getDefaultState();
                }

                @Override
                public BlockState getDefaultFluid()
                {
                    return BlockInit.DISTORTIC_MIRROR.get().getDefaultState();
                }
            });
            this.randomSeed.skip(5349);
        }
    }

    public static class DistortedDimension extends Dimension
    {

        public DistortedDimension(final World world, final DimensionType type)
        {
            // This 0.0f is whatever was used in the light table before!
            super(world, type, 0.5f);
            this.nether = false;
        }

        @Override
        public ChunkGenerator<?> createChunkGenerator()
        {
            return new DistortedChunkGenerator(this.world, new DistortedBiomeProvider(this.world));
        }

        @Override
        public BlockPos findSpawn(final ChunkPos chunkPosIn, final boolean checkValid)
        {
            return null;
        }

        @Override
        public BlockPos findSpawn(final int posX, final int posZ, final boolean checkValid)
        {
            return null;
        }

        @Override
        public float calculateCelestialAngle(long worldTime, float partialTicks) {
			double d0 = MathHelper.frac((double) worldTime / 24000.0D - 0.25D);
			double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
			return (float) (d0 * 2.0D + d1) / 3.0F;
		}

        @Override
        public boolean isSurfaceWorld()
        {
            return false;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public Vec3d getFogColor(final float cangle, final float ticks)
        {
            return new Vec3d(0, 0, 0.8);
        }

        @Override
        public boolean canRespawnHere()
        {
            return true;
        }

        @Override
		public boolean doesWaterVaporize() {
			return false;
		}
        
        @Override
		public SleepResult canSleepAt(final PlayerEntity player, final BlockPos pos) {
			return SleepResult.BED_EXPLODES;
		}

        @OnlyIn(Dist.CLIENT)
        @Override
        public boolean doesXZShowFog(final int x, final int z)
        {
            return false;
        }
    }
}
