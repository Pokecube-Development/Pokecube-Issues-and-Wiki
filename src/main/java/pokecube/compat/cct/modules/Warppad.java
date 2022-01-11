package pokecube.compat.cct.modules;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import pokecube.adventures.blocks.warp_pad.WarpPadTile;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.api.maths.Vector3;

public class Warppad extends BasePeripheral<WarpPadTile>
{
    public static class Provider
    {
        private final WarpPadTile tile;

        public Provider(final WarpPadTile tile)
        {
            this.tile = tile;
        }

        public float[] getDest()
        {
            final TeleDest dest = this.tile.getDest();

            return new float[]
            { dest.loc.pos().getX(), dest.loc.pos().getY(), dest.loc.pos().getZ() };
        }

        public boolean setDest(final int x, final int y, final int z) throws LuaException
        {
            final TeleDest dest = this.tile.getDest();
            dest.setLoc(GlobalPos.of(this.tile.getLevel().dimension(), new BlockPos(x, y, z)),
                    new Vector3().set(new BlockPos(x, y, z)).add(0.5, 0, 0.5));
            return true;
        }
    }

    private final Provider provider;

    public Warppad(final WarpPadTile tile)
    {
        super(tile, "warp_pad");
        this.provider = new Provider(tile);
    }

    @Override
    public Object getTarget()
    {
        return this.provider;
    }

}
