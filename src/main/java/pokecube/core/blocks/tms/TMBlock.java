package pokecube.core.blocks.tms;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class TMBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> TM_MACHINE  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.HORIZONTAL_FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
        TMBlock.TM_MACHINE.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 2.75, 16, 1, 13.25),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.9, 6.22, 2.62, 5.43, 7.51, 3.2),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.13, 1, 3.17, 15.85, 9, 12.83),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 2.62, 16, 10, 13.38),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.14, 1, 2.93, 7.21, 9, 13.07),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.1, 6.62, 1.48, 5.22, 7.11, 3.94),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.25, 6.52, 2.02, 4.82, 7.21, 3.17),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.27, 6.52, 1.57, 5.05, 7.21, 2.02),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.31, 10, 4.36, 8.83, 10.5, 7.86),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.71, 10, 5.38, 14.71, 10.75, 10.38),
                                Block.makeCuboidShape(0.89, 9, 9.43, 7.89, 15.23, 12.53),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR)
        );
        TMBlock.TM_MACHINE.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.75, 0, 0, 13.25, 1, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.8, 6.22, 1.9, 13.38, 7.51, 5.43),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.17, 1, 6.13, 12.83, 9, 15.85),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.62, 9, 0, 13.38, 10, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.93, 1, 0.14, 13.07, 9, 7.21),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.06, 6.62, 2.1, 14.52, 7.11, 5.22),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.83, 6.52, 4.25, 13.98, 7.21, 4.82),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.98, 6.52, 2.27, 14.43, 7.21, 5.05),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.14, 10, 0.31, 11.64, 10.5, 8.83),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.62, 10, 10.71, 10.62, 10.75, 14.71),
                                Block.makeCuboidShape(3.47, 9, 0.89, 6.57, 15.23, 7.89),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR)
        );
        TMBlock.TM_MACHINE.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 2.75, 16, 1, 13.25),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.57, 6.22, 12.8, 14.1, 7.51, 13.38),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.15, 1, 3.17, 9.87, 9, 12.83),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 9, 2.62, 16, 10, 13.38),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.79, 1, 2.93, 15.86, 9, 13.07),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.78, 6.62, 12.06, 13.9, 7.11, 14.52),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.18, 6.52, 12.83, 11.75, 7.21, 14.01),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.95, 6.52, 13.98, 13.73, 7.21, 14.43),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.17, 10, 8.14, 15.69, 10.5, 11.64),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.29, 10, 5.62, 5.29, 10.75, 10.62),
                                Block.makeCuboidShape(8.11, 9, 3.47, 15.11, 15.23, 6.57),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR)
        );
        TMBlock.TM_MACHINE.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.75, 0, 0, 13.25, 1, 16),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.62, 6.22, 10.57, 3.2, 7.51, 14.1),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.17, 1, 0.15, 12.83, 9, 9.87),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.62, 9, 0, 13.38, 10, 16),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.93, 1, 8.79, 13.07, 9, 15.86),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.48, 6.62, 10.78, 3.94, 7.11, 13.9),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.02, 6.52, 11.18, 3.17, 7.21, 11.75),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.57, 6.52, 10.95, 2.02, 7.21, 13.73),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.36, 10, 7.17, 7.86, 10.5, 15.69),
                              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.38, 10, 1.29, 10.38, 10.75, 5.29),
                                Block.makeCuboidShape(9.43, 9, 8.11, 12.53, 15.23, 15.11),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR)
        );
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TMBlock.TM_MACHINE.get(state.get(TMBlock.FACING));
    }

    public TMBlock(final Properties properties)
    {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(TMBlock.FACING, Direction.NORTH).with(
                TMBlock.WATERLOGGED, false));
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(TMBlock.FACING, TMBlock.WATERLOGGED);
    }

    // Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        return Objects.requireNonNull(super.getStateForPlacement(context)).with(TMBlock.FACING, context
                .getPlacementHorizontalFacing().getOpposite()).with(TMBlock.WATERLOGGED, ifluidstate.isTagged(
                        FluidTags.WATER) && ifluidstate.getLevel() == 8);
    }

    // Adds Waterlogging State
    @SuppressWarnings("deprecation")
    @Override
    public BlockState updatePostPlacement(final BlockState state, final Direction facing, final BlockState facingState,
            final IWorld world, final BlockPos currentPos, final BlockPos facingPos)
    {
        if (state.get(TMBlock.WATERLOGGED)) world.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER,
                Fluids.WATER.getTickRate(world));

        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IFluidState getFluidState(final BlockState state)
    {
        return state.get(TMBlock.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TMTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }
}
