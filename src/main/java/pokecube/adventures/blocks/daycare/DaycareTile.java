package pokecube.adventures.blocks.daycare;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.core.blocks.InteractableTile;

public class DaycareTile extends InteractableTile
{
    public static TileEntityType<? extends TileEntity> TYPE;

    public DaycareTile()
    {
        super(DaycareTile.TYPE);
    }

    public DaycareTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

}
