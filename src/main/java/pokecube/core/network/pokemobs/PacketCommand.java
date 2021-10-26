package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackEntityHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackLocationHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackNothingHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.MoveIndexHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.MoveToHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.SwapMovesHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
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
        {
        }

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

    int                entityId;
    IMobCommandHandler handler;

    Command command;

    public PacketCommand()
    {
        super(null);
    }

    public PacketCommand(final PacketBuffer buf)
    {
        super(buf);
        this.entityId = buf.readInt();
        this.command = Command.values()[buf.readByte()];
        try
        {
            this.handler = IHasCommands.COMMANDHANDLERS.get(this.command).newInstance();
            this.handler.readFromBuf(buf);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error handling a command to a pokemob", e);
            this.handler = new DefaultHandler();
        }
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final Entity user = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), this.entityId,
                true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(user);
        if (pokemob == null) return;
        pokemob.handleCommand(this.command, this.handler);
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        buf.writeInt(this.entityId);
        buf.writeByte(this.command.ordinal());
        this.handler.writeToBuf(buf);
    }

}
