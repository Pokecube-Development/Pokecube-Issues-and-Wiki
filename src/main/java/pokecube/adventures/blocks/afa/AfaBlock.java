package pokecube.adventures.blocks.afa;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class AfaBlock extends InteractableHorizontalBlock
{

    public AfaBlock(final Properties properties, final MaterialColor color)
    {
        super(properties, color);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new AfaTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

}
