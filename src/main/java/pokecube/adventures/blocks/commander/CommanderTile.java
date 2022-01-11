package pokecube.adventures.blocks.commander;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.network.PacketCommander;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class CommanderTile extends InteractableTile
{
    protected boolean addedToNetwork = false;
    public UUID pokeID = null;
    public Command command = null;
    public IMobCommandHandler handler = null;
    public String args = "";
    private String prev_args = "";
    protected int power = 0;

    public CommanderTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeAdv.COMMANDER_TYPE.get(), pos, state);
    }

    public CommanderTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
    }

    public void setCommand(final Command command, final String args) throws Exception
    {
        this.command = command;
        this.args = args;
        if (command != null) this.initCommand();
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag)
    {
        this.load(tag);
    }

    @Override
    public void load(final CompoundTag nbt)
    {
        super.load(nbt);
        if (nbt.hasUUID("pokeID")) this.pokeID = nbt.getUUID("pokeID");
        if (nbt.contains("cmd")) this.command = Command.valueOf(nbt.getString("cmd"));
        this.args = nbt.getString("args");
    }

    @Override
    public void saveAdditional(final CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        if (this.getPokeID() != null) nbt.putUUID("pokeID", this.getPokeID());
        nbt.putString("args", this.args);
        if (this.command != null) nbt.putString("cmd", this.command.name());
    }

    public UUID getPokeID()
    {
        return this.pokeID;
    }

    public void setPokeID(final UUID pokeID)
    {
        this.pokeID = pokeID;
    }

    public Command getCommand()
    {
        return this.command;
    }

    protected void initCommand() throws Exception
    {
        this.setCommand(this.command, this.getArgs());
    }

    private Object[] getArgs() throws Exception
    {
        final Map<Command, Class<? extends IMobCommandHandler>> handlers = IHasCommands.COMMANDHANDLERS;
        final Class<? extends IMobCommandHandler> clazz = handlers.get(this.command);
        for (final Constructor<?> c : clazz.getConstructors()) if (c.getParameterCount() != 0) return this.getArgs(c);
        // for constructorless ones
        return this.getArgs(clazz.getConstructor());
    }

    private Object[] getArgs(final Constructor<?> constructor)
    {
        final String[] args = this.args.split(" ");
        final Class<?>[] argTypes = constructor.getParameterTypes();
        int index = 0;
        final Object[] ret = new Object[argTypes.length];
        for (int i = 0; i < ret.length; i++)
        {
            final Class<?> type = argTypes[i];
            if (type == Vector3.class)
            {
                final Vector3 arg = Vector3.getNewVector();
                arg.set(Double.parseDouble(args[index]), Double.parseDouble(args[index + 1]),
                        Double.parseDouble(args[index + 2]));
                index += 3;
                ret[i] = arg;
            }
            else if (type == float.class || type == Float.class)
            {
                final float arg = (float) Double.parseDouble(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == byte.class || type == Byte.class)
            {
                final byte arg = (byte) Integer.parseInt(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == int.class || type == Integer.class)
            {
                final int arg = Integer.parseInt(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == boolean.class || type == Boolean.class)
            {
                final boolean arg = Boolean.parseBoolean(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == String.class)
            {
                final String arg = args[index];
                index += 1;
                ret[i] = arg;
            }
        }
        return ret;
    }

    public void setCommand(final Command command, final Object... args) throws Exception
    {
        this.command = command;
        final Class<? extends IMobCommandHandler> clazz = IHasCommands.COMMANDHANDLERS.get(command);
        if (this.handler != null && clazz == this.handler.getClass() && this.prev_args.equals(this.args)) return;
        if (args == null)
        {
            this.handler = clazz.getConstructor().newInstance();
            return;
        }
        final Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) argTypes[i] = args[i].getClass();
        final Constructor<? extends IMobCommandHandler> constructor = clazz.getConstructor(argTypes);
        this.handler = constructor.newInstance(args);
        this.prev_args = this.args;
    }

    public void sendCommand() throws Exception
    {
        final Level w = this.getLevel();
        if (!(w instanceof ServerLevel)) return;
        if (this.command != null && this.handler == null) this.initCommand();
        if (this.handler == null) throw new Exception("No CommandHandler has been set");
        if (this.pokeID == null) throw new Exception("No Pokemob Set, please set a UUID first.");
        final ServerLevel world = (ServerLevel) w;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntity(this.pokeID));
        if (pokemob == null) throw new Exception("Pokemob for given ID is not found.");
        try
        {
            this.handler.handleCommand(pokemob);
        }
        catch (final Exception e)
        {
            PokecubeMod.LOGGER.error("Error executing a command for a pokemob", e);
            throw new Exception("Error handling the command", e);
        }
    }

    @Override
    public InteractionResult onInteract(final BlockPos pos, final Player player, final InteractionHand hand,
            final BlockHitResult hit)
    {
        final UUID id = PokecubeManager.getUUID(player.getItemInHand(hand));
        if (id != null)
        {
            this.setPokeID(id);
            if (!player.getLevel().isClientSide) CommandTools.sendMessage(player, "UUID Set to: " + id);
            return InteractionResult.SUCCESS;
        }
        else if (!player.isCrouching() && player instanceof ServerPlayer)
            PacketCommander.sendOpenPacket(pos, (ServerPlayer) player);
        return !player.isCrouching() ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
