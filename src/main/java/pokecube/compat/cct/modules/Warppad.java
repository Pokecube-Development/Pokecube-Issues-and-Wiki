package pokecube.compat.cct.modules;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.world.DimensionType;
import pokecube.adventures.blocks.warppad.WarppadTile;
import pokecube.core.utils.PokecubeSerializer.TeleDest;

public class Warppad extends BasePeripheral<WarppadTile>
{
    public static class Provider
    {
        private final WarppadTile tile;

        public Provider(final WarppadTile tile)
        {
            this.tile = tile;
        }

        public float[] getDest()
        {
            final TeleDest dest = this.tile.getDest();

            return new float[] { dest.loc.x, dest.loc.y, dest.loc.z, dest.loc.w };
        }

        public boolean setDest(final int x, final int y, final int z) throws LuaException
        {
            return this.setDest(x, y, z, this.tile.getWorld().getDimension().getType().getId());
        }

        public boolean setDest(final int x, final int y, final int z, final int w) throws LuaException
        {
            final TeleDest dest = this.tile.getDest();
            dest.loc.x = x;
            dest.loc.y = y;
            dest.loc.z = z;
            final int dim = w;
            if (DimensionType.getById(dim) == null) throw new LuaException("No dimension by id " + dim);
            dest.loc.w = dim;
            return true;
        }
    }

    private final Provider provider;

    public Warppad(final WarppadTile tile)
    {
        super(tile, "warppad");
        this.provider = new Provider(tile);
    }

    @Override
    public Object getTarget()
    {
        return this.provider;
    }

}
