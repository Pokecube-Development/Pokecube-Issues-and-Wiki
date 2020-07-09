package pokecube.legends.worldgen.dimension;

import java.util.Set;
import java.util.function.LongFunction;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.DimensionType;
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
import net.minecraft.world.gen.layer.ZoomLayer;
import net.minecraft.world.gen.layer.traits.IC0Transformer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.init.BlockInit;

public class UltraSpaceConfig
{

    private static Set<Biome> dimensionBiomes;
    private static Biome[]    biomeArr;

    public void init(final FMLCommonSetupEvent event)
    {
        UltraSpaceConfig.dimensionBiomes = Sets.newHashSet(
                ForgeRegistries.BIOMES.getValue(new ResourceLocation("pokecube_legends:ub001")),
                ForgeRegistries.BIOMES.getValue(new ResourceLocation("pokecube_legends:ub002")),
                ForgeRegistries.BIOMES.getValue(new ResourceLocation("pokecube_legends:ub003")),
                ForgeRegistries.BIOMES.getValue(new ResourceLocation("pokecube_legends:ub004")));
        UltraSpaceConfig.biomeArr = UltraSpaceConfig.dimensionBiomes.toArray(new Biome[0]);
    }

    public static class UltraSpaceBiomeProvider extends BiomeProvider
    {
        private final Layer                 genBiomes;
        private final SimplexNoiseGenerator generator;

        public UltraSpaceBiomeProvider(final World world)
        {
            super(UltraSpaceConfig.dimensionBiomes);
            final Layer[] aLayer = this.makeTheWorld(world.getSeed());
            this.genBiomes = aLayer[0];
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
            return new Layer[] { new Layer(biomeLayer) };
        }

        @Override
        public Biome getNoiseBiome(final int x, final int y, final int z)
        {
            return this.genBiomes.func_215738_a(x, z);
        }

        @Override
        public boolean hasStructure(final Structure<?> structureIn)
        {
            return this.hasStructureCache.computeIfAbsent(structureIn, (p_205006_1_) ->
            {
                for (final Biome biome : UltraSpaceConfig.dimensionBiomes)
                    if (biome.hasStructure(p_205006_1_)) return true;
                return false;
            });
        }

        @Override
        public Set<BlockState> getSurfaceBlocks()
        {
            if (this.topBlocksCache.isEmpty()) for (final Biome biome : UltraSpaceConfig.dimensionBiomes)
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
            return Registry.BIOME.getId(UltraSpaceConfig.biomeArr[context.random(UltraSpaceConfig.biomeArr.length)]);
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
                    return BlockInit.ULTRA_STONE.get().getDefaultState();
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
            // This 0.0f is whatever was used in the light table before!
            super(world, type, 0.0f);
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
        public boolean isSurfaceWorld()
        {
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public Vector3d getFogColor(final float cangle, final float ticks)
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
