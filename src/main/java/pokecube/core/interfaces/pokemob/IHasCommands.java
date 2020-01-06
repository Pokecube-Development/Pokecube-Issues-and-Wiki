package pokecube.core.interfaces.pokemob;

import java.util.Map;

import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;

public interface IHasCommands
{
    public static enum Command
    {
        /** Sent to attack an entity. */
        ATTACKENTITY,
        /** Sent to attack a location. */
        ATTACKLOCATION,
        /** Sent to just use an attack */
        ATTACKNOTHING,
        /** Sent to change move index */
        CHANGEMOVEINDEX,
        /** Sent to change form */
        CHANGEFORM,
        /** Sent to order the pokemob to move somewhere */
        MOVETO,
        /** Sent to toggle between sit/stay/guard */
        STANCE,
        /** Sent to swap moves */
        SWAPMOVES,
        /** Sent to initiate teleport */
        TELEPORT;
    }

    public static interface IMobCommandHandler
    {
        boolean fromOwner();

        /**
         * Handles the command for the pokemob
         *
         * @param pokemob
         * @throws Exception
         *             - Something goes wrong, throw this, it will be logged.
         */
        void handleCommand(IPokemob pokemob) throws Exception;

        /**
         * Read message on server.
         *
         * @param buf
         */
        void readFromBuf(ByteBuf buf);

        IMobCommandHandler setFromOwner(boolean owner);

        /**
         * Write message to server.
         *
         * @param buf
         */
        void writeToBuf(ByteBuf buf);
    }

    /** These are what will be used to handle the commands sent in. */
    public static final Map<Command, Class<? extends IMobCommandHandler>> COMMANDHANDLERS = Maps.newHashMap();

    /**
     * Handles the given command
     *
     * @param command
     * @param handler
     */
    default void handleCommand(Command command, IMobCommandHandler handler)
    {
        final IPokemob pokemob = (IPokemob) this;
        try
        {
            handler.handleCommand(pokemob);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error Handling command for type " + command + " for mob " + pokemob.getEntity(),
                    e);
            PokecubeCore.LOGGER.error("Owner: " + pokemob.getOwner());
        }
    }
}
