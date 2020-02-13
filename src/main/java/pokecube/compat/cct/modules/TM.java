package pokecube.compat.cct.modules;

import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;

public class TM extends BasePeripheral<TMTile>
{
    IItemHandler inventory = null;

    public TM(final TMTile tile)
    {
        super(tile, "tm_machine", "get_list", "apply_index");
        this.inventory = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
    }

    @Override
    public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
            final Object[] arguments) throws LuaException, InterruptedException
    {
        final IPokemob pokemob = PokecubeManager.itemToPokemob(this.inventory.getStackInSlot(1), this.tile.getWorld());
        switch (method)
        {
        case 0:
            if (!PokecubeManager.isFilled(this.inventory.getStackInSlot(1))) throw new LuaException(
                    "Requires a filled cube in the cube slot!");
            return this.tile.getMoves(pokemob);
        case 1:
            if (!PokecubeManager.isFilled(this.inventory.getStackInSlot(1))) throw new LuaException(
                    "Requires a filled cube in the cube slot!");
            if (this.inventory.getStackInSlot(0).isEmpty()) throw new LuaException("Requires a TM in the TM slot!");
            final String[] moves = this.tile.getMoves(pokemob);
            final int index = ArgumentHelper.getInt(arguments, 0);
            if (index < 0 || index > moves.length - 1) throw new LuaException("Argument must be in range [0, "
                    + moves.length + ")");
            this.tile.addMoveToTM(moves[index], this.inventory.getStackInSlot(0));
            return new Object[] { true };
        }
        return null;
    }

}
