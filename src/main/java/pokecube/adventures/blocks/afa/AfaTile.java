package pokecube.adventures.blocks.afa;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.core.blocks.InteractableTile;

public class AfaTile extends InteractableTile
{
    public static TileEntityType<? extends TileEntity> TYPE;

    public AfaTile()
    {
        super(AfaTile.TYPE);
    }

    public AfaTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

}
