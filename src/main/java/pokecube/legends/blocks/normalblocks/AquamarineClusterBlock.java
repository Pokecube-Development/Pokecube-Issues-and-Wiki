package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
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
        BlockPos posDown = pos.relative(direction.DOWN);
        return world.getBlockState(posOpposite).isFaceSturdy(world, posOpposite, direction) || world.getBlockState(posDown).is(BlockInit.CRYSTALLIZED_CACTUS.get());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor world, BlockPos pos, BlockPos pos1)
    {
       if (state.getValue(WATERLOGGED))
       {
          world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
       }

       return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(world, pos)
               ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, state1, world, pos, pos1);
    }
    
    @Override
    public boolean isPathfindable(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final PathComputationType path)
    {
      return false;
    }
}
