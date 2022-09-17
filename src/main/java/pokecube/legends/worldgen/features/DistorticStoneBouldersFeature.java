package pokecube.legends.worldgen.features;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.FluidInit;

public class DistorticStoneBouldersFeature extends Feature<ColumnFeatureConfiguration>
{
    private static final ImmutableList<Block> CAN_PLACE_ON      = ImmutableList.of(BlockInit.DISTORTIC_GRASS_BLOCK.get(),
            BlockInit.DISTORTIC_STONE.get(), BlockInit.ULTRA_STONE.get(), FluidInit.DISTORTIC_WATER_BLOCK.get());
    private static final int                  CLUSTERED_REACH   = 5;
    private static final int                  CLUSTERED_SIZE    = 50;
    private static final int                  UNCLUSTERED_REACH = 8;
    private static final int                  UNCLUSTERED_SIZE  = 15;

    public DistorticStoneBouldersFeature(Codec<ColumnFeatureConfiguration> config)
    {
        super(config);
    }

    @Override
    public boolean place(final FeaturePlaceContext<ColumnFeatureConfiguration> context)
    {
        final int i = context.chunkGenerator().getSeaLevel();
        final BlockPos pos = context.origin();
        final WorldGenLevel world = context.level();
        final RandomSource random = context.random();
        final ColumnFeatureConfiguration conlumnConfig = context.config();
        if (!DistorticStoneBouldersFeature.canPlaceAt(world, i, pos.mutable())) return false;
        else
        {
            final int j = conlumnConfig.height().sample(random);
            final boolean flag = random.nextFloat() < 0.9F;
            final int k = Math.min(j, flag ? DistorticStoneBouldersFeature.CLUSTERED_REACH
                    : DistorticStoneBouldersFeature.UNCLUSTERED_REACH);
            final int l = flag ? DistorticStoneBouldersFeature.CLUSTERED_SIZE
                    : DistorticStoneBouldersFeature.UNCLUSTERED_SIZE;
            boolean flag1 = false;

            for (final BlockPos pos1 : BlockPos.randomBetweenClosed(random, l, pos.getX() - k, pos.getY(), pos.getZ()
                    - k, pos.getX() + k, pos.getY(), pos.getZ() + k))
            {
                final int i1 = j - pos1.distManhattan(pos);
                if (i1 >= 0) flag1 |= this.placeColumn(world, i, pos1, i1, conlumnConfig.reach().sample(random));
            }

            return flag1;
        }
    }

    public boolean placeColumn(final LevelAccessor world, final int a, final BlockPos pos, final int height,
            final int reach)
    {
        boolean flag = false;

        for (final BlockPos pos1 : BlockPos.betweenClosed(pos.getX() - reach, pos.getY(), pos.getZ() - reach, pos.getX()
                + reach, pos.getY(), pos.getZ() + reach))
        {
            final int i = pos1.distManhattan(pos);
            final BlockPos pos2 = DistorticStoneBouldersFeature.isAirOrLavaOcean(world, a, pos1)
                    ? DistorticStoneBouldersFeature.findSurface(world, a, pos1.mutable(), i)
                    : DistorticStoneBouldersFeature.findAir(world, pos1.mutable(), i);
            if (pos2 != null)
            {
                int j = height - i / 2;

                for (final BlockPos.MutableBlockPos mutablePos = pos2.mutable(); j >= 0; --j)
                    if (DistorticStoneBouldersFeature.isAirOrLavaOcean(world, a, mutablePos))
                    {
                        this.setBlock(world, mutablePos, BlockInit.DISTORTIC_STONE.get().defaultBlockState());
                        mutablePos.move(Direction.UP);
                        flag = true;
                    }
                    else
                    {
                        if (!world.getBlockState(mutablePos).is(BlockInit.DISTORTIC_STONE.get())) break;
                        mutablePos.move(Direction.UP);
                    }
            }
        }
        return flag;
    }

    @Nullable
    public static BlockPos findSurface(final LevelAccessor world, final int y, final BlockPos.MutableBlockPos pos,
            int height)
    {
        while (pos.getY() > world.getMinBuildHeight() + 1 && height > 0)
        {
            --height;
            if (DistorticStoneBouldersFeature.canPlaceAt(world, y, pos)) return pos;
            pos.move(Direction.DOWN);
        }
        return null;
    }

    public static boolean canPlaceAt(final LevelAccessor world, final int y, final BlockPos.MutableBlockPos pos)
    {
        if (!DistorticStoneBouldersFeature.isAirOrLavaOcean(world, y, pos)) return false;
        else
        {
            final BlockState state = world.getBlockState(pos.move(Direction.DOWN));
            pos.move(Direction.UP);
            return !state.isAir() && DistorticStoneBouldersFeature.CAN_PLACE_ON.contains(state.getBlock());
        }
    }

    @Nullable
    public static BlockPos findAir(final LevelAccessor world, final BlockPos.MutableBlockPos pos, int height)
    {
        while (pos.getY() < world.getMaxBuildHeight() && height > 0)
        {
            --height;
            final BlockState state = world.getBlockState(pos);
            if (!DistorticStoneBouldersFeature.CAN_PLACE_ON.contains(state.getBlock())) return null;

            if (state.isAir()) return pos;
            pos.move(Direction.UP);
        }
        return null;
    }

    public static boolean isAirOrLavaOcean(final LevelAccessor world, final int height, final BlockPos pos)
    {
        final BlockState state = world.getBlockState(pos);
        return state.isAir() || state.is(Blocks.LAVA) && pos.getY() <= height;
    }
}