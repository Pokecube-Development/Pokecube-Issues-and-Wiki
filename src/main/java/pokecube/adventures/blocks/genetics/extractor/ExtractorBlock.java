package pokecube.adventures.blocks.genetics.extractor;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class ExtractorBlock extends InteractableHorizontalBlock
{

    public ExtractorBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new ExtractorTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

}
