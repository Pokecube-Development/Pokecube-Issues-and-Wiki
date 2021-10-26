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

public class TroughBlock extends Rotates implements SimpleWaterloggedBlock
{
    private static final Map<Direction, VoxelShape> TROUGH  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	TroughBlock.TROUGH.put(Direction.NORTH, Shapes.or(
            Block.box(12, 6, 4, 13, 11, 12),
            Block.box(3, 6, 4, 4, 11, 12),
            Block.box(3, 6, 3, 13, 11, 4),
            Block.box(3, 6, 12, 13, 11, 13),
            Block.box(4, 5, 4, 12, 6, 12),
            Block.box(6, 2, 6, 10, 5, 10),
            Block.box(5, 3, 5, 11, 4, 11),
            Block.box(5, 1, 5, 11, 2, 11),
            Block.box(4, 0, 4, 12, 1, 12)).optimize());
    	TroughBlock.TROUGH.put(Direction.EAST, Shapes.or(
		    Block.box(12, 6, 4, 13, 11, 12),
            Block.box(3, 6, 4, 4, 11, 12),
            Block.box(3, 6, 3, 13, 11, 4),
            Block.box(3, 6, 12, 13, 11, 13),
            Block.box(4, 5, 4, 12, 6, 12),
            Block.box(6, 2, 6, 10, 5, 10),
            Block.box(5, 3, 5, 11, 4, 11),
            Block.box(5, 1, 5, 11, 2, 11),
            Block.box(4, 0, 4, 12, 1, 12)).optimize());
    	TroughBlock.TROUGH.put(Direction.SOUTH, Shapes.or(
		    Block.box(12, 6, 4, 13, 11, 12),
            Block.box(3, 6, 4, 4, 11, 12),
            Block.box(3, 6, 3, 13, 11, 4),
            Block.box(3, 6, 12, 13, 11, 13),
            Block.box(4, 5, 4, 12, 6, 12),
            Block.box(6, 2, 6, 10, 5, 10),
            Block.box(5, 3, 5, 11, 4, 11),
            Block.box(5, 1, 5, 11, 2, 11),
            Block.box(4, 0, 4, 12, 1, 12)).optimize());
    	TroughBlock.TROUGH.put(Direction.WEST, Shapes.or(
            Block.box(12, 6, 4, 13, 11, 12),
            Block.box(3, 6, 4, 4, 11, 12),
            Block.box(3, 6, 3, 13, 11, 4),
            Block.box(3, 6, 12, 13, 11, 13),
            Block.box(4, 5, 4, 12, 6, 12),
            Block.box(6, 2, 6, 10, 5, 10),
            Block.box(5, 3, 5, 11, 4, 11),
            Block.box(5, 1, 5, 11, 2, 11),
            Block.box(4, 0, 4, 12, 1, 12)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return TroughBlock.TROUGH.get(state.getValue(TroughBlock.FACING));
    }
    
    public TroughBlock(final Properties props)
    {
    	super(props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TroughBlock.FACING, Direction.NORTH).setValue(
    			TroughBlock.WATERLOGGED, false));
    }
}