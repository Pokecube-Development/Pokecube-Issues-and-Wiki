package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import pokecube.legends.blocks.FallingDirtBlockBase;

public class AshBlock extends FallingDirtBlockBase implements Fallable
{
    public static final BooleanProperty WET = BooleanProperty.create("wet");

    public AshBlock(final int color, final Properties properties)
    {
        super(color, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WET, false));
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, Random random)
    {
        if (isNearWater(world, pos) || world.isRainingAt(pos.above()))
        {
            world.setBlock(pos, state.setValue(WET, true), 2);
        }

        if (isFree(world.getBlockState(pos.below())) && pos.getY() >= world.getMinBuildHeight()
                && state.getValue(WET) == false)
        {
            FallingBlockEntity entity = FallingBlockEntity.fall(world, pos, world.getBlockState(pos));
            this.falling(entity);
            world.addFreshEntity(entity);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random)
    {
        if (!isNearWater(world, pos) && !world.isRainingAt(pos.above()))
        {
            world.setBlock(pos, state.setValue(WET, false), 2);
        }

        if (world.isRainingAt(pos.above()))
        {
            world.setBlock(pos, state.setValue(WET, true), 2);
        }
    }

    public static boolean isNearWater(LevelReader world, BlockPos pos)
    {
        if (world.getFluidState(pos.above()).is(FluidTags.WATER) || world.getFluidState(pos.below()).is(FluidTags.WATER)
                || world.getFluidState(pos.north()).is(FluidTags.WATER)
                || world.getFluidState(pos.south()).is(FluidTags.WATER)
                || world.getFluidState(pos.east()).is(FluidTags.WATER)
                || world.getFluidState(pos.west()).is(FluidTags.WATER))
        {
            return true;
        }
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random random)
    {
        if (random.nextInt(16) == 0 && state.getValue(WET) == false)
        {
            BlockPos posBelow = pos.below();
            if (isFree(world.getBlockState(posBelow)))
            {
                double d0 = (double) pos.getX() + random.nextDouble();
                double d1 = (double) pos.getY() - 0.05D;
                double d2 = (double) pos.getZ() + random.nextDouble();
                world.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), d0, d1, d2, 0.0D, 0.0D,
                        0.0D);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(WET);
    }
}
