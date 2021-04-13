package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class TapuLeleCore extends Rotates implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> LELE  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	TapuLeleCore.LELE.put(Direction.NORTH, VoxelShapes.or(
    		Block.box(4, 0, 4, 12, 1, 12),
			Block.box(3, 1, 3, 13, 11, 13),
			Block.box(4, 11, 4, 12, 16, 12),
			Block.box(4, 11, 2, 6, 12, 4),
			Block.box(10, 11, 2, 12, 12, 4),
			Block.box(4, 6, 2, 6, 11, 3),
			Block.box(10, 6, 2, 12, 11, 3),
			Block.box(4, 3, 0, 6, 6, 3),
			Block.box(10, 3, 0, 12, 6, 3),
			Block.box(4, 5, 13, 5, 10, 16),
			Block.box(11, 5, 13, 12, 10, 16),
			Block.box(4, 2, 13, 5, 5, 15),
			Block.box(11, 2, 13, 12, 5, 15)).optimize());
    	TapuLeleCore.LELE.put(Direction.EAST, VoxelShapes.or(
			Block.box(4, 0, 4, 12, 1, 12),
			Block.box(3, 1, 3, 13, 11, 13),
			Block.box(4, 11, 4, 12, 16, 12),
			Block.box(12, 11, 4, 14, 12, 6),
			Block.box(12, 11, 10, 14, 12, 12),
			Block.box(13, 6, 4, 14, 11, 6),
			Block.box(13, 6, 10, 14, 11, 12),
			Block.box(13, 3, 4, 16, 6, 6),
			Block.box(13, 3, 10, 16, 6, 12),
			Block.box(0, 5, 4, 3, 10, 5),
			Block.box(0, 5, 11, 3, 10, 12),
			Block.box(1, 2, 4, 3, 5, 5),
			Block.box(1, 2, 11, 3, 5, 12)).optimize());
    	TapuLeleCore.LELE.put(Direction.SOUTH, VoxelShapes.or(
			Block.box(4, 0, 4, 12, 1, 12),
			Block.box(3, 1, 3, 13, 11, 13),
			Block.box(4, 11, 4, 12, 16, 12),
			Block.box(10, 11, 12, 12, 12, 14),
			Block.box(4, 11, 12, 6, 12, 14),
			Block.box(10, 6, 13, 12, 11, 14),
			Block.box(4, 6, 13, 6, 11, 14),
			Block.box(10, 3, 13, 12, 6, 16),
			Block.box(4, 3, 13, 6, 6, 16),
			Block.box(11, 5, 0, 12, 10, 3),
			Block.box(4, 5, 0, 5, 10, 3),
			Block.box(11, 2, 1, 12, 5, 3),
			Block.box(4, 2, 1, 5, 5, 3)).optimize());
    	TapuLeleCore.LELE.put(Direction.WEST, VoxelShapes.or(
			Block.box(4, 0, 4, 12, 1, 12),
			Block.box(3, 1, 3, 13, 11, 13),
			Block.box(4, 11, 4, 12, 16, 12),
			Block.box(2, 11, 10, 4, 12, 12),
			Block.box(2, 11, 4, 4, 12, 6),
			Block.box(2, 6, 10, 3, 11, 12),
			Block.box(2, 6, 4, 3, 11, 6),
			Block.box(0, 3, 10, 3, 6, 12),
			Block.box(0, 3, 4, 3, 6, 6),
			Block.box(13, 5, 11, 16, 10, 12),
			Block.box(13, 5, 4, 16, 10, 5),
			Block.box(13, 2, 11, 15, 5, 12),
			Block.box(13, 2, 4, 15, 5, 5)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TapuLeleCore.LELE.get(state.getValue(TapuLeleCore.FACING));
    }
    
    public TapuLeleCore(final Properties props)
    {
    	super(props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TapuLeleCore.FACING, Direction.NORTH).setValue(
    			TapuLeleCore.WATERLOGGED, false));
    }
}