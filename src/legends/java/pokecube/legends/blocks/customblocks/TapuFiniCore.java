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

public class TapuFiniCore extends Rotates implements SimpleWaterloggedBlock
{
    private static final Map<Direction, VoxelShape> FINI  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	TapuFiniCore.FINI.put(Direction.NORTH, Shapes.or(
			Block.box(4, 0, 4, 12, 3, 12),
			Block.box(3, 3, 3, 13, 13, 13),
			Block.box(4, 13, 4, 12, 16, 12),
			Block.box(7, 7, 0, 9, 9, 3),
			Block.box(13, 5, 6, 16, 6, 10),
			Block.box(0, 5, 6, 3, 6, 10)).optimize());
    	TapuFiniCore.FINI.put(Direction.EAST, Shapes.or(
			Block.box(4, 0, 4, 12, 3, 12),
			Block.box(3, 3, 3, 13, 13, 13),
			Block.box(4, 13, 4, 12, 16, 12),
			Block.box(13, 7, 7, 16, 9, 9),
			Block.box(6, 5, 13, 10, 6, 16),
			Block.box(6, 5, 0, 10, 6, 3)).optimize());
    	TapuFiniCore.FINI.put(Direction.SOUTH, Shapes.or(
			Block.box(4, 0, 4, 12, 3, 12),
			Block.box(3, 3, 3, 13, 13, 13),
			Block.box(4, 13, 4, 12, 16, 12),
			Block.box(7, 7, 13, 9, 9, 16),
			Block.box(13, 5, 6, 16, 6, 10),
			Block.box(0, 5, 6, 3, 6, 10)).optimize());
    	TapuFiniCore.FINI.put(Direction.WEST, Shapes.or(
			Block.box(4, 0, 4, 12, 3, 12),
			Block.box(3, 3, 3, 13, 13, 13),
			Block.box(4, 13, 4, 12, 16, 12),
			Block.box(0, 7, 7, 3, 9, 9),
			Block.box(6, 5, 13, 10, 6, 16),
			Block.box(6, 5, 0, 10, 6, 3)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return TapuFiniCore.FINI.get(state.getValue(TapuFiniCore.FACING));
    }
    
    public TapuFiniCore(final Properties props)
    {
    	super(props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TapuFiniCore.FACING, Direction.NORTH).setValue(
    			TapuFiniCore.WATERLOGGED, false));
    }
}