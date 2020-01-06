package pokecube.adventures.blocks.commander;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;

public class CommanderTile extends InteractableTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(CommanderTile::new,
            PokecubeAdv.COMMANDER).build(null);

    public CommanderTile()
    {
        super(CommanderTile.TYPE);
    }

    public CommanderTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

}
