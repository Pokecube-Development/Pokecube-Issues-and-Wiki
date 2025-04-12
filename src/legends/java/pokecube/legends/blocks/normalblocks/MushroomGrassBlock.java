package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ParticleInit;

public class MushroomGrassBlock extends GrassBlock implements BonemealableBlock
{
    public MushroomGrassBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SnowyDirtBlock.SNOWY, false));
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel world, final BlockPos pos, final RandomSource random)
    {
        if (!MushroomGrassBlock.canBeGrass(state, world, pos))
        {
            if (!world.isAreaLoaded(pos, 3))
                return;

            world.setBlockAndUpdate(pos, BlockInit.MUSHROOM_DIRT.get().defaultBlockState());
        } else if (world.getMaxLocalRawBrightness(pos.above()) >= 9)
        {
            final BlockState blockstate = this.defaultBlockState();

            for (int i = 0; i < 4; ++i)
            {
                final BlockPos blockpos = pos.offset(random.nextInt(3) - 1,
                        random.nextInt(5) - 3, random.nextInt(3) - 1);
                if (world.getBlockState(blockpos).is(BlockInit.MUSHROOM_DIRT.get()) &&
                        MushroomGrassBlock.canPropagate(blockstate, world, blockpos))
                    world.setBlockAndUpdate(blockpos, blockstate
                            .setValue(SnowyDirtBlock.SNOWY, world.getBlockState(blockpos.above()).is(Blocks.SNOW)));
            }
        }
    }

    public static boolean canPropagate(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        return MushroomGrassBlock.canBeGrass(state, world, pos) && !world.getFluidState(blockpos).is(FluidTags.WATER);
    }

    public static boolean canBeGrass(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        final BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowLayerBlock.LAYERS) >= 1)
            return true;
        else if (blockstate.getFluidState().getAmount() == 8)
            return false;
        else
        {
            final int i = LightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP,
                    blockstate.getLightBlock(world, blockpos));
            return i < world.getMaxLightLevel();
        }
    }

    @Override
    public void animateTick(final BlockState state, final Level world, final BlockPos pos, final RandomSource random)
    {
        super.animateTick(state, world, pos, random);
        if (random.nextInt(10) == 0)
            world.addParticle(ParticleInit.MUSHROOM.get(), pos.getX() + random.nextDouble(), pos.getY() + 1.1D, pos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
    }
}
