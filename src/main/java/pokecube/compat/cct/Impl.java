package pokecube.compat.cct;

import java.util.Locale;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.commander.CommanderTile;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;

public class Impl
{

    private static boolean reged = false;

    public static void register()
    {
        if (!Impl.reged)
        {
            Impl.reged = true;
            PokecubeCore.LOGGER.info("Registering Pokecube CC Peripherals.");
            ComputerCraftAPI.registerPeripheralProvider(new PokecubePeripherals());
        }
    }

    public static class CommanderPeripheral implements IPeripheral
    {
        final CommanderTile tile;
        final String[]      methods;

        public CommanderPeripheral(final CommanderTile tile)
        {
            this.tile = tile;
            this.methods = new String[3];
            this.methods[0] = "set";
            this.methods[1] = "execute";
            this.methods[2] = "status";
            this.methods[3] = "help";
        }

        @Override
        public String getType()
        {
            return "poke_commander";
        }

        @Override
        public String[] getMethodNames()
        {
            return this.methods;
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

        @Override
        public boolean equals(final IPeripheral other)
        {
            return other instanceof CommanderPeripheral && ((CommanderPeripheral) other).tile == this.tile;
        }

    }

    public static class PokecubePeripherals implements IPeripheralProvider
    {
        @Override
        public IPeripheral getPeripheral(final World world, final BlockPos pos, final Direction side)
        {
            final TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof CommanderTile) return new CommanderPeripheral((CommanderTile) tile);
            return null;
        }
    }
}
