package pokecube.legends.blocks.customblocks;

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

import java.util.HashMap;
import java.util.Map;

public class TapuKokoCore extends Rotates implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> KOKO  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	TapuKokoCore.KOKO.put(Direction.NORTH, VoxelShapes.or(
			Block.box(3, 0, 3, 13, 9, 13),
			Block.box(4, 9, 4, 12, 16, 12),
			Block.box(6, 0, 1, 10, 3, 3),
			Block.box(7, 0, 0, 9, 2, 1),
			Block.box(12, 7, 6, 16, 12, 7),
			Block.box(0, 7, 6, 4, 12, 7)).optimize());
    	TapuKokoCore.KOKO.put(Direction.EAST, VoxelShapes.or(
			Block.box(3, 0, 3, 13, 9, 13),
			Block.box(4, 9, 4, 12, 16, 12),
			Block.box(13, 0, 6, 15, 3, 10),
			Block.box(15, 0, 7, 16, 2, 9),
			Block.box(9, 7, 12, 10, 12, 16),
			Block.box(9, 7, 0, 10, 12, 4)).optimize());
    	TapuKokoCore.KOKO.put(Direction.SOUTH, VoxelShapes.or(
			Block.box(3, 0, 3, 13, 9, 13),
			Block.box(4, 9, 4, 12, 16, 12),
			Block.box(6, 0, 13, 10, 3, 15),
			Block.box(7, 0, 15, 9, 2, 16),
			Block.box(0, 7, 9, 4, 12, 10),
			Block.box(12, 7, 9, 16, 12, 10)).optimize());
    	TapuKokoCore.KOKO.put(Direction.WEST, VoxelShapes.or(
			Block.box(3, 0, 3, 13, 9, 13),
			Block.box(4, 9, 4, 12, 16, 12),
			Block.box(1, 0, 6, 3, 3, 10),
			Block.box(0, 0, 7, 1, 2, 9),
			Block.box(6, 7, 0, 7, 12, 4),
			Block.box(6, 7, 12, 7, 12, 16)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TapuKokoCore.KOKO.get(state.getValue(TapuKokoCore.FACING));
    }
    
    public TapuKokoCore(final String name, final Properties props)
    {
    	super(name, props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TapuKokoCore.FACING, Direction.NORTH).setValue(
    			TapuKokoCore.WATERLOGGED, false));
    }
}