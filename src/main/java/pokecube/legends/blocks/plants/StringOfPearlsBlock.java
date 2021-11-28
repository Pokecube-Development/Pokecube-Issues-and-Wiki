package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.IForgeShearable;

public class StringOfPearlsBlock extends VineBlock implements BonemealableBlock, IForgeShearable
{
    public static final float           CHANCE_OF_FLOWERS_ON_GROWTH = 0.11F;
    public static final BooleanProperty FLOWERS                     = BooleanProperty.create("flowers");

    public StringOfPearlsBlock(final BlockBehaviour.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VineBlock.UP, Boolean.valueOf(false)).setValue(
                VineBlock.NORTH, Boolean.valueOf(false)).setValue(VineBlock.EAST, Boolean.valueOf(false)).setValue(
                        VineBlock.SOUTH, Boolean.valueOf(false)).setValue(VineBlock.WEST, Boolean.valueOf(false))
                .setValue(StringOfPearlsBlock.FLOWERS, Boolean.valueOf(false)));
    }

    public int getBlocksToGrowWhenBonemealed(final Random random)
    {
        return 1;
    }

    public boolean canGrowInto(final BlockState state)
    {
        return state.isAir();
    }

    @Override
    public boolean isValidBonemealTarget(final BlockGetter block, final BlockPos pos, final BlockState state,
            final boolean b)
    {
        return !state.getValue(StringOfPearlsBlock.FLOWERS);
    }

    @Override
    public boolean isBonemealSuccess(final Level world, final Random random, final BlockPos pos, final BlockState state)
    {
        return true;
    }

    @Override
    public void performBonemeal(final ServerLevel world, final Random random, final BlockPos pos,
            final BlockState state)
    {
        world.setBlock(pos, state.setValue(StringOfPearlsBlock.FLOWERS, Boolean.valueOf(true)), 2);
    }

    public boolean canSupportAtFace(final BlockGetter block, final BlockPos pos, final Direction direction)
    {
        if (direction == Direction.DOWN) return false;
        else
        {
            final BlockPos pos1 = pos.relative(direction);
            if (VineBlock.isAcceptableNeighbour(block, pos1, direction)) return true;
            else if (direction.getAxis() == Direction.Axis.Y) return false;
            else
            {
                final BooleanProperty b = VineBlock.PROPERTY_BY_DIRECTION.get(direction);
                final BlockState state = block.getBlockState(pos.above());
                return state.is(this) && state.getValue(b);
            }
        }
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel world, final BlockPos pos, final Random random)
    {
        if (world.random.nextInt(4) == 0 && world.isAreaLoaded(pos, 4))
        {
            final Direction direction = Direction.getRandom(random);
            final BlockPos pos1 = pos.above();
            if (direction.getAxis().isHorizontal() && !state.getValue(VineBlock.getPropertyForFace(direction)))
            {
                if (this.canSpread(world, pos))
                {
                    final BlockPos pos2 = pos.relative(direction);
                    final BlockState state1 = world.getBlockState(pos2);
                    if (state1.isAir())
                    {
                        final Direction direction3 = direction.getClockWise();
                        final Direction direction4 = direction.getCounterClockWise();
                        final boolean flag = state.getValue(VineBlock.getPropertyForFace(direction3));
                        final boolean flag1 = state.getValue(VineBlock.getPropertyForFace(direction4));
                        final BlockPos pos3 = pos2.relative(direction3);
                        final BlockPos pos4 = pos2.relative(direction4);
                        if (flag && VineBlock.isAcceptableNeighbour(world, pos3, direction3)) world.setBlock(pos2, this
                                .defaultBlockState().setValue(VineBlock.getPropertyForFace(direction3), Boolean.valueOf(
                                        true)).setValue(StringOfPearlsBlock.FLOWERS, Boolean.valueOf(random
                                                .nextFloat() < 0.11F)), 2);
                        else if (flag1 && VineBlock.isAcceptableNeighbour(world, pos4, direction4)) world.setBlock(pos2,
                                this.defaultBlockState().setValue(VineBlock.getPropertyForFace(direction4), Boolean
                                        .valueOf(true)).setValue(StringOfPearlsBlock.FLOWERS, Boolean.valueOf(random
                                                .nextFloat() < 0.11F)), 2);
                        else
                        {
                            final Direction direction1 = direction.getOpposite();
                            if (flag && world.isEmptyBlock(pos3) && VineBlock.isAcceptableNeighbour(world, pos.relative(
                                    direction3), direction1)) world.setBlock(pos3, this.defaultBlockState().setValue(
                                            VineBlock.getPropertyForFace(direction1), Boolean.valueOf(true)).setValue(
                                                    StringOfPearlsBlock.FLOWERS, Boolean.valueOf(random
                                                            .nextFloat() < 0.11F)), 2);
                            else if (flag1 && world.isEmptyBlock(pos4) && VineBlock.isAcceptableNeighbour(world, pos
                                    .relative(direction4), direction1)) world.setBlock(pos4, this.defaultBlockState()
                                            .setValue(VineBlock.getPropertyForFace(direction1), Boolean.valueOf(true))
                                            .setValue(StringOfPearlsBlock.FLOWERS, Boolean.valueOf(random
                                                    .nextFloat() < 0.11F)), 2);
                            else if (random.nextFloat() < 0.05D && VineBlock.isAcceptableNeighbour(world, pos2.above(),
                                    Direction.UP)) world.setBlock(pos2, this.defaultBlockState().setValue(VineBlock.UP,
                                            Boolean.valueOf(true)).setValue(StringOfPearlsBlock.FLOWERS, Boolean
                                                    .valueOf(random.nextFloat() < 0.11F)), 2);
                        }
                    }
                    else if (VineBlock.isAcceptableNeighbour(world, pos2, direction)) world.setBlock(pos, state
                            .setValue(VineBlock.getPropertyForFace(direction), Boolean.valueOf(true)).setValue(
                                    StringOfPearlsBlock.FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)), 2);

                }
            }
            else
            {
                if (direction == Direction.UP && pos.getY() < world.getMaxBuildHeight() - 1)
                {
                    if (this.canSupportAtFace(world, pos, direction))
                    {
                        world.setBlock(pos, state.setValue(VineBlock.UP, Boolean.valueOf(true)).setValue(
                                StringOfPearlsBlock.FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)), 2);
                        return;
                    }

                    if (world.isEmptyBlock(pos1))
                    {
                        if (!this.canSpread(world, pos)) return;

                        BlockState state2 = state;

                        for (final Direction direction2 : Direction.Plane.HORIZONTAL)
                            if (random.nextBoolean() || !VineBlock.isAcceptableNeighbour(world, pos1.relative(
                                    direction2), direction2)) state2 = state2.setValue(VineBlock.getPropertyForFace(
                                            direction2), Boolean.valueOf(false));

                        if (this.hasHorizontalConnection(state2)) world.setBlock(pos1, state2, 2);
                        return;
                    }
                }

                if (pos.getY() > world.getMinBuildHeight())
                {
                    final BlockPos pos5 = pos.below();
                    final BlockState state3 = world.getBlockState(pos5);
                    if (state3.isAir() || state3.is(this))
                    {
                        final BlockState state4 = state3.isAir() ? this.defaultBlockState() : state3;
                        final BlockState state5 = this.copyRandomFaces(state, state4, random);
                        if (state4 != state5 && this.hasHorizontalConnection(state5)) world.setBlock(pos5, state5, 2);
                    }
                }

            }
        }
    }

    public BlockState copyRandomFaces(final BlockState state, BlockState state1, final Random random)
    {
        for (final Direction direction : Direction.Plane.HORIZONTAL)
            if (random.nextBoolean())
            {
                final BooleanProperty b = VineBlock.getPropertyForFace(direction);
                if (state.getValue(b)) state1 = state1.setValue(b, Boolean.valueOf(true)).setValue(
                        StringOfPearlsBlock.FLOWERS, Boolean.valueOf(random
                                .nextFloat() < StringOfPearlsBlock.CHANCE_OF_FLOWERS_ON_GROWTH));
            }

        return state1;
    }

    public boolean hasHorizontalConnection(final BlockState state)
    {
        return state.getValue(VineBlock.NORTH) || state.getValue(VineBlock.EAST) || state.getValue(VineBlock.SOUTH)
                || state.getValue(VineBlock.WEST);
    }

    public boolean canSpread(final BlockGetter block, final BlockPos pos)
    {
        final int i = 4;
        final Iterable<BlockPos> iterable = BlockPos.betweenClosed(pos.getX() - i, pos.getY() - 1, pos.getZ() - i, pos
                .getX() + i, pos.getY() + 1, pos.getZ() + i);
        int j = 5;

        for (final BlockPos pos1 : iterable)
            if (block.getBlockState(pos1).is(this))
            {
                --j;
                if (j <= 0) return false;
            }
        return true;
    }

    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos, final Player player,
            final InteractionHand hand, final BlockHitResult blockHit)
    {
        if (state.getValue(StringOfPearlsBlock.FLOWERS))
        {
            Block.popResource(world, pos, new ItemStack(Items.PINK_DYE, 1));
            final float f = Mth.randomBetween(world.random, 0.8F, 1.2F);
            world.playSound((Player) null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, f);
            world.setBlock(pos, state.setValue(StringOfPearlsBlock.FLOWERS, Boolean.valueOf(false)), 2);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        else return InteractionResult.PASS;
    }

    @Override
    public void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(VineBlock.UP, VineBlock.NORTH, VineBlock.EAST, VineBlock.SOUTH, VineBlock.WEST,
                StringOfPearlsBlock.FLOWERS);
    }
}
