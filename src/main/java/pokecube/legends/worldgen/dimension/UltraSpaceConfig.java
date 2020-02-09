package pokecube.legends.worldgen.dimension;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.LongFunction;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
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
import net.minecraft.world.gen.IExtendedNoiseRandom;
import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.LazyAreaLayerContext;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.gen.OverworldGenSettings;
import net.minecraft.world.gen.SimplexNoiseGenerator;
import net.minecraft.world.gen.area.IAreaFactory;
import net.minecraft.world.gen.area.LazyArea;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.layer.IslandLayer;
import net.minecraft.world.gen.layer.Layer;
import net.minecraft.world.gen.layer.VoroniZoomLayer;
import net.minecraft.world.gen.layer.ZoomLayer;
import net.minecraft.world.gen.layer.traits.IC0Transformer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.init.BlockInit;

public class UltraSpaceConfig
{

    private static Biome[] dimensionBiomes;

    public void init(final FMLCommonSetupEvent event)
    {
        UltraSpaceConfig.dimensionBiomes = new Biome[] { ForgeRegistries.BIOMES.getValue(new ResourceLocation(
                "pokecube_legends:ub001")), ForgeRegistries.BIOMES.getValue(new ResourceLocation(
                        "pokecube_legends:ub002")), ForgeRegistries.BIOMES.getValue(new ResourceLocation(
                                "pokecube_legends:ub003")), ForgeRegistries.BIOMES.getValue(new ResourceLocation(
                                        "pokecube_legends:ub004")), };

    }

    public static class UltraSpaceBiomeProvider extends BiomeProvider
    {
        private final Layer                 genBiomes;
        private final Layer                 biomeFactoryLayer;
        private final Biome[]               biomes;
        private final SimplexNoiseGenerator generator;

        public UltraSpaceBiomeProvider(final World world)
        {
            final Layer[] aLayer = this.makeTheWorld(world.getSeed());
            this.genBiomes = aLayer[0];
            this.biomeFactoryLayer = aLayer[1];
            this.biomes = UltraSpaceConfig.dimensionBiomes;
            this.generator = new SimplexNoiseGenerator(new SharedSeedRandom(world.getSeed()));
        }

        private Layer[] makeTheWorld(final long seed)
        {
            final LongFunction<IExtendedNoiseRandom<LazyArea>> contextFactory = l -> new LazyAreaLayerContext(25, seed,
                    l);
            final IAreaFactory<LazyArea> parentLayer = IslandLayer.INSTANCE.apply(contextFactory.apply(1));
            IAreaFactory<LazyArea> biomeLayer = new UltraSpaceBiomeLayer().apply(contextFactory.apply(200),
                    parentLayer);
            biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1000), biomeLayer);
            biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1001), biomeLayer);
            biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1002), biomeLayer);
            biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1003), biomeLayer);
            biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1004), biomeLayer);
            biomeLayer = ZoomLayer.NORMAL.apply(contextFactory.apply(1005), biomeLayer);
            final IAreaFactory<LazyArea> voronoizoom = VoroniZoomLayer.INSTANCE.apply(contextFactory.apply(10),
                    biomeLayer);
            return new Layer[] { new Layer(biomeLayer), new Layer(voronoizoom) };
        }

        // @Override
        /**
         * Gets the biome from the provided coordinates
         */
        @Override
        public Biome getBiome(final int x, final int y)
        {
            return this.biomeFactoryLayer.func_215738_a(x, y);
        }

        @Override
        public Biome getBiomeAtFactorFour(final int p_222366_1_, final int p_222366_2_)
        {
            return this.genBiomes.func_215738_a(p_222366_1_, p_222366_2_);
        }

        @Override
        public Biome[] getBiomes(final int x, final int z, final int width, final int length, final boolean cacheFlag)
        {
            return this.biomeFactoryLayer.generateBiomes(x, z, width, length);
        }

        @Override
        public Set<Biome> getBiomesInSquare(final int centerX, final int centerZ, final int sideLength)
        {
            final int i = centerX - sideLength >> 2;
            final int j = centerZ - sideLength >> 2;
            final int k = centerX + sideLength >> 2;
            final int l = centerZ + sideLength >> 2;
            final int i1 = k - i + 1;
            final int j1 = l - j + 1;
            final Set<Biome> set = Sets.newHashSet();
            Collections.addAll(set, this.genBiomes.generateBiomes(i, j, i1, j1));
            return set;
        }

        @Override
        @Nullable
        public BlockPos findBiomePosition(final int x, final int z, final int range, final List<Biome> biomes,
                final Random random)
        {
            final int i = x - range >> 2;
            final int j = z - range >> 2;
            final int k = x + range >> 2;
            final int l = z + range >> 2;
            final int i1 = k - i + 1;
            final int j1 = l - j + 1;
            final Biome[] abiome = this.genBiomes.generateBiomes(i, j, i1, j1);
            BlockPos blockpos = null;
            int k1 = 0;
            for (int l1 = 0; l1 < i1 * j1; ++l1)
            {
                final int i2 = i + l1 % i1 << 2;
                final int j2 = j + l1 / i1 << 2;
                if (biomes.contains(abiome[l1]))
                {
                    if (blockpos == null || random.nextInt(k1 + 1) == 0) blockpos = new BlockPos(i2, 0, j2);
                    ++k1;
                }
            }
            return blockpos;
        }

        @Override
        public boolean hasStructure(final Structure<?> structureIn)
        {
            return this.hasStructureCache.computeIfAbsent(structureIn, (p_205006_1_) ->
            {
                for (final Biome biome : this.biomes)
                    if (biome.hasStructure(p_205006_1_)) return true;
                return false;
            });
        }

        @Override
        public Set<BlockState> getSurfaceBlocks()
        {
            if (this.topBlocksCache.isEmpty()) for (final Biome biome : this.biomes)
                this.topBlocksCache.add(biome.getSurfaceBuilderConfig().getTop());
            return this.topBlocksCache;
        }

        @Override
        public float func_222365_c(final int p_222365_1_, final int p_222365_2_)
        {
            final int i = p_222365_1_ / 2;
            final int j = p_222365_2_ / 2;
            final int k = p_222365_1_ % 2;
            final int l = p_222365_2_ % 2;
            float f = 100.0F - MathHelper.sqrt(p_222365_1_ * p_222365_1_ + p_222365_2_ * p_222365_2_) * 8.0F;
            f = MathHelper.clamp(f, -100.0F, 80.0F);
            for (int i1 = -12; i1 <= 12; ++i1)
                for (int j1 = -12; j1 <= 12; ++j1)
                {
                    final long k1 = i + i1;
                    final long l1 = j + j1;
                    if (k1 * k1 + l1 * l1 > 4096L && this.generator.getValue(k1, l1) < -0.9F)
                    {
                        final float f1 = (MathHelper.abs(k1) * 3439.0F + MathHelper.abs(l1) * 147.0F) % 13.0F + 9.0F;
                        final float f2 = k - i1 * 2;
                        final float f3 = l - j1 * 2;
                        float f4 = 100.0F - MathHelper.sqrt(f2 * f2 + f3 * f3) * f1;
                        f4 = MathHelper.clamp(f4, -100.0F, 80.0F);
                        f = Math.max(f, f4);
                    }
                }
            return f;
        }
    }

    public static class UltraSpaceBiomeLayer implements IC0Transformer
    {

        @SuppressWarnings("deprecation")
        @Override
        public int apply(final INoiseRandom context, final int value)
        {
            return Registry.BIOME.getId(UltraSpaceConfig.dimensionBiomes[context.random(
                    UltraSpaceConfig.dimensionBiomes.length)]);
        }
    }

    public static class UltraSpaceChunkGenerator extends OverworldChunkGenerator
    {
        // private static final int SEALEVEL = 63;

        public UltraSpaceChunkGenerator(final IWorld world, final BiomeProvider provider)
        {
            super(world, provider, new OverworldGenSettings()
            {
                @Override
                public BlockState getDefaultBlock()
                {
                    return BlockInit.ULTRA_STONE.getDefaultState();
                }

                @Override
                public BlockState getDefaultFluid()
                {
                    return Blocks.WATER.getDefaultState();
                }
            });
            this.randomSeed.skip(5349);
        }
    }

    public static class UltraSpaceDimension extends Dimension
    {

        public UltraSpaceDimension(final World world, final DimensionType type)
        {
            super(world, type);
            this.nether = false;
        }

        @Override
        public ChunkGenerator<?> createChunkGenerator()
        {
            return new UltraSpaceChunkGenerator(this.world, new UltraSpaceBiomeProvider(this.world));
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
        public float calculateCelestialAngle(final long worldTime, final float partialTicks)
        {
            final double d0 = MathHelper.frac(worldTime / 24000.0D - 0.25D);
            final double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
            return (float) (d0 * 2.0D + d1) / 3.0F;
        }

        @Override
        protected void generateLightBrightnessTable()
        {
            final float f = 0.5f;
            for (int i = 0; i <= 15; ++i)
            {
                final float f1 = 1 - i / 15f;
                this.lightBrightnessTable[i] = (1 - f1) / (f1 * 3 + 1) * (1 - f) + f;
            }
        }

        @Override
        public boolean isSurfaceWorld()
        {
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public Vec3d getFogColor(final float cangle, final float ticks)
        {
            return new Vec3d(0, 0, 0);
        }

        @Override
        public boolean canRespawnHere()
        {
            return true;
        }

        @Override
        public SleepResult canSleepAt(final PlayerEntity player, final BlockPos pos)
        {
            return SleepResult.ALLOW;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public boolean doesXZShowFog(final int x, final int z)
        {
            return true;
        }
    }
}
