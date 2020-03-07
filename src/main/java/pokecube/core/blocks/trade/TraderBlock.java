package pokecube.core.blocks.trade;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.Material;
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
import net.minecraft.world.IWorld;
import net.minecraftforge.common.ToolType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pokecube.core.blocks.InteractableHorizontalBlock;
import pokecube.core.blocks.maxspot.MaxBlock;
import pokecube.core.blocks.tms.TMBlock;

import static net.minecraft.util.math.shapes.VoxelShapes.combineAndSimplify;

public class TraderBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> TRADER_BLOCK = new HashMap<>();
    private static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    //Precise selection box
    static
    {
        TRADER_BLOCK.put(Direction.NORTH,
          combineAndSimplify(makeCuboidShape(0, 0, 1.66, 16, 1, 14.34),
            combineAndSimplify(makeCuboidShape(0, 9, 1.66, 16, 10, 14.34),
              combineAndSimplify(makeCuboidShape(2.64, 1, 2.02, 13.36, 9, 13.98),
                combineAndSimplify(makeCuboidShape(.21, 1, 2.67, 15.79, 9, 13.33),
                  combineAndSimplify(makeCuboidShape(5.5, 10, 4.28, 10.5, 10.5, 7.28),
                    combineAndSimplify(makeCuboidShape(10.07, 10, 3.16, 15.07, 11, 8.16),
                      combineAndSimplify(makeCuboidShape(1.12, 10, 3.16, 5.87, 11, 8.16),
                              makeCuboidShape(4.4, 8, 10.25, 11.4, 15.45, 13.45),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR)
        );
        TRADER_BLOCK.put(Direction.EAST,
          combineAndSimplify(makeCuboidShape(1.66, 0, 0, 14.34, 1, 16),
            combineAndSimplify(makeCuboidShape(1.66, 9, 0, 14.34, 10, 16),
              combineAndSimplify(makeCuboidShape(2.02, 1, 2.64, 13.98, 9, 13.36),
                combineAndSimplify(makeCuboidShape(2.67, 1, 0.21, 13.33, 9, 15.79),
                  combineAndSimplify(makeCuboidShape(8.72, 10, 5.5, 11.72, 10.5, 10.5),
                    combineAndSimplify(makeCuboidShape(7.84, 10, 10.7, 12.84, 11, 15.07),
                      combineAndSimplify(makeCuboidShape(7.84, 10, 1.12, 12.84, 11, 5.87),
                          makeCuboidShape(2.55, 9, 4.4, 5.75, 15.45, 11.4),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
              IBooleanFunction.OR)
        );
        TRADER_BLOCK.put(Direction.SOUTH,
          combineAndSimplify(makeCuboidShape(0, 0, 1.66, 16, 1, 14.34),
            combineAndSimplify(makeCuboidShape(0, 9, 1.66, 16, 10, 14.34),
              combineAndSimplify(makeCuboidShape(2.64, 1, 2.02, 13.36, 9, 13.98),
                combineAndSimplify(makeCuboidShape(.21, 1, 2.67, 15.79, 9, 13.33),
                  combineAndSimplify(makeCuboidShape(5.5, 10, 8.72, 10.5, 10.5, 11.72),
                    combineAndSimplify(makeCuboidShape(0.93, 10, 7.84, 5.93, 11, 12.84),
                      combineAndSimplify(makeCuboidShape(10.13, 10, 7.84, 14.88, 11, 12.84),
                        makeCuboidShape(4.6, 9, 2.55, 11.6, 15.45, 5.75),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
        TRADER_BLOCK.put(Direction.WEST,
          combineAndSimplify(makeCuboidShape(1.66, 0, 0, 14.34, 1, 16),
            combineAndSimplify(makeCuboidShape(1.66, 9, 0, 14.34, 10, 16),
              combineAndSimplify(makeCuboidShape(2.02, 1, 2.64, 13.98, 9, 13.36),
                combineAndSimplify(makeCuboidShape(2.67, 1, 0.21, 13.33, 9, 15.79),
                  combineAndSimplify(makeCuboidShape(4.28, 10, 5.5, 7.28, 10.5, 10.5),
                    combineAndSimplify(makeCuboidShape(3.16, 10, 0.93, 8.16, 11, 5.93),
                      combineAndSimplify(makeCuboidShape(3.16, 10, 10.13, 8.16, 11, 14.88),
                        makeCuboidShape(10.25, 9, 4.6, 13.45, 15.45, 11.6),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
    }

    //Precise selection box
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return TRADER_BLOCK.get(state.get(FACING));
    }

    public TraderBlock(final Properties properties)
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
        return new TraderTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

}
