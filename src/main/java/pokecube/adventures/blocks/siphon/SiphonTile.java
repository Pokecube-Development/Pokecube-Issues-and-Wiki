package pokecube.adventures.blocks.siphon;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;

public class SiphonTile extends InteractableTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(SiphonTile::new,
            PokecubeAdv.SIPHON).build(null);

    public SiphonTile()
    {
        super(SiphonTile.TYPE);
    }

    public SiphonTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

}
