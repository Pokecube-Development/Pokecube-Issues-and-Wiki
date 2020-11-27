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

public class KeldeoBlock extends Rotates implements IWaterLoggable
{
	private static final Map<Direction, VoxelShape> KELDEO  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.HORIZONTAL_FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	KeldeoBlock.KELDEO.put(Direction.NORTH,
		Stream.of(
		Block.makeCuboidShape(13, 13, 1, 15, 15, 3),
		Block.makeCuboidShape(1, 13, 13, 3, 15, 15),
		Block.makeCuboidShape(0, 11, 1, 16, 13, 15),
		Block.makeCuboidShape(0, 0, 0, 16, 2, 16),
		Block.makeCuboidShape(1, 13, 1, 3, 15, 3),
		Block.makeCuboidShape(13, 13, 13, 15, 15, 15),
		Block.makeCuboidShape(1, 2, 1, 3, 11, 3),
		Block.makeCuboidShape(1, 2, 13, 3, 11, 15),
		Block.makeCuboidShape(13, 2, 13, 15, 11, 15),
		Block.makeCuboidShape(13, 2, 1, 15, 11, 3),
		Block.makeCuboidShape(3, 2, 2, 13, 11, 14),
		Block.makeCuboidShape(3, 2, 1, 13, 11, 2),
		Block.makeCuboidShape(3, 2, 14, 13, 11, 15)
		).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get());
    	
    	KeldeoBlock.KELDEO.put(Direction.EAST,
		Stream.of(
		Block.makeCuboidShape(1, 13, 1, 3, 15, 3),
		Block.makeCuboidShape(13, 13, 13, 15, 15, 15),
		Block.makeCuboidShape(1, 11, 0, 15, 13, 16),
		Block.makeCuboidShape(0, 0, 0, 16, 2, 16),
		Block.makeCuboidShape(1, 13, 13, 3, 15, 15),
		Block.makeCuboidShape(13, 13, 1, 15, 15, 3),
		Block.makeCuboidShape(1, 2, 13, 3, 11, 15),
		Block.makeCuboidShape(13, 2, 13, 15, 11, 15),
		Block.makeCuboidShape(13, 2, 1, 15, 11, 3),
		Block.makeCuboidShape(1, 2, 1, 3, 11, 3),
		Block.makeCuboidShape(2, 2, 3, 14, 11, 13),
		Block.makeCuboidShape(1, 2, 3, 2, 11, 13),
		Block.makeCuboidShape(14, 2, 3, 15, 11, 13)
		).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get());
    	
    	KeldeoBlock.KELDEO.put(Direction.SOUTH,
		Stream.of(
		Block.makeCuboidShape(13, 13, 1, 15, 15, 3),
		Block.makeCuboidShape(1, 13, 13, 3, 15, 15),
		Block.makeCuboidShape(0, 11, 1, 16, 13, 15),
		Block.makeCuboidShape(0, 0, 0, 16, 2, 16),
		Block.makeCuboidShape(1, 13, 1, 3, 15, 3),
		Block.makeCuboidShape(13, 13, 13, 15, 15, 15),
		Block.makeCuboidShape(1, 2, 1, 3, 11, 3),
		Block.makeCuboidShape(1, 2, 13, 3, 11, 15),
		Block.makeCuboidShape(13, 2, 13, 15, 11, 15),
		Block.makeCuboidShape(13, 2, 1, 15, 11, 3),
		Block.makeCuboidShape(3, 2, 2, 13, 11, 14),
		Block.makeCuboidShape(3, 2, 1, 13, 11, 2),
		Block.makeCuboidShape(3, 2, 14, 13, 11, 15)
		).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get());
    	
    	KeldeoBlock.KELDEO.put(Direction.WEST,
		Stream.of(
		Block.makeCuboidShape(1, 13, 1, 3, 15, 3),
		Block.makeCuboidShape(13, 13, 13, 15, 15, 15),
		Block.makeCuboidShape(1, 11, 0, 15, 13, 16),
		Block.makeCuboidShape(0, 0, 0, 16, 2, 16),
		Block.makeCuboidShape(1, 13, 13, 3, 15, 15),
		Block.makeCuboidShape(13, 13, 1, 15, 15, 3),
		Block.makeCuboidShape(1, 2, 13, 3, 11, 15),
		Block.makeCuboidShape(13, 2, 13, 15, 11, 15),
		Block.makeCuboidShape(13, 2, 1, 15, 11, 3),
		Block.makeCuboidShape(1, 2, 1, 3, 11, 3),
		Block.makeCuboidShape(2, 2, 3, 14, 11, 13),
		Block.makeCuboidShape(1, 2, 3, 2, 11, 13),
		Block.makeCuboidShape(14, 2, 3, 15, 11, 13)
		).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get());
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return KeldeoBlock.KELDEO.get(state.get(KeldeoBlock.FACING));
    }

    public KeldeoBlock(final String name, final Properties props)
    {
        super(name, props);
        this.setDefaultState(this.stateContainer.getBaseState().with(KeldeoBlock.FACING, Direction.NORTH).with(
        		KeldeoBlock.WATERLOGGED, false));
    }
}