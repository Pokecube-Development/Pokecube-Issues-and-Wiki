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

public class TaoTrioBlock extends Rotates implements IWaterLoggable
{
    private static final DirectionProperty          FACING      = HorizontalBlock.HORIZONTAL_FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    private static final VoxelShape TAO = VoxelShapes.or(Block.makeCuboidShape(1, 0, 1, 15, 2, 15), 
    		Block.makeCuboidShape(2, 10, 2, 14, 13, 14), Block.makeCuboidShape(5, 13, 5, 11, 16, 11), 
            Block.makeCuboidShape(3, 2, 3, 13, 10, 13)).simplify();

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TaoTrioBlock.TAO;
    }

    public TaoTrioBlock(final String name, final Properties props)
    {
        super(name, props);
        this.setDefaultState(this.stateContainer.getBaseState().with(TaoTrioBlock.FACING, Direction.NORTH).with(
        		TaoTrioBlock.WATERLOGGED, false));
    }
}