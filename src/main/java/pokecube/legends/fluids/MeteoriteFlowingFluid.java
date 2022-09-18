package pokecube.legends.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.FluidInit;

public class MeteoriteFlowingFluid extends ForgeFlowingFluid.Flowing
{

    public MeteoriteFlowingFluid(Properties properties)
    {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(LEVEL, 2));
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
    {
        super.createFluidStateDefinition(builder);
    }

    public boolean isSame(Fluid fluid)
    {
        return fluid == FluidInit.MOLTEN_METEORITE.get() || fluid == FluidInit.MOLTEN_METEORITE_FLOWING.get();
    }

    @Override
    protected float getExplosionResistance()
    {
        return 0.0F;
    }

    @Override
    public int getSlopeFindDistance(LevelReader world)
    {
        return world.dimensionType().ultraWarm() ? 4 : 2;
    }

    @Override
    public int getDropOff(LevelReader world)
    {
        return world.dimensionType().ultraWarm() ? 1 : 1;
    }

    @Override
    public int getTickDelay(LevelReader p_76226_)
    {
        return p_76226_.dimensionType().ultraWarm() ? 20 : 60;
    }

    @Override
    public int getSpreadDelay(Level world, BlockPos pos, FluidState fluidState, FluidState fluidState1)
    {
        int i = this.getTickDelay(world);
        if (!fluidState.isEmpty() && !fluidState1.isEmpty() && !fluidState.getValue(FALLING) && !fluidState1.getValue(FALLING)
                && fluidState1.getHeight(world, pos) > fluidState.getHeight(world, pos) && world.getRandom().nextInt(4) != 0)
        {
            i *= 10;
        }
        return i;
    }

    @Override
    public void animateTick(Level world, BlockPos pos, FluidState fluidState, RandomSource random)
    {
        BlockPos posAbove = pos.above();
        if (world.getBlockState(posAbove).isAir() && !world.getBlockState(posAbove).isSolidRender(world, posAbove))
        {
            if (random.nextInt(100) == 0)
            {
                double d0 = (double)pos.getX() + random.nextDouble();
                double d1 = (double)pos.getY() + 1.0D;
                double d2 = (double)pos.getZ() + random.nextDouble();
                world.addParticle(ParticleTypes.LAVA, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                world.playLocalSound(d0, d1, d2, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                        0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }

            if (random.nextInt(200) == 0)
            {
                world.playLocalSound((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS,
                        0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }
        }
    }

    @Override
    protected boolean isRandomlyTicking()
    {
        return true;
    }

    @Override
    public void randomTick(Level world, BlockPos pos, FluidState fluidState, RandomSource random)
    {
        if (world.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK))
        {
            int i = random.nextInt(3);
            if (i > 0)
            {
                BlockPos posOffset = pos;

                for(int j = 0; j < i; ++j)
                {
                    posOffset = posOffset.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
                    if (!world.isLoaded(posOffset))
                    {
                        return;
                    }

                    BlockState state = world.getBlockState(posOffset);
                    if (state.isAir())
                    {
                        if (this.hasFlammableNeighbours(world, posOffset))
                        {
                            world.setBlockAndUpdate(posOffset, ForgeEventFactory.fireFluidPlaceBlockEvent(world, posOffset, pos, Blocks.FIRE.defaultBlockState()));
                            return;
                        }
                    } else if (state.getMaterial().blocksMotion())
                    {
                        return;
                    }
                }
            } else
            {
                for(int k = 0; k < 3; ++k)
                {
                    BlockPos posOffset = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
                    if (!world.isLoaded(posOffset))
                    {
                        return;
                    }

                    if (world.isEmptyBlock(posOffset.above()) && this.isFlammable(world, posOffset, Direction.UP))
                    {
                        world.setBlockAndUpdate(posOffset.above(), ForgeEventFactory.fireFluidPlaceBlockEvent(world, posOffset.above(), pos, Blocks.FIRE.defaultBlockState()));
                    }
                }
            }
        }
    }

    public boolean hasFlammableNeighbours(LevelReader world, BlockPos pos)
    {
        for(Direction direction : Direction.values())
        {
            if (this.isFlammable(world, pos.relative(direction), direction.getOpposite()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isFlammable(LevelReader level, BlockPos pos, Direction face)
    {
        return pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight() && !level.hasChunkAt(pos) ? false : level.getBlockState(pos).isFlammable(level, pos, face);
    }

    public void fizz(LevelAccessor world, BlockPos pos)
    {
        world.levelEvent(1501, pos, 0);
    }

    public void spreadTo(LevelAccessor world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState)
    {
        if (direction == Direction.DOWN)
        {
            FluidState fluidStatePos = world.getFluidState(pos);
            if (this.is(FluidTags.LAVA) && fluidStatePos.is(FluidTags.WATER))
            {
                if (state.getBlock() instanceof LiquidBlock)
                {
                    world.setBlock(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(world, pos, pos, BlockInit.ULTRA_DARKSTONE.get().defaultBlockState()), 3);
                }
                this.fizz(world, pos);
                return;
            }
        }
        super.spreadTo(world, pos, state, direction, fluidState);
    }
}