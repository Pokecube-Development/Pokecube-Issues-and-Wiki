package pokecube.core.blocks.maxspot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
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
import net.minecraft.world.World;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class MaxBlock extends InteractableHorizontalBlock
{
    private static final Map<Direction, VoxelShape> DYNAMAX     = new HashMap<>();
    protected static final DirectionProperty        FACING      = HorizontalBlock.HORIZONTAL_FACING;
    protected static final BooleanProperty          WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {//@formatter:off
        MaxBlock.DYNAMAX.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.7, 0, 0.4, 12.8, 1.35, 2.5),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.6, 0, 2.25, 15.3, 3.9, 7.85),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.2, 0, 0.78, 10.3, 4.3, 6.78),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.2762, 0, 9.22, 14.4762, 1.76, 11.32),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.7, 0, 10.65, 3.9, 2, 12.85),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.5, 0, 5.6, 11.5, 3, 8.6),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.3, 0, 6.1184, 9, 1.6, 9.8184),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.9, 0, 8.2, 12.05, 4, 13.4),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 0, 9.55, 8.5, 3.87, 14.55),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.7, 0, 5.6904, 5.9, 4.15, 10.9904),
                              Block.makeCuboidShape(1.67, 0, 0.95, 6.37, 3.6, 5.75),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
        MaxBlock.DYNAMAX.put(Direction.EAST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.5, 0, 10.7, 15.6, 1.35, 12.8),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.15, 0, 9.6, 13.75, 3.9, 15.3),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.22, 0, 4.2, 15.22, 4.3, 10.3),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.68, 0, 12.2762, 6.78, 1.76, 14.4762),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.15, 0, 1.7, 5.35, 2, 3.9),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.4, 0, 8.5, 10.4, 3, 11.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.1816, 0, 5.3, 9.8816, 1.6, 9),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.6, 0, 6.9, 7.8, 4, 12.05),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.45, 0, 3.5, 6.45, 3.87, 8.5),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.0096, 0, 0.7, 10.3096, 4.15, 5.9),
                              Block.makeCuboidShape(10.25, 0, 1.67, 15.05, 3.6, 6.37),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
        MaxBlock.DYNAMAX.put(Direction.SOUTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.2, 0, 13.5, 5.3, 1.35, 15.6),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.7, 0, 8.15, 6.4, 3.9, 13.75),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.7, 0, 9.22, 11.8, 4.3, 15.22),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5238, 0, 4.68, 3.7238, 1.76, 6.78),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.1, 0, 3.15, 14.3, 2, 5.35),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 0, 7.4, 7.5, 3, 10.4),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7, 0, 6.1816, 10.7, 1.6, 9.8816),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.95, 0, 2.6, 9.1, 4, 7.8),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.5, 0, 1.45, 12.5, 3.87, 6.45),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.1, 0, 5.0096, 15.3, 4.15, 10.3096),
                              Block.makeCuboidShape(9.63, 0, 10.25, 14.33, 3.6, 15.05),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
        MaxBlock.DYNAMAX.put(Direction.WEST,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.4, 0, 3.2, 2.5, 1.35, 5.3),
            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.25, 0, 0.7, 7.85, 3.9, 6.4),
              VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.78, 0, 5.7, 6.78, 4.3, 11.8),
                VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.22, 0, 1.5238, 11.32, 1.76, 3.7238),
                  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.65, 0, 12.1, 12.85, 2, 14.3),
                    VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6, 0, 4.5, 8.6, 3, 7.5),
                      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.1184, 0, 7, 9.8184, 1.6, 10.7),
                        VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.2, 0, 3.95, 13.4, 4, 9.1),
                          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.55, 0, 7.5, 14.55, 3.87, 12.5),
                            VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.6904, 0, 10.1, 10.9904, 4.15, 15.3),
                              Block.makeCuboidShape(0.95, 0, 9.63, 5.75, 3.6, 14.33),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
    }//@formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return MaxBlock.DYNAMAX.get(state.get(MaxBlock.FACING));
    }

    public MaxBlock(final Properties properties)
    {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(MaxBlock.FACING, Direction.NORTH).with(
                MaxBlock.WATERLOGGED, false));
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(MaxBlock.FACING, MaxBlock.WATERLOGGED);
    }

    // Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        return Objects.requireNonNull(super.getStateForPlacement(context)).with(MaxBlock.FACING, context
                .getPlacementHorizontalFacing().getOpposite()).with(MaxBlock.WATERLOGGED, ifluidstate.isTagged(
                        FluidTags.WATER) && ifluidstate.getLevel() == 8);
    }

    // Adds Waterlogging State
    @SuppressWarnings("deprecation")
    @Override
    public IFluidState getFluidState(final BlockState state)
    {
        return state.get(MaxBlock.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new MaxTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public void neighborChanged(final BlockState state, final World worldIn, final BlockPos pos, final Block blockIn,
            final BlockPos fromPos, final boolean isMoving)
    {
        final int power = worldIn.getRedstonePowerFromNeighbors(pos);
        final TileEntity tile = worldIn.getTileEntity(pos);
        if (tile == null || !(tile instanceof MaxTile)) return;
        final MaxTile repel = (MaxTile) tile;
        if (power != 0)
        {
            repel.enabled = false;
            repel.removeForbiddenSpawningCoord();
        }
        else
        {
            repel.enabled = true;
            repel.addForbiddenSpawningCoord();
        }
    }
}
