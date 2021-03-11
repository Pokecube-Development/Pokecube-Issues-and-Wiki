package pokecube.compat.cct.modules;

import dan200.computercraft.api.lua.LuaException;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;

public class TM extends BasePeripheral<TMTile>
{
    public static class Provider
    {
        private final TMTile                 tile;
        private final IItemHandlerModifiable inventory;

        public Provider(final TMTile tile)
        {
            this.tile = tile;
            this.inventory = (IItemHandlerModifiable) tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .orElse(null);
        }

        public String[] getList() throws LuaException
        {
            final IPokemob pokemob = PokecubeManager.itemToPokemob(this.inventory.getStackInSlot(1), this.tile
                    .getLevel());
            if (!PokecubeManager.isFilled(this.inventory.getStackInSlot(1))) throw new LuaException(
                    "Requires a filled cube in the cube slot!");
            return this.tile.getMoves(pokemob);
        }

        public boolean applyIndex(final int index) throws LuaException
        {
            final IPokemob pokemob = PokecubeManager.itemToPokemob(this.inventory.getStackInSlot(1), this.tile
                    .getLevel());
            if (!PokecubeManager.isFilled(this.inventory.getStackInSlot(1))) throw new LuaException(
                    "Requires a filled cube in the cube slot!");
            if (this.inventory.getStackInSlot(0).isEmpty()) throw new LuaException("Requires a TM in the TM slot!");
            final String[] moves = this.tile.getMoves(pokemob);
            if (index < 0 || index > moves.length - 1) throw new LuaException("Argument must be in range [0, "
                    + moves.length + ")");
            this.tile.addMoveToTM(moves[index], this.inventory.getStackInSlot(0));
            return true;
        }
    }

    private final Provider provider;

    public TM(final TMTile tile)
    {
        super(tile, "tm_machine");
        this.provider = new Provider(tile);
    }

    @Override
    public Object getTarget()
    {
        return this.provider;
    }

}
