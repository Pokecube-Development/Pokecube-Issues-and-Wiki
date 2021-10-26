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

public class HeatranBlock extends Rotates implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> HEATRAN  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	HeatranBlock.HEATRAN.put(Direction.NORTH, VoxelShapes.or(
			Block.box(3, 6, 3, 13, 10, 13),
			Block.box(2, 10, 2, 14, 12, 14),
			Block.box(2, 4, 2, 14, 6, 14),
			Block.box(1, 12, 1, 15, 14, 15),
			Block.box(1, 2, 1, 15, 4, 15),
			Block.box(0, 14, 0, 16, 16, 16),
			Block.box(0, 0, 0, 16, 2, 16)).optimize());
    	HeatranBlock.HEATRAN.put(Direction.EAST, VoxelShapes.or(
			Block.box(3, 6, 3, 13, 10, 13),
			Block.box(2, 10, 2, 14, 12, 14),
			Block.box(2, 4, 2, 14, 6, 14),
			Block.box(1, 12, 1, 15, 14, 15),
			Block.box(1, 2, 1, 15, 4, 15),
			Block.box(0, 14, 0, 16, 16, 16),
			Block.box(0, 0, 0, 16, 2, 16)).optimize());
    	HeatranBlock.HEATRAN.put(Direction.SOUTH, VoxelShapes.or(
			Block.box(3, 6, 3, 13, 10, 13),
			Block.box(2, 10, 2, 14, 12, 14),
			Block.box(2, 4, 2, 14, 6, 14),
			Block.box(1, 12, 1, 15, 14, 15),
			Block.box(1, 2, 1, 15, 4, 15),
			Block.box(0, 14, 0, 16, 16, 16),
			Block.box(0, 0, 0, 16, 2, 16)).optimize());
    	HeatranBlock.HEATRAN.put(Direction.WEST, VoxelShapes.or(
			Block.box(3, 6, 3, 13, 10, 13),
			Block.box(2, 10, 2, 14, 12, 14),
			Block.box(2, 4, 2, 14, 6, 14),
			Block.box(1, 12, 1, 15, 14, 15),
			Block.box(1, 2, 1, 15, 4, 15),
			Block.box(0, 14, 0, 16, 16, 16),
			Block.box(0, 0, 0, 16, 2, 16)).optimize());
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return HeatranBlock.HEATRAN.get(state.getValue(HeatranBlock.FACING));
    }

    public HeatranBlock(final Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(HeatranBlock.FACING, Direction.NORTH).setValue(
        		HeatranBlock.WATERLOGGED, false));
    }
}