package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KeldeoBlock extends Rotates implements SimpleWaterloggedBlock
{
	private static final Map<Direction, VoxelShape> KELDEO  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	KeldeoBlock.KELDEO.put(Direction.NORTH,
			Stream.of(
			Block.box(13, 13, 1, 15, 15, 3),
			Block.box(1, 13, 13, 3, 15, 15),
			Block.box(0, 11, 1, 16, 13, 15),
			Block.box(0, 0, 0, 16, 2, 16),
			Block.box(1, 13, 1, 3, 15, 3),
			Block.box(13, 13, 13, 15, 15, 15),
			Block.box(1, 2, 1, 3, 11, 3),
			Block.box(1, 2, 13, 3, 11, 15),
			Block.box(13, 2, 13, 15, 11, 15),
			Block.box(13, 2, 1, 15, 11, 3),
			Block.box(3, 2, 2, 13, 11, 14))
			.reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get());
    	
    	KeldeoBlock.KELDEO.put(Direction.EAST,
			Stream.of(
			Block.box(1, 13, 1, 3, 15, 3),
			Block.box(13, 13, 13, 15, 15, 15),
			Block.box(1, 11, 0, 15, 13, 16),
			Block.box(0, 0, 0, 16, 2, 16),
			Block.box(1, 13, 13, 3, 15, 15),
			Block.box(13, 13, 1, 15, 15, 3),
			Block.box(1, 2, 13, 3, 11, 15),
			Block.box(13, 2, 13, 15, 11, 15),
			Block.box(13, 2, 1, 15, 11, 3),
			Block.box(1, 2, 1, 3, 11, 3),
			Block.box(2, 2, 3, 14, 11, 13))
			.reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get());
    	
    	KeldeoBlock.KELDEO.put(Direction.SOUTH,
			Stream.of(
			Block.box(13, 13, 1, 15, 15, 3),
			Block.box(1, 13, 13, 3, 15, 15),
			Block.box(0, 11, 1, 16, 13, 15),
			Block.box(0, 0, 0, 16, 2, 16),
			Block.box(1, 13, 1, 3, 15, 3),
			Block.box(13, 13, 13, 15, 15, 15),
			Block.box(1, 2, 1, 3, 11, 3),
			Block.box(1, 2, 13, 3, 11, 15),
			Block.box(13, 2, 13, 15, 11, 15),
			Block.box(13, 2, 1, 15, 11, 3),
			Block.box(3, 2, 2, 13, 11, 14))
			.reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get());
    	
    	KeldeoBlock.KELDEO.put(Direction.WEST,
			Stream.of(
			Block.box(1, 13, 1, 3, 15, 3),
			Block.box(13, 13, 13, 15, 15, 15),
			Block.box(1, 11, 0, 15, 13, 16),
			Block.box(0, 0, 0, 16, 2, 16),
			Block.box(1, 13, 13, 3, 15, 15),
			Block.box(13, 13, 1, 15, 15, 3),
			Block.box(1, 2, 13, 3, 11, 15),
			Block.box(13, 2, 13, 15, 11, 15),
			Block.box(13, 2, 1, 15, 11, 3),
			Block.box(1, 2, 1, 3, 11, 3),
			Block.box(2, 2, 3, 14, 11, 13))
			.reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get());
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return KeldeoBlock.KELDEO.get(state.getValue(KeldeoBlock.FACING));
    }

    public KeldeoBlock(Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(KeldeoBlock.FACING, Direction.NORTH).setValue(
        		KeldeoBlock.WATERLOGGED, false));
    }
}