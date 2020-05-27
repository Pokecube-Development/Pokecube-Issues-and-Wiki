package pokecube.compat.cct.modules;

import dan200.computercraft.api.lua.LuaException;
import pokecube.adventures.blocks.commander.CommanderTile;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;

public class Commander extends BasePeripheral<CommanderTile>
{
    public static class Provider
    {
        final CommanderTile tile;

        public Provider(final CommanderTile tile)
        {
            this.tile = tile;
        }

        public boolean set(final String type, final String args) throws LuaException
        {
            Command command_ = null;
            try
            {
                command_ = Command.valueOf(type);
            }
            catch (final Exception e1)
            {
                throw new LuaException("Try using help to get list of commands");
            }
            try
            {
                this.tile.setCommand(command_, args);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error setting commander block command via computer", e);
                throw new LuaException("Error with settig command");
            }
            return true;
        }

        public boolean execute() throws LuaException
        {
            if (this.tile.command == null) throw new LuaException("No Command set!");
            if (this.tile.pokeID == null) throw new LuaException("No pokemob linked!");
            try
            {
                this.tile.sendCommand();
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error executing commander block command via computer", e);
                throw new LuaException("Error with executing command");
            }
            return true;
        }

        public String[] options()
        {
            final String[] names = new String[Command.values().length];
            for (int i = 0; i < names.length; i++)
                names[i] = Command.values()[i].name();
            return names;
        }

        public void help()
        {

        }
    }

    private final Provider provider;

    public Commander(final CommanderTile tile)
    {
        super(tile, "poke_commander");
        this.provider = new Provider(tile);
    }

    @Override
    public Object getTarget()
    {
        return this.provider;
    }
}
