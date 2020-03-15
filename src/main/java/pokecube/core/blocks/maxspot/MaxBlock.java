package pokecube.core.blocks.maxspot;

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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import pokecube.core.blocks.InteractableHorizontalBlock;
import static net.minecraft.util.math.shapes.VoxelShapes.combineAndSimplify;

public class MaxBlock extends InteractableHorizontalBlock
{
    private static final Map<Direction, VoxelShape> DYNAMAX = new HashMap<>();
    private static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    //Precise selection box
    static
    {
        DYNAMAX.put(Direction.NORTH,
          combineAndSimplify(makeCuboidShape(10.7, 0, 0.4, 12.8, 1.35, 2.5),
            combineAndSimplify(makeCuboidShape(9.6, 0, 2.25, 15.3, 3.9, 7.85),
              combineAndSimplify(makeCuboidShape(4.2, 0, 0.78, 10.3, 4.3, 6.78),
                combineAndSimplify(makeCuboidShape(12.2762, 0, 9.22, 14.4762, 1.76, 11.32),
                  combineAndSimplify(makeCuboidShape(1.7, 0, 10.65, 3.9, 2, 12.85),
                    combineAndSimplify(makeCuboidShape(8.5, 0, 5.6, 11.5, 3, 8.6),
                      combineAndSimplify(makeCuboidShape(5.3, 0, 6.1184, 9, 1.6, 9.8184),
                        combineAndSimplify(makeCuboidShape(6.9, 0, 8.2, 12.05, 4, 13.4),
                          combineAndSimplify(makeCuboidShape(3.5, 0, 9.55, 8.5, 3.87, 14.55),
                            combineAndSimplify(makeCuboidShape(0.7, 0, 5.6904, 5.9, 4.15, 10.9904),
                              makeCuboidShape(1.67, 0, 0.95, 6.37, 3.6, 5.75),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
        DYNAMAX.put(Direction.EAST,
          combineAndSimplify(makeCuboidShape(13.5, 0, 10.7, 15.6, 1.35, 12.8),
            combineAndSimplify(makeCuboidShape(8.15, 0, 9.6, 13.75, 3.9, 15.3),
              combineAndSimplify(makeCuboidShape(9.22, 0, 4.2, 15.22, 4.3, 10.3),
                combineAndSimplify(makeCuboidShape(4.68, 0, 12.2762, 6.78, 1.76, 14.4762),
                  combineAndSimplify(makeCuboidShape(3.15, 0, 1.7, 5.35, 2, 3.9),
                    combineAndSimplify(makeCuboidShape(7.4, 0, 8.5, 10.4, 3, 11.5),
                      combineAndSimplify(makeCuboidShape(6.1816, 0, 5.3, 9.8816, 1.6, 9),
                        combineAndSimplify(makeCuboidShape(2.6, 0, 6.9, 7.8, 4, 12.05),
                          combineAndSimplify(makeCuboidShape(1.45, 0, 3.5, 6.45, 3.87, 8.5),
                            combineAndSimplify(makeCuboidShape(5.0096, 0, 0.7, 10.3096, 4.15, 5.9),
                              makeCuboidShape(10.25, 0, 1.67, 15.05, 3.6, 6.37),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
        DYNAMAX.put(Direction.SOUTH,
          combineAndSimplify(makeCuboidShape(3.2, 0, 13.5, 5.3, 1.35, 15.6),
            combineAndSimplify(makeCuboidShape(0.7, 0, 8.15, 6.4, 3.9, 13.75),
              combineAndSimplify(makeCuboidShape(5.7, 0, 9.22, 11.8, 4.3, 15.22),
                combineAndSimplify(makeCuboidShape(1.5238, 0, 4.68, 3.7238, 1.76, 6.78),
                  combineAndSimplify(makeCuboidShape(12.1, 0, 3.15, 14.3, 2, 5.35),
                    combineAndSimplify(makeCuboidShape(4.5, 0, 7.4, 7.5, 3, 10.4),
                      combineAndSimplify(makeCuboidShape(7, 0, 6.1816, 10.7, 1.6, 9.8816),
                        combineAndSimplify(makeCuboidShape(3.95, 0, 2.6, 9.1, 4, 7.8),
                          combineAndSimplify(makeCuboidShape(7.5, 0, 1.45, 12.5, 3.87, 6.45),
                            combineAndSimplify(makeCuboidShape(10.1, 0, 5.0096, 15.3, 4.15, 10.3096),
                              makeCuboidShape(9.63, 0, 10.25, 14.33, 3.6, 15.05),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
        DYNAMAX.put(Direction.WEST,
          combineAndSimplify(makeCuboidShape(0.4, 0, 3.2, 2.5, 1.35, 5.3),
            combineAndSimplify(makeCuboidShape(2.25, 0, 0.7, 7.85, 3.9, 6.4),
              combineAndSimplify(makeCuboidShape(0.78, 0, 5.7, 6.78, 4.3, 11.8),
                combineAndSimplify(makeCuboidShape(9.22, 0, 1.5238, 11.32, 1.76, 3.7238),
                  combineAndSimplify(makeCuboidShape(10.65, 0, 12.1, 12.85, 2, 14.3),
                    combineAndSimplify(makeCuboidShape(5.6, 0, 4.5, 8.6, 3, 7.5),
                      combineAndSimplify(makeCuboidShape(6.1184, 0, 7, 9.8184, 1.6, 10.7),
                        combineAndSimplify(makeCuboidShape(8.2, 0, 3.95, 13.4, 4, 9.1),
                          combineAndSimplify(makeCuboidShape(9.55, 0, 7.5, 14.55, 3.87, 12.5),
                            combineAndSimplify(makeCuboidShape(5.6904, 0, 10.1, 10.9904, 4.15, 15.3),
                              makeCuboidShape(0.95, 0, 9.63, 5.75, 3.6, 14.33),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
    }

    //Precise selection box
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return DYNAMAX.get(state.get(FACING));
    }

    public MaxBlock(final Properties properties)
    {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState()
          .with(FACING, Direction.NORTH)
          .with(WATERLOGGED, false));
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, WATERLOGGED);
    }

    //Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        return Objects.requireNonNull(super.getStateForPlacement(context))
          .with(FACING, context.getPlacementHorizontalFacing().getOpposite())
          .with(WATERLOGGED, ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8);
    }

    //Adds Waterlogging State
    @SuppressWarnings("deprecation")
    @Override
    public IFluidState getFluidState(BlockState state)
    {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
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
