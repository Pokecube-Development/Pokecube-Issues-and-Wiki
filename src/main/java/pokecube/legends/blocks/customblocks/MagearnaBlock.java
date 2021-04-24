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

public class MagearnaBlock extends Rotates implements IWaterLoggable
{
	private static final Map<Direction, VoxelShape> MAGEARNA  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	MagearnaBlock.MAGEARNA.put(Direction.NORTH, VoxelShapes.or(
			Block.box(2, 1, 0, 14, 15, 16),
			Block.box(0, 2, 1, 16, 14, 15),
			Block.box(1, 0, 2, 15, 16, 14)).optimize());
		MagearnaBlock.MAGEARNA.put(Direction.EAST, VoxelShapes.or(
			Block.box(0, 1, 2, 16, 15, 14),
			Block.box(1, 2, 0, 15, 14, 16),
			Block.box(2, 0, 1, 14, 16, 15)).optimize());
		MagearnaBlock.MAGEARNA.put(Direction.SOUTH, VoxelShapes.or(
			Block.box(2, 1, 0, 14, 15, 16),
			Block.box(0, 2, 1, 16, 14, 15),
			Block.box(1, 0, 2, 15, 16, 14)).optimize());
		MagearnaBlock.MAGEARNA.put(Direction.WEST, VoxelShapes.or(
			Block.box(0, 1, 2, 16, 15, 14),
			Block.box(1, 2, 0, 15, 14, 16),
			Block.box(2, 0, 1, 14, 16, 15)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return MagearnaBlock.MAGEARNA.get(state.getValue(MagearnaBlock.FACING));
    }

    public MagearnaBlock(Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(MagearnaBlock.FACING, Direction.NORTH).setValue(
        		MagearnaBlock.WATERLOGGED, false));
    }
}