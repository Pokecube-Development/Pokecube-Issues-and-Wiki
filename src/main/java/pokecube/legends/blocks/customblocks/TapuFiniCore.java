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
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	TapuFiniCore.TROUGH.put(Direction.NORTH,
    	Stream.of(
    			Block.box(4, 0, 5, 12, 4, 15),
    			Block.box(3, 4, 4, 13, 11, 16),
    			Block.box(7, 4, 0, 9, 6, 4),
    			Block.box(0, 3, 10, 4, 4, 13),
    			Block.box(12, 3, 10, 16, 4, 13),
    			Block.box(5, 11, 5, 11, 16, 14)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuFiniCore.TROUGH.put(Direction.EAST,
		Stream.of(
				Block.box(1, 0, 4, 11, 4, 12),
				Block.box(0, 4, 3, 12, 11, 13),
				Block.box(12, 4, 7, 16, 6, 9),
				Block.box(3, 3, 0, 6, 4, 4),
				Block.box(3, 3, 12, 6, 4, 16),
				Block.box(2, 11, 5, 11, 16, 11)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuFiniCore.TROUGH.put(Direction.SOUTH,
		Stream.of(
				Block.box(4, 0, 1, 12, 4, 11),
				Block.box(3, 4, 0, 13, 11, 12),
				Block.box(7, 4, 12, 9, 6, 16),
				Block.box(12, 3, 3, 16, 4, 6),
				Block.box(0, 3, 3, 4, 4, 6),
				Block.box(5, 11, 2, 11, 16, 11)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuFiniCore.TROUGH.put(Direction.WEST,
		Stream.of(
				Block.box(5, 0, 4, 15, 4, 12),
				Block.box(4, 4, 3, 16, 11, 13),
				Block.box(0, 4, 7, 4, 6, 9),
				Block.box(10, 3, 12, 13, 4, 16),
				Block.box(10, 3, 0, 13, 4, 4),
				Block.box(5, 11, 5, 14, 16, 11)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TapuFiniCore.TROUGH.get(state.getValue(TapuFiniCore.FACING));
    }
    
    public TapuFiniCore(final String name, final Properties props)
    {
    	super(name, props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TapuFiniCore.FACING, Direction.NORTH).setValue(
    			TapuFiniCore.WATERLOGGED, false));
    }
}