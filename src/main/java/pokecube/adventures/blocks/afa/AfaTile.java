package pokecube.adventures.blocks.afa;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;

public class AfaTile extends InteractableTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(AfaTile::new,
            PokecubeAdv.AFA).build(null);

    public AfaTile()
    {
        super(AfaTile.TYPE);
    }

    public AfaTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

}
