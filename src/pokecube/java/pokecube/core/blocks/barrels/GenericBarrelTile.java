package pokecube.core.blocks.barrels;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GenericBarrelTile extends BarrelBlockEntity
{

    public GenericBarrelTile(BlockPos pos, BlockState blockState)
    {
        super(pos, blockState);
    }

    @Override
    public boolean isValidBlockState(BlockState state)
    {
        return super.isValidBlockState(state) || state.getBlock() instanceof GenericBarrelBlock;
    }
}
