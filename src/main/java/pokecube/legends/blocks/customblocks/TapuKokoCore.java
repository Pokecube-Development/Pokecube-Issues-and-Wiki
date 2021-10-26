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

public class TapuKokoCore extends Rotates implements SimpleWaterloggedBlock
{
    private static final Map<Direction, VoxelShape> KOKO  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	TapuKokoCore.KOKO.put(Direction.NORTH, Shapes.or(
			Block.box(3, 0, 3, 13, 9, 13),
			Block.box(4, 9, 4, 12, 16, 12),
			Block.box(6, 0, 1, 10, 3, 3),
			Block.box(7, 0, 0, 9, 2, 1),
			Block.box(12, 7, 6, 16, 12, 7),
			Block.box(0, 7, 6, 4, 12, 7)).optimize());
    	TapuKokoCore.KOKO.put(Direction.EAST, Shapes.or(
			Block.box(3, 0, 3, 13, 9, 13),
			Block.box(4, 9, 4, 12, 16, 12),
			Block.box(13, 0, 6, 15, 3, 10),
			Block.box(15, 0, 7, 16, 2, 9),
			Block.box(9, 7, 12, 10, 12, 16),
			Block.box(9, 7, 0, 10, 12, 4)).optimize());
    	TapuKokoCore.KOKO.put(Direction.SOUTH, Shapes.or(
			Block.box(3, 0, 3, 13, 9, 13),
			Block.box(4, 9, 4, 12, 16, 12),
			Block.box(6, 0, 13, 10, 3, 15),
			Block.box(7, 0, 15, 9, 2, 16),
			Block.box(0, 7, 9, 4, 12, 10),
			Block.box(12, 7, 9, 16, 12, 10)).optimize());
    	TapuKokoCore.KOKO.put(Direction.WEST, Shapes.or(
			Block.box(3, 0, 3, 13, 9, 13),
			Block.box(4, 9, 4, 12, 16, 12),
			Block.box(1, 0, 6, 3, 3, 10),
			Block.box(0, 0, 7, 1, 2, 9),
			Block.box(6, 7, 0, 7, 12, 4),
			Block.box(6, 7, 12, 7, 12, 16)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return TapuKokoCore.KOKO.get(state.getValue(TapuKokoCore.FACING));
    }
    
    public TapuKokoCore(final Properties props)
    {
    	super(props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TapuKokoCore.FACING, Direction.NORTH).setValue(
    			TapuKokoCore.WATERLOGGED, false));
    }
}