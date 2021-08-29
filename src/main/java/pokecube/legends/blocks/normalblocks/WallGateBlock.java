package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class WallGateBlock extends FenceGateBlock
{
    public WallGateBlock(Properties properties) {
    	super(properties);
	}

	@Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return ((Direction)state.getValue(FACING)).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }
}
