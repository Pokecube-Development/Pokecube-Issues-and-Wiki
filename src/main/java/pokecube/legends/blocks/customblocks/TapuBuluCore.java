package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TapuBuluCore extends Rotates implements SimpleWaterloggedBlock
{
    private static final Map<Direction, VoxelShape> BULU  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	TapuBuluCore.BULU.put(Direction.NORTH, Shapes.or(
			Block.box(5, 0, 5, 11, 4, 11),
			Block.box(4, 4, 4, 12, 10, 12),
			Block.box(3, 10, 3, 13, 16, 13),
			Block.box(13, 10, 6, 16, 14, 10),
			Block.box(14, 14, 7, 16, 16, 9),
			Block.box(0, 10, 6, 3, 14, 10),
			Block.box(0, 14, 7, 2, 16, 9)).optimize());
    	TapuBuluCore.BULU.put(Direction.EAST, Shapes.or(
			Block.box(5, 0, 5, 11, 4, 11),
			Block.box(4, 4, 4, 12, 10, 12),
			Block.box(3, 10, 3, 13, 16, 13),
			Block.box(6, 10, 13, 10, 14, 16),
			Block.box(7, 14, 14, 9, 16, 16),
			Block.box(6, 10, 0, 10, 14, 3),
			Block.box(7, 14, 0, 9, 16, 2)).optimize());
    	TapuBuluCore.BULU.put(Direction.SOUTH, Shapes.or(
			Block.box(5, 0, 5, 11, 4, 11),
			Block.box(4, 4, 4, 12, 10, 12),
			Block.box(3, 10, 3, 13, 16, 13),
			Block.box(13, 10, 6, 16, 14, 10),
			Block.box(14, 14, 7, 16, 16, 9),
			Block.box(0, 10, 6, 3, 14, 10),
			Block.box(0, 14, 7, 2, 16, 9)).optimize());
    	TapuBuluCore.BULU.put(Direction.WEST, Shapes.or(
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
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
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