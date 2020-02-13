package pokecube.compat.cct.modules;

import java.util.Locale;

import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import pokecube.adventures.blocks.commander.CommanderTile;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;

public class Commander extends BasePeripheral<CommanderTile>
{

    public Commander(final CommanderTile tile)
    {
        super(tile, "poke_commander", "set", "execute", "status", "help");
    }

    @Override
    public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
            final Object[] arguments) throws LuaException, InterruptedException
    {
        switch (method)
        {
        case 0:
            // This is setting the command, returns a boolean as to whether
            // it set correctly.
            final String type = ArgumentHelper.getString(arguments, 0).toUpperCase(Locale.ROOT);
            Command command_ = null;
            try
            {
                command_ = Command.valueOf(type);
            }
            catch (final Exception e1)
            {
                throw new LuaException("Try using help to get list of commands");
            }
            String args = ArgumentHelper.getString(arguments, 1);
            for (int i = 2; i < arguments.length; i++)
                args = args + " " + ArgumentHelper.getString(arguments, i);
            try
            {
                this.tile.setCommand(command_, args);
                return new Object[] { true };
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error setting commander block command via computer", e);
                throw new LuaException("Error with settig command");
            }
        case 1:
            if (this.tile.command == null) throw new LuaException("No Command set!");
            if (this.tile.pokeID == null) throw new LuaException("No pokemob linked!");
            try
            {
                this.tile.sendCommand();
                return new Object[] { true };
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error executing commander block command via computer", e);
                throw new LuaException("Error with executing command");
            }
        case 3:
            final String[] names = new String[Command.values().length];
            for (int i = 0; i < names.length; i++)
                names[i] = Command.values()[i].name();
            return names;

        default:
            throw new LuaException("Unimplemented Argument!");
        }
    }
}
