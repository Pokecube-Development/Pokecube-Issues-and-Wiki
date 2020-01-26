package pokecube.adventures.blocks.commander;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.core.blocks.InteractableTile;

public class CommanderTile extends InteractableTile
{
    public static TileEntityType<? extends TileEntity> TYPE;

    public CommanderTile()
    {
        super(CommanderTile.TYPE);
    }

    public CommanderTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

}
