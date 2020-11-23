package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.Map;

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

public class HeatranBlock extends Rotates implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> HEATRAN  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.HORIZONTAL_FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	HeatranBlock.HEATRAN.put(Direction.NORTH,
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 6, 3, 13, 10, 13),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 10, 2, 14, 12, 14),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 4, 2, 14, 6, 14),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 12, 1, 15, 14, 15),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 2, 1, 15, 4, 15),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 14, 0, 16, 16, 16),
				Block.makeCuboidShape(0, 0, 0, 16, 2, 16),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	HeatranBlock.HEATRAN.put(Direction.EAST,
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 6, 3, 13, 10, 13),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 10, 2, 14, 12, 14),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 4, 2, 14, 6, 14),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 12, 1, 15, 14, 15),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 2, 1, 15, 4, 15),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 14, 0, 16, 16, 16),
				Block.makeCuboidShape(0, 0, 0, 16, 2, 16),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	HeatranBlock.HEATRAN.put(Direction.SOUTH,
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 6, 3, 13, 10, 13),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 10, 2, 14, 12, 14),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 4, 2, 14, 6, 14),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 12, 1, 15, 14, 15),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 2, 1, 15, 4, 15),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 14, 0, 16, 16, 16),
				Block.makeCuboidShape(0, 0, 0, 16, 2, 16),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	HeatranBlock.HEATRAN.put(Direction.WEST,
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 6, 3, 13, 10, 13),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 10, 2, 14, 12, 14),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 4, 2, 14, 6, 14),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 12, 1, 15, 14, 15),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 2, 1, 15, 4, 15),
		VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 14, 0, 16, 16, 16),
				Block.makeCuboidShape(0, 0, 0, 16, 2, 16),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return HeatranBlock.HEATRAN.get(state.get(HeatranBlock.FACING));
    }

    public HeatranBlock(final String name, final Properties props)
    {
        super(name, props);
        this.setDefaultState(this.stateContainer.getBaseState().with(HeatranBlock.FACING, Direction.NORTH).with(
        		HeatranBlock.WATERLOGGED, false));
    }
}