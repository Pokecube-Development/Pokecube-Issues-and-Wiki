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

public class TapuFiniCore extends Rotates implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> FINI  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	TapuFiniCore.FINI.put(Direction.NORTH, VoxelShapes.or(
			Block.box(4, 0, 4, 12, 3, 12),
			Block.box(3, 3, 3, 13, 13, 13),
			Block.box(4, 13, 4, 12, 16, 12),
			Block.box(7, 7, 0, 9, 9, 3),
			Block.box(13, 5, 6, 16, 6, 10),
			Block.box(0, 5, 6, 3, 6, 10)).optimize());
    	TapuFiniCore.FINI.put(Direction.EAST, VoxelShapes.or(
			Block.box(4, 0, 4, 12, 3, 12),
			Block.box(3, 3, 3, 13, 13, 13),
			Block.box(4, 13, 4, 12, 16, 12),
			Block.box(13, 7, 7, 16, 9, 9),
			Block.box(6, 5, 13, 10, 6, 16),
			Block.box(6, 5, 0, 10, 6, 3)).optimize());
    	TapuFiniCore.FINI.put(Direction.SOUTH, VoxelShapes.or(
			Block.box(4, 0, 4, 12, 3, 12),
			Block.box(3, 3, 3, 13, 13, 13),
			Block.box(4, 13, 4, 12, 16, 12),
			Block.box(7, 7, 13, 9, 9, 16),
			Block.box(13, 5, 6, 16, 6, 10),
			Block.box(0, 5, 6, 3, 6, 10)).optimize());
    	TapuFiniCore.FINI.put(Direction.WEST, VoxelShapes.or(
			Block.box(4, 0, 4, 12, 3, 12),
			Block.box(3, 3, 3, 13, 13, 13),
			Block.box(4, 13, 4, 12, 16, 12),
			Block.box(0, 7, 7, 3, 9, 9),
			Block.box(6, 5, 13, 10, 6, 16),
			Block.box(6, 5, 0, 10, 6, 3)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TapuFiniCore.FINI.get(state.getValue(TapuFiniCore.FACING));
    }
    
    public TapuFiniCore(final String name, final Properties props)
    {
    	super(name, props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TapuFiniCore.FACING, Direction.NORTH).setValue(
    			TapuFiniCore.WATERLOGGED, false));
    }
}