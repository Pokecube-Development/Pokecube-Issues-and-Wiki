package pokecube.adventures.blocks.warp_pad;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class WarpPadBlock extends InteractableHorizontalBlock implements EntityBlock
{

    public WarpPadBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new WarpPadTile(pos, state);
    }

}
