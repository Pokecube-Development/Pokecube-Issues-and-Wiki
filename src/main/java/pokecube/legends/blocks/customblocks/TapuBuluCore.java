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

public class TapuBuluCore extends Rotates implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> BULU  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	TapuBuluCore.BULU.put(Direction.NORTH, VoxelShapes.or(
			Block.box(5, 0, 5, 11, 4, 11),
			Block.box(4, 4, 4, 12, 10, 12),
			Block.box(3, 10, 3, 13, 16, 13),
			Block.box(13, 10, 6, 16, 14, 10),
			Block.box(14, 14, 7, 16, 16, 9),
			Block.box(0, 10, 6, 3, 14, 10),
			Block.box(0, 14, 7, 2, 16, 9)).optimize());
    	TapuBuluCore.BULU.put(Direction.EAST, VoxelShapes.or(
			Block.box(5, 0, 5, 11, 4, 11),
			Block.box(4, 4, 4, 12, 10, 12),
			Block.box(3, 10, 3, 13, 16, 13),
			Block.box(6, 10, 13, 10, 14, 16),
			Block.box(7, 14, 14, 9, 16, 16),
			Block.box(6, 10, 0, 10, 14, 3),
			Block.box(7, 14, 0, 9, 16, 2)).optimize());
    	TapuBuluCore.BULU.put(Direction.SOUTH, VoxelShapes.or(
			Block.box(5, 0, 5, 11, 4, 11),
			Block.box(4, 4, 4, 12, 10, 12),
			Block.box(3, 10, 3, 13, 16, 13),
			Block.box(13, 10, 6, 16, 14, 10),
			Block.box(14, 14, 7, 16, 16, 9),
			Block.box(0, 10, 6, 3, 14, 10),
			Block.box(0, 14, 7, 2, 16, 9)).optimize());
    	TapuBuluCore.BULU.put(Direction.WEST, VoxelShapes.or(
			Block.box(5, 0, 5, 11, 4, 11),
			Block.box(4, 4, 4, 12, 10, 12),
			Block.box(3, 10, 3, 13, 16, 13),
			Block.box(6, 10, 13, 10, 14, 16),
			Block.box(7, 14, 14, 9, 16, 16),
			Block.box(6, 10, 0, 10, 14, 3),
			Block.box(7, 14, 0, 9, 16, 2)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TapuBuluCore.BULU.get(state.getValue(TapuBuluCore.FACING));
    }
    
    public TapuBuluCore(final Properties props)
    {
    	super(props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TapuBuluCore.FACING, Direction.NORTH).setValue(
    			TapuBuluCore.WATERLOGGED, false));
    }
}