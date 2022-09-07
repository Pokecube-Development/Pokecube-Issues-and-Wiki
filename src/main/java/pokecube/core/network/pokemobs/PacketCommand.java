package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IHasCommands;
import pokecube.api.entity.pokemob.IHasCommands.Command;
import pokecube.api.entity.pokemob.IHasCommands.IMobCommandHandler;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.commandhandlers.AttackEntityHandler;
import pokecube.api.entity.pokemob.commandhandlers.AttackLocationHandler;
import pokecube.api.entity.pokemob.commandhandlers.AttackNothingHandler;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.api.entity.pokemob.commandhandlers.MoveIndexHandler;
import pokecube.api.entity.pokemob.commandhandlers.MoveToHandler;
import pokecube.api.entity.pokemob.commandhandlers.StanceHandler;
import pokecube.api.entity.pokemob.commandhandlers.SwapMovesHandler;
import pokecube.api.entity.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.PokecubeCore;
import thut.core.common.network.Packet;

public class PacketCommand extends Packet
{
    public static class DefaultHandler implements IMobCommandHandler
    {
        boolean byOwner;

        @Override
        public boolean fromOwner()
        {
            return this.byOwner;
        }

        @Override
        public void handleCommand(final IPokemob pokemob) throws Exception
        {}

        @Override
        public void readFromBuf(final ByteBuf buf)
        {
            this.setFromOwner(buf.readBoolean());
        }

        @Override
        public IMobCommandHandler setFromOwner(final boolean owner)
        {
            this.byOwner = owner;
            return this;
        }

        @Override
        public void writeToBuf(final ByteBuf buf)
        {
            buf.writeBoolean(this.fromOwner());
        }

    }

    // Register default command handlers
    static
    {
        // Only populate this if someone else hasn't override in.
        if (IHasCommands.COMMANDHANDLERS.isEmpty())
        {
            IHasCommands.COMMANDHANDLERS.put(Command.ATTACKENTITY, AttackEntityHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.ATTACKLOCATION, AttackLocationHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.ATTACKNOTHING, AttackNothingHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.CHANGEFORM, ChangeFormHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.CHANGEMOVEINDEX, MoveIndexHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.MOVETO, MoveToHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.STANCE, StanceHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.SWAPMOVES, SwapMovesHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.TELEPORT, TeleportHandler.class);
        }
    }

    public static void init()
    {

    }

    public static void sendCommand(final IPokemob pokemob, final Command command, final IMobCommandHandler handler)
    {
        final PacketCommand packet = new PacketCommand();
        packet.entityId = pokemob.getEntity().getId();
        packet.command = command;
        packet.handler = handler;
        PokecubeCore.packets.sendToServer(packet);
    }

    int entityId;
    IMobCommandHandler handler;

    Command command;

    public PacketCommand()
    {
        super(null);
    }

    public PacketCommand(final FriendlyByteBuf buf)
    {
        super(buf);
        this.entityId = buf.readInt();
        this.command = Command.values()[buf.readByte()];
        try
        {
            this.handler = IHasCommands.COMMANDHANDLERS.get(this.command).getConstructor().newInstance();
            this.handler.readFromBuf(buf);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error handling a command to a pokemob", e);
            this.handler = new DefaultHandler();
        }
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final Entity user = PokecubeAPI.getEntityProvider().getEntity(player.getLevel(), this.entityId, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(user);
        if (pokemob == null) return;
        pokemob.handleCommand(this.command, this.handler);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.entityId);
        buf.writeByte(this.command.ordinal());
        this.handler.writeToBuf(buf);
    }

}
