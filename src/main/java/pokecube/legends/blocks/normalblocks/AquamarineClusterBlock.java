package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import pokecube.legends.init.BlockInit;

public class AquamarineClusterBlock extends AmethystClusterBlock implements SimpleWaterloggedBlock
{
    public AquamarineClusterBlock(int height, int width, final Properties properties)
    {
        super(height, width, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.UP));
    }
    
    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        Direction direction = state.getValue(FACING);
        BlockPos posOpposite = pos.relative(direction.getOpposite());
        return world.getBlockState(posOpposite).isFaceSturdy(world, posOpposite, direction) || state.is(BlockInit.CRYSTALLIZED_CACTUS.get());
    }
    
    @Override
    public boolean isPathfindable(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final PathComputationType path)
    {
      return false;
    }
}
