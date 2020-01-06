package pokecube.core.blocks.pc;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;

public class PCTile extends InteractableTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(PCTile::new,
            PokecubeItems.PCTOP, PokecubeItems.PCBASE).build(null);

    public PCTile()
    {
        this(PCTile.TYPE);
    }

    public PCTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

}
