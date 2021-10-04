package pokecube.core.blocks.bases;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.blocks.InteractableBlock;

public class BaseBlock extends InteractableBlock
{

    public BaseBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockEntity createTileEntity(final BlockState state, final BlockGetter world)
    {
        return new BaseTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }
}
