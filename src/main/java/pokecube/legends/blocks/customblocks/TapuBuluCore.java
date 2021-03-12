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

public class TapuBuluCore extends Rotates implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> TROUGH  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	TapuBuluCore.TROUGH.put(Direction.NORTH,
    	Stream.of(
    			Block.box(0, 14, 7, 2, 16, 10),
    			Block.box(14, 14, 6, 16, 16, 9),
    			Block.box(0, 10, 6, 3, 14, 11),
    			Block.box(13, 10, 5, 16, 14, 10),
    			Block.box(3, 10, 3, 13, 16, 13),
    			Block.box(6, 0, 6.5, 10, 2, 9.5),
    			Block.box(5, 2, 5, 11, 4, 11),
    			Block.box(4, 4, 4, 12, 10, 12)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuBuluCore.TROUGH.put(Direction.EAST,
		Stream.of(
				Block.box(6, 14, 0, 9, 16, 2),
				Block.box(7, 14, 14, 10, 16, 16),
				Block.box(5, 10, 0, 10, 14, 3),
				Block.box(6, 10, 13, 11, 14, 16),
				Block.box(3, 10, 3, 13, 16, 13),
				Block.box(6.5, 0, 6, 9.5, 2, 10),
				Block.box(5, 2, 5, 11, 4, 11),
				Block.box(4, 4, 4, 12, 10, 12)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuBuluCore.TROUGH.put(Direction.SOUTH,
		Stream.of(
				Block.box(0, 14, 7, 2, 16, 10),
    			Block.box(14, 14, 6, 16, 16, 9),
    			Block.box(0, 10, 6, 3, 14, 11),
    			Block.box(13, 10, 5, 16, 14, 10),
    			Block.box(3, 10, 3, 13, 16, 13),
    			Block.box(6, 0, 6.5, 10, 2, 9.5),
    			Block.box(5, 2, 5, 11, 4, 11),
    			Block.box(4, 4, 4, 12, 10, 12)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuBuluCore.TROUGH.put(Direction.WEST,
		Stream.of(
				Block.box(6, 14, 0, 9, 16, 2),
				Block.box(7, 14, 14, 10, 16, 16),
				Block.box(5, 10, 0, 10, 14, 3),
				Block.box(6, 10, 13, 11, 14, 16),
				Block.box(3, 10, 3, 13, 16, 13),
				Block.box(6.5, 0, 6, 9.5, 2, 10),
				Block.box(5, 2, 5, 11, 4, 11),
				Block.box(4, 4, 4, 12, 10, 12)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TapuBuluCore.TROUGH.get(state.getValue(TapuBuluCore.FACING));
    }
    
    public TapuBuluCore(final String name, final Properties props)
    {
    	super(name, props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TapuBuluCore.FACING, Direction.NORTH).setValue(
    			TapuBuluCore.WATERLOGGED, false));
    }
}