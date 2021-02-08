package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class TapuFiniCore extends Rotates implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> TROUGH  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.HORIZONTAL_FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	TapuFiniCore.TROUGH.put(Direction.NORTH,
    	Stream.of(
    			Block.makeCuboidShape(4, 0, 5, 12, 4, 15),
    			Block.makeCuboidShape(3, 4, 4, 13, 11, 16),
    			Block.makeCuboidShape(7, 4, 0, 9, 6, 4),
    			Block.makeCuboidShape(0, 3, 10, 4, 4, 13),
    			Block.makeCuboidShape(12, 3, 10, 16, 4, 13),
    			Block.makeCuboidShape(5, 11, 5, 11, 16, 14)
		).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get());
    	TapuFiniCore.TROUGH.put(Direction.EAST,
		Stream.of(
				Block.makeCuboidShape(1, 0, 4, 11, 4, 12),
				Block.makeCuboidShape(0, 4, 3, 12, 11, 13),
				Block.makeCuboidShape(12, 4, 7, 16, 6, 9),
				Block.makeCuboidShape(3, 3, 0, 6, 4, 4),
				Block.makeCuboidShape(3, 3, 12, 6, 4, 16),
				Block.makeCuboidShape(2, 11, 5, 11, 16, 11)
		).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get());
    	TapuFiniCore.TROUGH.put(Direction.SOUTH,
		Stream.of(
				Block.makeCuboidShape(4, 0, 1, 12, 4, 11),
				Block.makeCuboidShape(3, 4, 0, 13, 11, 12),
				Block.makeCuboidShape(7, 4, 12, 9, 6, 16),
				Block.makeCuboidShape(12, 3, 3, 16, 4, 6),
				Block.makeCuboidShape(0, 3, 3, 4, 4, 6),
				Block.makeCuboidShape(5, 11, 2, 11, 16, 11)
		).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get());
    	TapuFiniCore.TROUGH.put(Direction.WEST,
		Stream.of(
				Block.makeCuboidShape(5, 0, 4, 15, 4, 12),
				Block.makeCuboidShape(4, 4, 3, 16, 11, 13),
				Block.makeCuboidShape(0, 4, 7, 4, 6, 9),
				Block.makeCuboidShape(10, 3, 12, 13, 4, 16),
				Block.makeCuboidShape(10, 3, 0, 13, 4, 4),
				Block.makeCuboidShape(5, 11, 5, 14, 16, 11)
		).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get());
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TapuFiniCore.TROUGH.get(state.get(TapuFiniCore.FACING));
    }
    
    public TapuFiniCore(final String name, final Properties props)
    {
    	super(name, props);
    	this.setDefaultState(this.stateContainer.getBaseState().with(TapuFiniCore.FACING, Direction.NORTH).with(
    			TapuFiniCore.WATERLOGGED, false));
    }
}