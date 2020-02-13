package pokecube.compat.cct.modules;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import pokecube.adventures.blocks.siphon.SiphonTile;

public class Siphon extends BasePeripheral<SiphonTile>
{

    public Siphon(final SiphonTile tile)
    {
        super(tile, "pokesiphon", "get_power");
    }

    @Override
    public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
            final Object[] arguments) throws LuaException, InterruptedException
    {
        return new Object[] { this.tile.energy.theoreticalOutput };
    }

}
