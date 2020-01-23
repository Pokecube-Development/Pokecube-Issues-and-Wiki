package pokecube.adventures.blocks.warppad;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import thut.api.entity.ThutTeleporter;
import thut.api.maths.Vector4;

public class WarppadTile extends InteractableTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(WarppadTile::new,
            PokecubeAdv.WARPPAD).build(null);

    public static void warp(final Entity entityIn, final TeleDest dest)
    {
        ThutTeleporter.transferTo(entityIn, dest.loc);
    }

    private TeleDest dest   = null;
    public int       energy = 0;

    public WarppadTile()
    {
        super(WarppadTile.TYPE);
    }

    public WarppadTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public TeleDest getDest()
    {
        if (this.dest == null) this.dest = new TeleDest(new Vector4(this.getPos().getX() + 0.5, this.getPos().getY()
                + 4, this.getPos().getZ() + 0.5, this.world.dimension.getType().getId()));
        return this.dest;
    }

    @Override
    public void onWalkedOn(final Entity entityIn)
    {
        // TODO check energy here first.
        WarppadTile.warp(entityIn, this.getDest());
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        if (compound.contains("dest"))
        {
            final CompoundNBT tag = compound.getCompound("dest");
            this.dest = TeleDest.readFromNBT(tag);
        }
        super.read(compound);
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        final CompoundNBT tag = new CompoundNBT();
        this.getDest().writeToNBT(tag);
        compound.put("dest", tag);
        return super.write(compound);
    }
}
