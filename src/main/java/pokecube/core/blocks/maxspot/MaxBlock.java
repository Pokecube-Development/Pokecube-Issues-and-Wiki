package pokecube.core.blocks.maxspot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
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

public class MaxBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> DYNAMAX     = new HashMap<>();
    protected static final DirectionProperty        FACING      = HorizontalBlock.FACING;
    protected static final BooleanProperty          WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {//@formatter:off
        MaxBlock.DYNAMAX.put(Direction.NORTH,
          VoxelShapes.join(Block.box(10.7, 0, 0.4, 12.8, 1.35, 2.5),
            VoxelShapes.join(Block.box(9.6, 0, 2.25, 15.3, 3.9, 7.85),
              VoxelShapes.join(Block.box(4.2, 0, 0.78, 10.3, 4.3, 6.78),
                VoxelShapes.join(Block.box(12.2762, 0, 9.22, 14.4762, 1.76, 11.32),
                  VoxelShapes.join(Block.box(1.7, 0, 10.65, 3.9, 2, 12.85),
                    VoxelShapes.join(Block.box(8.5, 0, 5.6, 11.5, 3, 8.6),
                      VoxelShapes.join(Block.box(5.3, 0, 6.1184, 9, 1.6, 9.8184),
                        VoxelShapes.join(Block.box(6.9, 0, 8.2, 12.05, 4, 13.4),
                          VoxelShapes.join(Block.box(3.5, 0, 9.55, 8.5, 3.87, 14.55),
                            VoxelShapes.join(Block.box(0.7, 0, 5.6904, 5.9, 4.15, 10.9904),
                              Block.box(1.67, 0, 0.95, 6.37, 3.6, 5.75),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
        MaxBlock.DYNAMAX.put(Direction.EAST,
          VoxelShapes.join(Block.box(13.5, 0, 10.7, 15.6, 1.35, 12.8),
            VoxelShapes.join(Block.box(8.15, 0, 9.6, 13.75, 3.9, 15.3),
              VoxelShapes.join(Block.box(9.22, 0, 4.2, 15.22, 4.3, 10.3),
                VoxelShapes.join(Block.box(4.68, 0, 12.2762, 6.78, 1.76, 14.4762),
                  VoxelShapes.join(Block.box(3.15, 0, 1.7, 5.35, 2, 3.9),
                    VoxelShapes.join(Block.box(7.4, 0, 8.5, 10.4, 3, 11.5),
                      VoxelShapes.join(Block.box(6.1816, 0, 5.3, 9.8816, 1.6, 9),
                        VoxelShapes.join(Block.box(2.6, 0, 6.9, 7.8, 4, 12.05),
                          VoxelShapes.join(Block.box(1.45, 0, 3.5, 6.45, 3.87, 8.5),
                            VoxelShapes.join(Block.box(5.0096, 0, 0.7, 10.3096, 4.15, 5.9),
                              Block.box(10.25, 0, 1.67, 15.05, 3.6, 6.37),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
        MaxBlock.DYNAMAX.put(Direction.SOUTH,
          VoxelShapes.join(Block.box(3.2, 0, 13.5, 5.3, 1.35, 15.6),
            VoxelShapes.join(Block.box(0.7, 0, 8.15, 6.4, 3.9, 13.75),
              VoxelShapes.join(Block.box(5.7, 0, 9.22, 11.8, 4.3, 15.22),
                VoxelShapes.join(Block.box(1.5238, 0, 4.68, 3.7238, 1.76, 6.78),
                  VoxelShapes.join(Block.box(12.1, 0, 3.15, 14.3, 2, 5.35),
                    VoxelShapes.join(Block.box(4.5, 0, 7.4, 7.5, 3, 10.4),
                      VoxelShapes.join(Block.box(7, 0, 6.1816, 10.7, 1.6, 9.8816),
                        VoxelShapes.join(Block.box(3.95, 0, 2.6, 9.1, 4, 7.8),
                          VoxelShapes.join(Block.box(7.5, 0, 1.45, 12.5, 3.87, 6.45),
                            VoxelShapes.join(Block.box(10.1, 0, 5.0096, 15.3, 4.15, 10.3096),
                              Block.box(9.63, 0, 10.25, 14.33, 3.6, 15.05),
                              IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                        IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                  IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
            IBooleanFunction.OR)
        );
        MaxBlock.DYNAMAX.put(Direction.WEST,
          VoxelShapes.join(Block.box(0.4, 0, 3.2, 2.5, 1.35, 5.3),
            VoxelShapes.join(Block.box(2.25, 0, 0.7, 7.85, 3.9, 6.4),
              VoxelShapes.join(Block.box(0.78, 0, 5.7, 6.78, 4.3, 11.8),
                VoxelShapes.join(Block.box(9.22, 0, 1.5238, 11.32, 1.76, 3.7238),
                  VoxelShapes.join(Block.box(10.65, 0, 12.1, 12.85, 2, 14.3),
                    VoxelShapes.join(Block.box(5.6, 0, 4.5, 8.6, 3, 7.5),
                      VoxelShapes.join(Block.box(6.1184, 0, 7, 9.8184, 1.6, 10.7),
                        VoxelShapes.join(Block.box(8.2, 0, 3.95, 13.4, 4, 9.1),
                          VoxelShapes.join(Block.box(9.55, 0, 7.5, 14.55, 3.87, 12.5),
                            VoxelShapes.join(Block.box(5.6904, 0, 10.1, 10.9904, 4.15, 15.3),
                              Block.box(0.95, 0, 9.63, 5.75, 3.6, 14.33),
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
        return MaxBlock.DYNAMAX.get(state.getValue(MaxBlock.FACING));
    }

    public MaxBlock(final Properties properties, final MaterialColor color)
    {
        super(properties, color);
        this.registerDefaultState(this.stateDefinition.any().setValue(MaxBlock.FACING, Direction.NORTH).setValue(
                MaxBlock.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(MaxBlock.FACING, MaxBlock.WATERLOGGED);
    }

    // Waterloggging on placement
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(MaxBlock.FACING, context
                .getHorizontalDirection().getOpposite()).setValue(MaxBlock.WATERLOGGED, ifluidstate.is(
                        FluidTags.WATER) && ifluidstate.getAmount() == 8);
    }

    // Adds Waterlogging State
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(MaxBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
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
        final int power = worldIn.getBestNeighborSignal(pos);
        final TileEntity tile = worldIn.getBlockEntity(pos);
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
