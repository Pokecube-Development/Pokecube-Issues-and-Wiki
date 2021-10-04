package pokecube.compat.cct.modules;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import pokecube.adventures.blocks.warppad.WarppadTile;
import thut.api.entity.ThutTeleporter.TeleDest;

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

            return new float[] { dest.loc.pos().getX(), dest.loc.pos().getY(), dest.loc.pos().getZ() };
        }

        public boolean setDest(final int x, final int y, final int z) throws LuaException
        {
            final TeleDest dest = this.tile.getDest();
            dest.setPos(GlobalPos.of(this.tile.getLevel().dimension(), new BlockPos(x, y, z)));
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
