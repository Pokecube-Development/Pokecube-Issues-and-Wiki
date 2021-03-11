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

public class TapuLeleCore extends Rotates implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> TROUGH  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	TapuLeleCore.TROUGH.put(Direction.NORTH,
    	Stream.of(
    			Block.box(3, 0, 4, 13, 10, 13),
    			Block.box(4, 10, 5, 12, 16, 12),
    			Block.box(4.5, 10, 3, 5.5, 11, 5),
    			Block.box(10.5, 10, 3, 11.5, 11, 5),
    			Block.box(4.2, 6.19, 2.8, 5.7, 10.2, 4.3),
    			Block.box(10.2, 6.19, 2.8, 11.7, 10.2, 4.3),
    			Block.box(4.2, 3.2, 1, 5.7, 6.2, 4),
    			Block.box(10.2, 3.2, 1, 11.7, 6.2, 4),
    			Block.box(4.6, 4, 13, 5.6, 9, 16),
    			Block.box(10.5, 4, 13, 11.5, 9, 16),
    			Block.box(4.5, 1, 13, 5.5, 4, 15),
    			Block.box(10.5, 1, 13, 11.5, 4, 15)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuLeleCore.TROUGH.put(Direction.EAST,
		Stream.of(
				Block.box(3, 0, 3, 12, 10, 13),
				Block.box(4, 10, 4, 11, 16, 12),
				Block.box(11, 10, 4.5, 13, 11, 5.5),
				Block.box(11, 10, 10.5, 13, 11, 11.5),
				Block.box(11.7, 6.19, 4.2, 13.2, 10.2, 5.7),
				Block.box(11.7, 6.19, 10.2, 13.2, 10.2, 11.7),
				Block.box(12, 3.2, 4.2, 15, 6.2, 5.7),
				Block.box(12, 3.2, 10.2, 15, 6.2, 11.7),
				Block.box(0, 4, 4.6, 3, 9, 5.6),
				Block.box(0, 4, 10.5, 3, 9, 11.5),
				Block.box(1, 1, 4.5, 3, 4, 5.5),
				Block.box(1, 1, 10.5, 3, 4, 11.5)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuLeleCore.TROUGH.put(Direction.SOUTH,
		Stream.of(
				Block.box(3, 0, 3, 13, 10, 12),
				Block.box(4, 10, 4, 12, 16, 11),
				Block.box(10.5, 10, 11, 11.5, 11, 13),
				Block.box(4.5, 10, 11, 5.5, 11, 13),
				Block.box(10.3, 6.199, 11.7, 11.8, 10.2, 13.2),
				Block.box(4.31, 6.19, 11.7, 5.81, 10.2, 13.2),
				Block.box(10.3, 3.2, 12, 11.8, 6.2, 15),
				Block.box(4.31, 3.2, 12, 5.81, 6.2, 15),
				Block.box(10.4, 4, 0, 11.4, 9, 3),
				Block.box(4.5, 4, 0, 5.5, 9, 3),
				Block.box(10.5, 1, 1, 11.5, 4, 3),
				Block.box(4.5, 1, 1, 5.5, 4, 3)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuLeleCore.TROUGH.put(Direction.WEST,
		Stream.of(
				Block.box(4, 0, 3, 13, 10, 13),
				Block.box(5, 10, 4, 12, 16, 12),
				Block.box(3, 10, 10.5, 5, 11, 11.5),
				Block.box(3, 10, 4.5, 5, 11, 5.5),
				Block.box(2.87, 6.19, 10.3, 4.31, 10.2, 11.8),
				Block.box(2.87, 6.19, 4.31, 4.31, 10.2, 5.81),
				Block.box(1, 3.2, 10.3, 4, 6.2, 11.8),
				Block.box(1, 3.2, 4.31, 4, 6.2, 5.81),
				Block.box(13, 4, 10.4, 16, 9, 11.4),
				Block.box(13, 4, 4.5, 16, 9, 5.5),
				Block.box(13, 1, 10.5, 15, 4, 11.5),
				Block.box(13, 1, 4.5, 15, 4, 5.5)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TapuLeleCore.TROUGH.get(state.getValue(TapuLeleCore.FACING));
    }
    
    public TapuLeleCore(final String name, final Properties props)
    {
    	super(name, props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TapuLeleCore.FACING, Direction.NORTH).setValue(
    			TapuLeleCore.WATERLOGGED, false));
    }
}