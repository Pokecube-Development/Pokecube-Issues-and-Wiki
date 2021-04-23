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

public class CramomaticBlock extends Rotates implements IWaterLoggable {

	private static final Map<Direction, VoxelShape> CRAMOBOT  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	CramomaticBlock.CRAMOBOT.put(Direction.NORTH, VoxelShapes.or(
			Block.box(5, 0, 3, 11, 6, 9),
			Block.box(4, 5, 4, 5, 6, 8),
			Block.box(11, 5, 4, 12, 6, 8),
			Block.box(4, 6, 5.5, 5, 12, 6.5),
			Block.box(11, 6, 5.5, 12, 12, 6.5),
			Block.box(4, 11, 6.5, 5, 12, 12.5),
			Block.box(4, 10, 11.5, 5, 11, 12.5),
			Block.box(11, 8, 11.5, 12, 11, 12.5),
			Block.box(11, 11, 6.5, 12, 12, 12.5),
			Block.box(7, 7, 5, 9, 12, 7),
			Block.box(7, 6, 4, 9, 7, 8),
			Block.box(6, 12, 4, 10, 16, 8),
			Block.box(6, 12, 1, 10, 13, 4),
			Block.box(3, 0, 9, 13, 8, 15),
			Block.box(3, 8, 10, 6, 10, 14),
			Block.box(6, 8, 12, 11, 10, 12.01)).optimize());
		CramomaticBlock.CRAMOBOT.put(Direction.EAST, VoxelShapes.or(
			Block.box(7, 0, 5, 13, 6, 11),
			Block.box(8, 5, 4, 12, 6, 5),
			Block.box(8, 5, 11, 12, 6, 12),
			Block.box(9.5, 6, 4, 10.5, 12, 5),
			Block.box(9.5, 6, 11, 10.5, 12, 12),
			Block.box(3.5, 11, 4, 9.5, 12, 5),
			Block.box(3.5, 10, 4, 4.5, 11, 5),
			Block.box(3.5, 8, 11, 4.5, 11, 12),
			Block.box(3.5, 11, 11, 9.5, 12, 12),
			Block.box(9, 7, 7, 11, 12, 9),
			Block.box(8, 6, 7, 12, 7, 9),
			Block.box(8, 12, 6, 12, 16, 10),
			Block.box(12, 12, 6, 15, 13, 10),
			Block.box(1, 0, 3, 7, 8, 13),
			Block.box(2, 8, 3, 6, 10, 6),
			Block.box(4, 8, 6, 4.01, 10, 11)).optimize());
		CramomaticBlock.CRAMOBOT.put(Direction.SOUTH, VoxelShapes.or(
			Block.box(5, 0, 7, 11, 6, 13),
			Block.box(11, 5, 8, 12, 6, 12),
			Block.box(4, 5, 8, 5, 6, 12),
			Block.box(11, 6, 9.5, 12, 12, 10.5),
			Block.box(4, 6, 9.5, 5, 12, 10.5),
			Block.box(11, 11, 3.5, 12, 12, 9.5),
			Block.box(11, 10, 3.5, 12, 11, 4.5),
			Block.box(4, 8, 3.5, 5, 11, 4.5),
			Block.box(4, 11, 3.5, 5, 12, 9.5),
			Block.box(7, 7, 9, 9, 12, 11),
			Block.box(7, 6, 8, 9, 7, 12),
			Block.box(6, 12, 8, 10, 16, 12),
			Block.box(6, 12, 12, 10, 13, 15),
			Block.box(3, 0, 1, 13, 8, 7),
			Block.box(10, 8, 2, 13, 10, 6),
			Block.box(5, 8, 4, 10, 10, 4.01)).optimize());
		CramomaticBlock.CRAMOBOT.put(Direction.WEST, VoxelShapes.or(
			Block.box(3, 0, 5, 9, 6, 11),
			Block.box(4, 5, 11, 8, 6, 12),
			Block.box(4, 5, 4, 8, 6, 5),
			Block.box(5.5, 6, 11, 6.5, 12, 12),
			Block.box(5.5, 6, 4, 6.5, 12, 5),
			Block.box(6.5, 11, 11, 12.5, 12, 12),
			Block.box(11.5, 10, 11, 12.5, 11, 12),
			Block.box(11.5, 8, 4, 12.5, 11, 5),
			Block.box(6.5, 11, 4, 12.5, 12, 5),
			Block.box(5, 7, 7, 7, 12, 9),
			Block.box(4, 6, 7, 8, 7, 9),
			Block.box(4, 12, 6, 8, 16, 10),
			Block.box(1, 12, 6, 4, 13, 10),
			Block.box(9, 0, 3, 15, 8, 13),
			Block.box(10, 8, 10, 14, 10, 13),
			Block.box(12, 8, 5, 12.01, 10, 10)).optimize());
    }
    
	@Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
		return CramomaticBlock.CRAMOBOT.get(state.getValue(CramomaticBlock.FACING));
    }
	
	public CramomaticBlock(final Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(CramomaticBlock.FACING, Direction.NORTH).setValue(
        		CramomaticBlock.WATERLOGGED, false));
    }
}