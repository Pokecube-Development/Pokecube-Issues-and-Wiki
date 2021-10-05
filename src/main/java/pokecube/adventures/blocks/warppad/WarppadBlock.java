package pokecube.adventures.blocks.warppad;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class WarppadBlock extends InteractableHorizontalBlock implements EntityBlock
{

    public WarppadBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new WarppadTile(pos, state);
    }

}
