package pokecube.core.blocks.bases;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.blocks.InteractableBlock;

/**
 * This block is the block for secret bases, it exists soley to be the
 * {@link EntityBlock} providing the bases ({@link BaseTile}). It inhertis from
 * {@link InteractableBlock} so that the right click gets forwarded to the
 * {@link BaseTile}
 *
 */
public class BaseBlock extends InteractableBlock implements EntityBlock
{

    public BaseBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new BaseTile(pos, state);
    }
}
