package pokecube.core.blocks.pc;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.core.blocks.InteractableTile;

public class PCTile extends InteractableTile
{
    public static TileEntityType<? extends TileEntity> TYPE;

    public PCTile()
    {
        this(PCTile.TYPE);
    }

    public PCTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

}
