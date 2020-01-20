package pokecube.core.world.dimension;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;

import com.google.common.collect.Sets;

import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SecretBaseDimension extends ModDimension
{

    public static class SecretBiome extends Biome
    {

        protected SecretBiome()
        {
            super(new Biome.Builder().surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG)
                    .precipitation(Biome.RainType.RAIN).category(Biome.Category.PLAINS).depth(0.125F).scale(0.05F)
                    .temperature(0.8F).downfall(0.4F).waterColor(4159204).waterFogColor(329011).parent((String) null));
        }

    }

    public static class SecretChunkGen extends ChunkGenerator<SecretSettings>
    {

        public SecretChunkGen(final IWorld w, final BiomeProvider bp, final SecretSettings gs)
        {
            super(w, bp, gs);
        }

        @Override
        public int func_222529_a(final int arg0, final int arg1, final Type hmtype)
        {
            return 15;
        }

        @Override
        public void generateSurface(final IChunk chunk)
        {

        }

        @Override
        public int getGroundHeight()
        {
            return 64;
        }

        @Override
        public void makeBase(final IWorld world, final IChunk chunk)
        {
            final ChunkPos pos = chunk.getPos();
            if (pos.x % 32 == 0 && pos.z % 32 == 0)
            {
                final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
                final Heightmap heightmap = chunk.func_217303_b(Heightmap.Type.OCEAN_FLOOR_WG);
                final Heightmap heightmap1 = chunk.func_217303_b(Heightmap.Type.WORLD_SURFACE_WG);
                BlockState state = Blocks.STONE.getDefaultState();
                for (int i = 58; i < 64; ++i)
                    for (int j = 4; j < 12; ++j)
                        for (int k = 4; k < 12; ++k)
                        {
                            chunk.setBlockState(blockpos$mutableblockpos.setPos(j, i, k), state, false);
                            heightmap.update(j, i, k, state);
                            heightmap1.update(j, i, k, state);
                        }
                state = Blocks.BARRIER.getDefaultState();
                for (int j = 0; j < 16; ++j)
                    for (int k = 0; k < 16; ++k)
                    {
                        chunk.setBlockState(blockpos$mutableblockpos.setPos(j, 0, k), state, false);
                        chunk.setBlockState(blockpos$mutableblockpos.setPos(j, world.getMaxHeight() - 1, k), state,
                                false);
                        chunk.setBlockState(blockpos$mutableblockpos.setPos(j, world.getMaxHeight() - 2, k), state,
                                false);
                        chunk.setBlockState(blockpos$mutableblockpos.setPos(j, world.getMaxHeight() - 3, k), state,
                                false);
                        chunk.setBlockState(blockpos$mutableblockpos.setPos(j, world.getMaxHeight() - 4, k), state,
                                false);
                        chunk.setBlockState(blockpos$mutableblockpos.setPos(j, world.getMaxHeight() - 5, k), state,
                                false);
                    }
            }
            else
            {
                final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
                final BlockState state = Blocks.BARRIER.getDefaultState();
                for (int i = 0; i < world.getMaxHeight(); ++i)
                    for (int j = 0; j < 16; ++j)
                        for (int k = 0; k < 16; ++k)
                            chunk.setBlockState(blockpos$mutableblockpos.setPos(j, i, k), state, false);
            }
        }

    }

    public static class SecretDimension extends OverworldDimension
    {

        public SecretDimension(final World worldIn, final DimensionType typeIn)
        {
            super(worldIn, typeIn);
        }

        @Override
        public ChunkGenerator<?> createChunkGenerator()
        {
            return new SecretChunkGen(this.getWorld(), new SecretProvider(), new SecretSettings());
        }
    }

    public static class SecretProvider extends BiomeProvider
    {
        Set<BlockState> blocks   = Sets.newHashSet(Blocks.STONE.getDefaultState());
        Set<Biome>      biomes   = Sets.newHashSet(SecretBaseDimension.BIOME);
        Biome[]         biomeArr = new Biome[256];

        public SecretProvider()
        {
            Arrays.fill(this.biomeArr, SecretBaseDimension.BIOME);
        }

        @Override
        public BlockPos findBiomePosition(final int arg0, final int arg1, final int arg2, final List<Biome> arg3,
                final Random arg4)
        {
            return new BlockPos(arg0, arg1, arg2);
        }

        @Override
        public Biome getBiome(final int arg0, final int arg1)
        {
            return SecretBaseDimension.BIOME;
        }

        @Override
        public Biome[] getBiomes(final int arg0, final int arg1, final int arg2, final int arg3, final boolean arg4)
        {
            return this.biomeArr;
        }

        @Override
        public Set<Biome> getBiomesInSquare(final int arg0, final int arg1, final int arg2)
        {
            return this.biomes;
        }

        @Override
        public Set<BlockState> getSurfaceBlocks()
        {
            return this.blocks;
        }

        @Override
        public boolean hasStructure(final Structure<?> arg0)
        {
            return false;
        }

    }

    public static class SecretSettings extends GenerationSettings
    {

    }

    public static final SecretBaseDimension DIMENSION = new SecretBaseDimension();
    public static DimensionType             TYPE;
    public static final Biome               BIOME     = new SecretBiome();

    @SubscribeEvent
    public static void register(final RegisterDimensionsEvent event)
    {
        SecretBaseDimension.TYPE = DimensionManager.registerOrGetDimension(SecretBaseDimension.DIMENSION
                .getRegistryName(), SecretBaseDimension.DIMENSION, new PacketBuffer(Unpooled.EMPTY_BUFFER), true);
        DimensionManager.keepLoaded(SecretBaseDimension.TYPE);
    }

    @Override
    public BiFunction<World, DimensionType, ? extends Dimension> getFactory()
    {
        return (w, t) -> new SecretDimension(w, t);
    }

}
