package pokecube.core.blocks.nests;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import pokecube.core.blocks.InteractableBlock;

public class NestBlock extends InteractableBlock
{

    public NestBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new NestTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }
}
