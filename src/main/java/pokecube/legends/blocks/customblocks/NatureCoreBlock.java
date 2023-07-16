package pokecube.legends.blocks.customblocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NatureCoreBlock extends Rotates implements SimpleWaterloggedBlock
{
    private static final EnumProperty<NatureCorePart> HALF        = EnumProperty.create("half", NatureCorePart.class);
    private static final BooleanProperty              WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty            FACING      = HorizontalDirectionalBlock.FACING;

    // Precise selection box
    private static final VoxelShape NATURE_CORE_TOP_NORTH =
            Block.box(2, 1, 6, 14, 14, 10);
    private static final VoxelShape NATURE_CORE_TOP_EAST  =
            Block.box(6, 1, 2, 10, 14, 14);
    private static final VoxelShape NATURE_CORE_TOP_SOUTH =
            Block.box(2, 1, 6, 14, 14, 10);
    private static final VoxelShape NATURE_CORE_TOP_WEST  =
            Block.box(6, 1, 2, 10, 14, 14);

    private static final VoxelShape NATURE_CORE_BOTTOM = Shapes.or(
            Block.box(0, 0, 4, 2, 2, 12),
            Block.box(0, 14, 4, 2, 16, 12),
            Block.box(2, 0, 4, 4, 4, 12),
            Block.box(2, 12, 4, 4, 16, 12),
            Block.box(4, 0, 0, 12, 2, 2),
            Block.box(4, 0, 12, 12, 4, 14),
            Block.box(4, 0, 14, 12, 2, 16),
            Block.box(4, 0, 2, 12, 4, 4),
            Block.box(4, 0, 4, 12, 16, 12),
            Block.box(4, 12, 2, 12, 16, 4),
            Block.box(4, 12, 12, 12, 16, 14),
            Block.box(4, 14, 0, 12, 16, 2),
            Block.box(4, 14, 14, 12, 16, 16),
            Block.box(12, 0, 4, 14, 4, 12),
            Block.box(14, 0, 4, 16, 2, 12),
            Block.box(12, 12, 4, 14, 16, 12),
            Block.box(14, 14, 4, 16, 16, 12)).optimize();

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        final NatureCorePart half = state.getValue(NatureCoreBlock.HALF);
        if (half == NatureCorePart.BOTTOM) return NatureCoreBlock.NATURE_CORE_BOTTOM;
        else // return NATURE_CORE_TOP.get(state.get(FACING));
            switch (state.getValue(NatureCoreBlock.FACING))
            {
            case NORTH:
            return NatureCoreBlock.NATURE_CORE_TOP_NORTH;
            case EAST:
            return NatureCoreBlock.NATURE_CORE_TOP_EAST;
            case SOUTH:
            return NatureCoreBlock.NATURE_CORE_TOP_SOUTH;
            case WEST:
            return NatureCoreBlock.NATURE_CORE_TOP_WEST;
            default:
            return NatureCoreBlock.NATURE_CORE_TOP_NORTH;
            }
    }

    // Default States
    public NatureCoreBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NatureCoreBlock.HALF, NatureCorePart.BOTTOM).setValue(
                NatureCoreBlock.FACING, Direction.NORTH).setValue(NatureCoreBlock.WATERLOGGED, false));
    }

    // Places Nature Core Spawner with both top and bottom pieces
    @Override
    public void setPlacedBy(final Level world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final FluidState fluidState = world.getFluidState(pos.above());
            world.setBlock(pos.above(), state.setValue(NatureCoreBlock.HALF, NatureCorePart.TOP).setValue(
                    NatureCoreBlock.WATERLOGGED, fluidState.getType() == Fluids.WATER), 3);
        }
    }

    // Breaking Nature Core Spawner breaks both parts and returns one item only
    @Override
    public void playerWillDestroy(final Level world, final BlockPos pos, final BlockState state,
            final Player player)
    {
        final Direction facing = state.getValue(NatureCoreBlock.FACING);
        final BlockPos natureCorePos = this.getNatureCorePos(pos, state.getValue(NatureCoreBlock.HALF), facing);
        BlockState NatureCoreBlockState = world.getBlockState(natureCorePos);
        if (NatureCoreBlockState.getBlock() == this && !pos.equals(natureCorePos)) this.removeHalf(world, natureCorePos,
                NatureCoreBlockState, player);
        final BlockPos natureCorePartPos = this.getNatureCoreTopPos(natureCorePos, facing);
        NatureCoreBlockState = world.getBlockState(natureCorePartPos);
        if (NatureCoreBlockState.getBlock() == this && !pos.equals(natureCorePartPos)) this.removeHalf(world,
                natureCorePartPos, NatureCoreBlockState, player);
        super.playerWillDestroy(world, pos, state, player);
    }

    private BlockPos getNatureCoreTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        default:
            return base.above();
        }
    }

    private BlockPos getNatureCorePos(final BlockPos pos, final NatureCorePart part, final Direction facing)
    {
        if (part == NatureCorePart.BOTTOM) return pos;
        switch (facing)
        {
        default:
            return pos.below();
        }
    }

    // Breaking the Nature Core Spawner leaves water if underwater
    private void removeHalf(final Level world, final BlockPos pos, final BlockState state, Player player)
    {
        BlockState blockstate = world.getBlockState(pos);
        final FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getType() == Fluids.WATER) world.setBlock(pos, fluidState.createLegacyBlock(), 35);
        else
        {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
            world.levelEvent(player, 2001, pos, Block.getId(blockstate));
        }
    }

    // Prevents the Nature Core Spawner from replacing blocks above it and
    // checks for water
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        final Direction direction = context.getHorizontalDirection().getOpposite();
        final BlockPos pos = context.getClickedPos();
        final Level world = context.getLevel();

        final BlockPos natureCorePos = this.getNatureCoreTopPos(pos, context.getHorizontalDirection().getOpposite());
        
        if (pos.getY() < world.getMaxBuildHeight() && natureCorePos.getY() < world.getMaxBuildHeight()
                && context.getLevel().getBlockState(pos.above()).canBeReplaced(context))
            return this.defaultBlockState().setValue(FACING, direction)
                    .setValue(HALF, NatureCorePart.BOTTOM).setValue(WATERLOGGED, fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8);
        return null;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(NatureCoreBlock.HALF, HorizontalDirectionalBlock.FACING, NatureCoreBlock.WATERLOGGED);
    }

    public NatureCoreBlock(final String name, final Properties props)
    {
        super(name, props.randomTicks());
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel worldIn, final BlockPos pos, final Random random)
    {
        if (random.nextInt(100) == 0) worldIn.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.AMBIENT_CAVE, SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
    }
}
