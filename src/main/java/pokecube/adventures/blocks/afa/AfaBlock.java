package pokecube.adventures.blocks.afa;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class AfaBlock extends InteractableHorizontalBlock
{

    public AfaBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockEntity createTileEntity(final BlockState state, final BlockGetter world)
    {
        return new AfaTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

}
