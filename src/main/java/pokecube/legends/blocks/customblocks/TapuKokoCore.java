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

public class TapuKokoCore extends Rotates implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> TROUGH  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	TapuKokoCore.TROUGH.put(Direction.NORTH,
    	Stream.of(
		    	Block.box(7, 0, 0, 9, 2, 2),
				Block.box(3, 0, 4, 13, 9, 14),
				Block.box(3.5, 9, 4.5, 12.5, 16, 13.5),
				Block.box(0, 7, 9, 4, 12, 10),
				Block.box(12, 7, 9, 16, 12, 10),
				Block.box(6, 0, 2, 10, 3, 4)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuKokoCore.TROUGH.put(Direction.EAST,
		Stream.of(
				Block.box(14, 0, 7, 16, 2, 9),
				Block.box(2, 0, 3, 12, 9, 13),
				Block.box(2.5, 9, 3.5, 11.5, 16, 12.5),
				Block.box(6, 7, 0, 7, 12, 4),
				Block.box(6, 7, 12, 7, 12, 16),
				Block.box(12, 0, 6, 14, 3, 10)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuKokoCore.TROUGH.put(Direction.SOUTH,
		Stream.of(
				Block.box(7, 0, 14, 9, 2, 16),
				Block.box(3, 0, 2, 13, 9, 12),
				Block.box(3.5, 9, 2.5, 12.5, 16, 11.5),
				Block.box(12, 7, 6, 16, 12, 7),
				Block.box(0, 7, 6, 4, 12, 7),
				Block.box(6, 0, 12, 10, 3, 14)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    	TapuKokoCore.TROUGH.put(Direction.WEST,
		Stream.of(
				Block.box(0, 0, 7, 2, 2, 9),
				Block.box(4, 0, 3, 14, 9, 13),
				Block.box(4.5, 9, 3.5, 13.5, 16, 12.5),
				Block.box(9, 7, 12, 10, 12, 16),
				Block.box(9, 7, 0, 10, 12, 4),
				Block.box(2, 0, 6, 4, 3, 10)
		).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get());
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TapuKokoCore.TROUGH.get(state.getValue(TapuKokoCore.FACING));
    }
    
    public TapuKokoCore(final String name, final Properties props)
    {
    	super(name, props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TapuKokoCore.FACING, Direction.NORTH).setValue(
    			TapuKokoCore.WATERLOGGED, false));
    }
}