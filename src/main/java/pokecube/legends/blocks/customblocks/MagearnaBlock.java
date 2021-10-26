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

public class MagearnaBlock extends Rotates implements SimpleWaterloggedBlock
{
	private static final Map<Direction, VoxelShape> MAGEARNA  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {
    	MagearnaBlock.MAGEARNA.put(Direction.NORTH, Shapes.or(
			Block.box(2, 1, 0, 14, 15, 16),
			Block.box(0, 2, 1, 16, 14, 15),
			Block.box(1, 0, 2, 15, 16, 14)).optimize());
		MagearnaBlock.MAGEARNA.put(Direction.EAST, Shapes.or(
			Block.box(0, 1, 2, 16, 15, 14),
			Block.box(1, 2, 0, 15, 14, 16),
			Block.box(2, 0, 1, 14, 16, 15)).optimize());
		MagearnaBlock.MAGEARNA.put(Direction.SOUTH, Shapes.or(
			Block.box(2, 1, 0, 14, 15, 16),
			Block.box(0, 2, 1, 16, 14, 15),
			Block.box(1, 0, 2, 15, 16, 14)).optimize());
		MagearnaBlock.MAGEARNA.put(Direction.WEST, Shapes.or(
			Block.box(0, 1, 2, 16, 15, 14),
			Block.box(1, 2, 0, 15, 14, 16),
			Block.box(2, 0, 1, 14, 16, 15)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return MagearnaBlock.MAGEARNA.get(state.getValue(MagearnaBlock.FACING));
    }

    public MagearnaBlock(final Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(MagearnaBlock.FACING, Direction.NORTH).setValue(
        		MagearnaBlock.WATERLOGGED, false));
    }
}