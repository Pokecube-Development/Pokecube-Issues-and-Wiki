package pokecube.adventures.blocks.daycare;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;

public class DaycareTile extends InteractableTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(DaycareTile::new,
            PokecubeAdv.DAYCARE).build(null);

    public DaycareTile()
    {
        super(DaycareTile.TYPE);
    }

    public DaycareTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

}
