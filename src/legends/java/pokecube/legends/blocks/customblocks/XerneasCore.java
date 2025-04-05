package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class XerneasCore extends Rotates implements SimpleWaterloggedBlock
{
    private static final EnumProperty<XerneasCorePart> PART = EnumProperty.create("part",
            XerneasCorePart.class);
    private static final Map<Direction, VoxelShape>    XERNEAS_MIDDLE_LEFT  = new HashMap<>();
    private static final Map<Direction, VoxelShape>    XERNEAS_MIDDLE_RIGHT = new HashMap<>();
    private static final Map<Direction, VoxelShape>    XERNEAS_TOP       = new HashMap<>();
    private static final Map<Direction, VoxelShape>    XERNEAS_TOP_LEFT  = new HashMap<>();
    private static final Map<Direction, VoxelShape>    XERNEAS_TOP_RIGHT = new HashMap<>();
    private static final BooleanProperty               WATERLOGGED       = BlockStateProperties.WATERLOGGED;
    private static final DirectionProperty             FACING            = HorizontalDirectionalBlock.FACING;

    // Precise selection box
    private static final VoxelShape XERNEAS_BOTTOM = Shapes.or(
            Block.box(2, 0, 2, 14, 16, 14),
            Block.box(0, 0, 4, 2, 8, 12),
            Block.box(4, 0, 14, 12, 8, 16),
            Block.box(14, 0, 4, 16, 8, 12),
            Block.box(4, 0, 0, 12, 8, 2)).optimize();

    static
    {
        XerneasCore.XERNEAS_MIDDLE_LEFT.put(Direction.NORTH, Shapes.or(
            Block.box(0, 0, 4, 8, 12, 12),
            Block.box(0, 5, 5, 15, 16, 11)).optimize());
        XerneasCore.XERNEAS_MIDDLE_LEFT.put(Direction.EAST, Shapes.or(
            Block.box(4, 0, 0, 12, 12, 8),
            Block.box(5, 5, 0, 11, 16, 15)).optimize());
        XerneasCore.XERNEAS_MIDDLE_LEFT.put(Direction.SOUTH, Shapes.or(
            Block.box(8, 0, 4, 16, 12, 12),
            Block.box(1, 5, 5, 16, 16, 11)).optimize());
        XerneasCore.XERNEAS_MIDDLE_LEFT.put(Direction.WEST, Shapes.or(
            Block.box(4, 0, 8, 12, 12, 16),
            Block.box(5, 5, 1, 11, 16, 16)).optimize());

        XerneasCore.XERNEAS_MIDDLE_RIGHT.put(Direction.NORTH, Shapes.or(
            Block.box(8, 0, 4, 16, 12, 12),
            Block.box(1, 5, 5, 16, 16, 11)).optimize());
        XerneasCore.XERNEAS_MIDDLE_RIGHT.put(Direction.EAST, Shapes.or(
            Block.box(4, 0, 8, 12, 12, 16),
            Block.box(5, 5, 1, 11, 16, 16)).optimize());
        XerneasCore.XERNEAS_MIDDLE_RIGHT.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 0, 4, 8, 12, 12),
            Block.box(0, 5, 5, 15, 16, 11)).optimize());
        XerneasCore.XERNEAS_MIDDLE_RIGHT.put(Direction.WEST, Shapes.or(
            Block.box(4, 0, 0, 12, 12, 8),
            Block.box(5, 5, 0, 11, 16, 15)).optimize());

        XerneasCore.XERNEAS_TOP.put(Direction.NORTH, Shapes.or(
            Block.box(3, 0, 3, 13, 16, 13),
            Block.box(0, 0, 4, 3, 12, 12),
            Block.box(13, 0, 4, 16, 12, 12),
            Block.box(0, 12, 5, 3, 16, 11),
            Block.box(13, 12, 5, 16, 16, 11)).optimize());
        XerneasCore.XERNEAS_TOP.put(Direction.EAST, Shapes.or(
            Block.box(3, 0, 3, 13, 16, 13),
            Block.box(4, 0, 0, 12, 12, 3),
            Block.box(4, 0, 13, 12, 12, 16),
            Block.box(5, 12, 0, 11, 16, 3),
            Block.box(5, 12, 13, 11, 16, 16)).optimize());
        XerneasCore.XERNEAS_TOP.put(Direction.SOUTH, Shapes.or(
            Block.box(3, 0, 3, 13, 16, 13),
            Block.box(0, 0, 4, 3, 12, 12),
            Block.box(13, 0, 4, 16, 12, 12),
            Block.box(0, 12, 5, 3, 16, 11),
            Block.box(13, 12, 5, 16, 16, 11)).optimize());
        XerneasCore.XERNEAS_TOP.put(Direction.WEST, Shapes.or(
            Block.box(3, 0, 3, 13, 16, 13),
            Block.box(4, 0, 0, 12, 12, 3),
            Block.box(4, 0, 13, 12, 12, 16),
            Block.box(5, 12, 0, 11, 16, 3),
            Block.box(5, 12, 13, 11, 16, 16)).optimize());

        XerneasCore.XERNEAS_TOP_LEFT.put(Direction.NORTH, Shapes.or(
            Block.box(0, 0, 5, 8, 16, 11),
            Block.box(8, 0, 6, 16, 10, 10)).optimize());
        XerneasCore.XERNEAS_TOP_LEFT.put(Direction.EAST, Shapes.or(
            Block.box(5, 0, 0, 11, 16, 8),
            Block.box(6, 0, 8, 10, 10, 16)).optimize());
        XerneasCore.XERNEAS_TOP_LEFT.put(Direction.SOUTH, Shapes.or(
            Block.box(8, 0, 5, 16, 16, 11),
            Block.box(0, 0, 6, 8, 10, 10)).optimize());
        XerneasCore.XERNEAS_TOP_LEFT.put(Direction.WEST, Shapes.or(
            Block.box(5, 0, 8, 11, 16, 16),
            Block.box(6, 0, 0, 10, 10, 8)).optimize());

        XerneasCore.XERNEAS_TOP_RIGHT.put(Direction.NORTH, Shapes.or(
            Block.box(8, 0, 5, 16, 16, 11),
            Block.box(0, 0, 6, 8, 10, 10)).optimize());
        XerneasCore.XERNEAS_TOP_RIGHT.put(Direction.EAST, Shapes.or(
            Block.box(5, 0, 8, 11, 16, 16),
            Block.box(6, 0, 0, 10, 10, 8)).optimize());
        XerneasCore.XERNEAS_TOP_RIGHT.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 0, 5, 8, 16, 11),
            Block.box(8, 0, 6, 16, 10, 10)).optimize());
        XerneasCore.XERNEAS_TOP_RIGHT.put(Direction.WEST, Shapes.or(
            Block.box(5, 0, 0, 11, 16, 8),
            Block.box(6, 0, 8, 10, 10, 16)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        final XerneasCorePart part = state.getValue(XerneasCore.PART);
        if (part == XerneasCorePart.BOTTOM) return XerneasCore.XERNEAS_BOTTOM;
        else if (part == XerneasCorePart.MIDDLE_LEFT) return XerneasCore.XERNEAS_MIDDLE_LEFT.get(state.getValue(
                XerneasCore.FACING));
        else if (part == XerneasCorePart.MIDDLE_RIGHT) return XerneasCore.XERNEAS_MIDDLE_RIGHT.get(state.getValue(
                XerneasCore.FACING));
        else if (part == XerneasCorePart.TOP_LEFT) return XerneasCore.XERNEAS_TOP_LEFT.get(state.getValue(
                XerneasCore.FACING));
        else if (part == XerneasCorePart.TOP_RIGHT) return XerneasCore.XERNEAS_TOP_RIGHT.get(state.getValue(
                XerneasCore.FACING));
        else return XerneasCore.XERNEAS_TOP.get(state.getValue(XerneasCore.FACING));
    }

    public XerneasCore(final Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(XerneasCore.FACING, Direction.NORTH).setValue(
                XerneasCore.WATERLOGGED, false).setValue(XerneasCore.PART, XerneasCorePart.BOTTOM));
    }

    // Places Xerneas Core Spawner with all pieces
    @Override
    public void setPlacedBy(final Level world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity entity, final ItemStack stack)
    {
        if (entity != null)
        {
            final BlockPos xerneasCoreMiddleLeftPos = this.getXerneasCoreMiddleLeftPos(pos, entity.getDirection());
            final BlockPos xerneasCoreMiddleRightPos = this.getXerneasCoreMiddleRightPos(pos, entity.getDirection());
            final BlockPos xerneasCoreTopLeftPos = this.getXerneasCoreTopLeftPos(pos, entity.getDirection());
            final BlockPos xerneasCoreTopRightPos = this.getXerneasCoreTopRightPos(pos, entity.getDirection());

            final FluidState fluidState = world.getFluidState(pos.above());
            final FluidState middleLeftFluidState = world.getFluidState(xerneasCoreMiddleLeftPos);
            final FluidState middleRightFluidState = world.getFluidState(xerneasCoreMiddleRightPos);
            final FluidState topLeftFluidState = world.getFluidState(xerneasCoreTopLeftPos);
            final FluidState topRightFluidState = world.getFluidState(xerneasCoreTopRightPos);

            world.setBlock(xerneasCoreMiddleLeftPos, state.setValue(XerneasCore.PART, XerneasCorePart.MIDDLE_LEFT).setValue(
                    XerneasCore.WATERLOGGED, middleLeftFluidState.getType() == Fluids.WATER), 3);
            world.setBlock(xerneasCoreMiddleRightPos, state.setValue(XerneasCore.PART, XerneasCorePart.MIDDLE_RIGHT).setValue(
                    XerneasCore.WATERLOGGED, middleRightFluidState.getType() == Fluids.WATER), 3);
            world.setBlock(pos.above(), state.setValue(XerneasCore.PART, XerneasCorePart.TOP).setValue(
                    XerneasCore.WATERLOGGED, fluidState.getType() == Fluids.WATER), 3);
            world.setBlock(xerneasCoreTopLeftPos, state.setValue(XerneasCore.PART, XerneasCorePart.TOP_LEFT).setValue(
                    XerneasCore.WATERLOGGED, topLeftFluidState.getType() == Fluids.WATER), 3);
            world.setBlock(xerneasCoreTopRightPos, state.setValue(XerneasCore.PART, XerneasCorePart.TOP_RIGHT).setValue(
                    XerneasCore.WATERLOGGED, topRightFluidState.getType() == Fluids.WATER), 3);
        }
    }

    // Breaking Xerneas Core Spawner breaks both parts and returns one item only
    @Override
    public void playerWillDestroy(final Level world, final BlockPos pos, final BlockState state,
            final Player player)
    {
        final Direction facing = state.getValue(XerneasCore.FACING);

        final BlockPos xerneasCorePos = this.getXerneasCorePos(pos, state.getValue(XerneasCore.PART), facing);
        BlockState XerneasCoreBlockState = world.getBlockState(xerneasCorePos);
        if (XerneasCoreBlockState.getBlock() == this && !pos.equals(xerneasCorePos)) this.removePart(world,
                xerneasCorePos, XerneasCoreBlockState, player);

        BlockPos xerneasCorePartPos = this.getXerneasCoreMiddleLeftPos(xerneasCorePos, facing);
        XerneasCoreBlockState = world.getBlockState(xerneasCorePartPos);
        if (XerneasCoreBlockState.getBlock() == this && !pos.equals(xerneasCorePartPos)) this.removePart(world,
                xerneasCorePartPos, XerneasCoreBlockState, player);

        xerneasCorePartPos = this.getXerneasCoreMiddleRightPos(xerneasCorePos, facing);
        XerneasCoreBlockState = world.getBlockState(xerneasCorePartPos);
        if (XerneasCoreBlockState.getBlock() == this && !pos.equals(xerneasCorePartPos)) this.removePart(world,
                xerneasCorePartPos, XerneasCoreBlockState, player);
        super.playerWillDestroy(world, pos, state, player);

        xerneasCorePartPos = this.getXerneasCoreTopPos(xerneasCorePos, facing);
        XerneasCoreBlockState = world.getBlockState(xerneasCorePartPos);
        if (XerneasCoreBlockState.getBlock() == this && !pos.equals(xerneasCorePartPos)) this.removePart(world,
                xerneasCorePartPos, XerneasCoreBlockState, player);

        xerneasCorePartPos = this.getXerneasCoreTopLeftPos(xerneasCorePos, facing);
        XerneasCoreBlockState = world.getBlockState(xerneasCorePartPos);
        if (XerneasCoreBlockState.getBlock() == this && !pos.equals(xerneasCorePartPos)) this.removePart(world,
                xerneasCorePartPos, XerneasCoreBlockState, player);

        xerneasCorePartPos = this.getXerneasCoreTopRightPos(xerneasCorePos, facing);
        XerneasCoreBlockState = world.getBlockState(xerneasCorePartPos);
        if (XerneasCoreBlockState.getBlock() == this && !pos.equals(xerneasCorePartPos)) this.removePart(world,
                xerneasCorePartPos, XerneasCoreBlockState, player);
        super.playerWillDestroy(world, pos, state, player);
    }

    private BlockPos getXerneasCoreMiddleLeftPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
            case NORTH:
                return base.above().west();
            case EAST:
                return base.above().north();
            case SOUTH:
                return base.above().east();
            case WEST:
                return base.above().south();
            default:
                return base.above().east();
        }
    }

    private BlockPos getXerneasCoreMiddleRightPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
            case NORTH:
                return base.above().east();
            case EAST:
                return base.above().south();
            case SOUTH:
                return base.above().west();
            case WEST:
                return base.above().north();
            default:
                return base.above().west();
        }
    }

    private BlockPos getXerneasCoreTopPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.above();
        case EAST:
            return base.above();
        case SOUTH:
            return base.above();
        case WEST:
            return base.above();
        default:
            return base.above();
        }
    }

    private BlockPos getXerneasCoreTopLeftPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.above(2).west();
        case EAST:
            return base.above(2).north();
        case SOUTH:
            return base.above(2).east();
        case WEST:
            return base.above(2).south();
        default:
            return base.above(2).east();
        }
    }

    private BlockPos getXerneasCoreTopRightPos(final BlockPos base, final Direction facing)
    {
        switch (facing)
        {
        case NORTH:
            return base.above(2).east();
        case EAST:
            return base.above(2).south();
        case SOUTH:
            return base.above(2).west();
        case WEST:
            return base.above(2).north();
        default:
            return base.above(2).west();
        }
    }

    private BlockPos getXerneasCorePos(final BlockPos pos, final XerneasCorePart part, final Direction facing)
    {
        if (part == XerneasCorePart.BOTTOM) return pos;
        switch (facing)
        {
        case NORTH:
            switch (part)
            {
                case MIDDLE_LEFT:
                    return pos.below().west();
                case MIDDLE_RIGHT:
                    return pos.below().east();
                case TOP:
                    return pos.below();
                case TOP_LEFT:
                    return pos.below(2).west();
                case TOP_RIGHT:
                    return pos.below(2).east();
                default:
                    return null;
            }
        case EAST:
            switch (part)
            {
                case MIDDLE_LEFT:
                    return pos.below().north();
                case MIDDLE_RIGHT:
                    return pos.below().south();
                case TOP:
                    return pos.below();
                case TOP_LEFT:
                    return pos.below(2).north();
                case TOP_RIGHT:
                    return pos.below(2).south();
                default:
                    return null;
            }
        case SOUTH:
            switch (part)
            {
                case MIDDLE_LEFT:
                    return pos.below().east();
                case MIDDLE_RIGHT:
                    return pos.below().west();
                case TOP:
                    return pos.below();
                case TOP_LEFT:
                    return pos.below(2).east();
                case TOP_RIGHT:
                    return pos.below(2).west();
                default:
                    return null;
            }
        case WEST:
            switch (part)
            {
                case MIDDLE_LEFT:
                    return pos.below().south();
                case MIDDLE_RIGHT:
                    return pos.below().north();
                case TOP:
                    return pos.below();
                case TOP_LEFT:
                    return pos.below(2).south();
                case TOP_RIGHT:
                    return pos.below(2).north();
                default:
                    return null;
            }
        default:
            return null;
        }
    }

    // Breaking the Xerneas Core Spawner leaves water if underwater
    private void removePart(final Level world, final BlockPos pos, final BlockState state, Player player)
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

    // Prevents the Xerneas Core Spawner from replacing blocks above it and
    // checks for water
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        final Direction direction = context.getHorizontalDirection().getOpposite();
        final BlockPos pos = context.getClickedPos();
        final Level world = context.getLevel();

        final BlockPos posMiddleLeft = this.getXerneasCoreMiddleLeftPos(pos, direction);
        final BlockPos posMiddleRight = this.getXerneasCoreMiddleRightPos(pos, direction);
        
        final BlockPos posTop = this.getXerneasCoreTopPos(pos, direction);
        final BlockPos posTopLeft = this.getXerneasCoreTopLeftPos(pos, direction);
        final BlockPos posTopRight = this.getXerneasCoreTopRightPos(pos, direction);

        if  (pos.getY() < world.getMaxBuildHeight()
                && posMiddleLeft.getY() < world.getMaxBuildHeight() && context.getLevel().getBlockState(posMiddleLeft).canBeReplaced(context)
                && posMiddleRight.getY() < world.getMaxBuildHeight() && context.getLevel().getBlockState(posMiddleRight).canBeReplaced(context)
                && posTop.getY() < world.getMaxBuildHeight() && context.getLevel().getBlockState(pos.above()).canBeReplaced(context)
                && posTopLeft.getY() < world.getMaxBuildHeight() && context.getLevel().getBlockState(posTopLeft).canBeReplaced(context)
                && posTopRight.getY() < world.getMaxBuildHeight() && context.getLevel().getBlockState(posTopRight).canBeReplaced(context))
        {
            return this.defaultBlockState().setValue(FACING, direction).setValue(PART, XerneasCorePart.BOTTOM)
                .setValue(WATERLOGGED, fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8);
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(XerneasCore.PART, XerneasCore.FACING, XerneasCore.WATERLOGGED);
    }
}