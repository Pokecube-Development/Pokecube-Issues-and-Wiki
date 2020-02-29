package pokecube.compat.cct.modules;

import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.world.dimension.DimensionType;
import pokecube.adventures.blocks.warppad.WarppadTile;
import pokecube.core.utils.PokecubeSerializer.TeleDest;

public class Warppad extends BasePeripheral<WarppadTile>
{

    public Warppad(final WarppadTile tile)
    {
        super(tile, "warppad", "get_dest", "set_dest");
    }

    @Override
    public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
            final Object[] arguments) throws LuaException, InterruptedException
    {
        final TeleDest dest = this.tile.getDest();
        switch (method)
        {
        case 0:
            return new Object[] { dest.loc.x, dest.loc.y, dest.loc.z, dest.loc.w };
        case 1:
            dest.loc.x = (float) ArgumentHelper.getDouble(arguments, 0);
            dest.loc.y = (float) ArgumentHelper.getDouble(arguments, 1);
            dest.loc.z = (float) ArgumentHelper.getDouble(arguments, 2);
            if (arguments.length == 4)
            {
                final int dim = ArgumentHelper.getInt(arguments, 3);
                if (DimensionType.getById(dim) == null) throw new LuaException("No dimension by id " + dim);
                dest.loc.w = dim;
            }
            return new Object[] { true };
        }
        return null;
    }

}
