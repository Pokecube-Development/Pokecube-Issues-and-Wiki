package pokecube.core.blocks.barrels;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GenericBarrelBlock extends BarrelBlock
{

    public GenericBarrelBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new GenericBarrelTile(pos, state);
    }

}
