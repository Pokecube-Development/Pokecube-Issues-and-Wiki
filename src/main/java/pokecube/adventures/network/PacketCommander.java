package pokecube.adventures.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.commander.CommanderTile;
import pokecube.adventures.client.gui.blocks.Commander;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import thut.core.common.network.Packet;
import thut.core.common.network.TileUpdate;

public class PacketCommander extends Packet
{
    public static void sendOpenPacket(final BlockPos pos, final ServerPlayer player)
    {
        PokecubeAdv.packets.sendTo(new PacketCommander(pos), player);
    }

    public CompoundTag data = new CompoundTag();

    public PacketCommander()
    {
    }

    public PacketCommander(final BlockPos pos)
    {
        this.data.putInt("x", pos.getX());
        this.data.putInt("y", pos.getY());
        this.data.putInt("z", pos.getZ());
    }

    public PacketCommander(final FriendlyByteBuf buf)
    {
        this.data = buf.readNbt();
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeNbt(this.data);
    }

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void handleClient()
    {
        // Open the gui.
        final Level world = PokecubeCore.proxy.getWorld();
        final BlockPos pos = new BlockPos(this.data.getInt("x"), this.data.getInt("y"), this.data.getInt("z"));
        final BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof CommanderTile)) return;
        net.minecraft.client.Minecraft.getInstance().setScreen(new Commander(pos));
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final Level world = player.getLevel();
        final BlockPos pos = new BlockPos(this.data.getInt("x"), this.data.getInt("y"), this.data.getInt("z"));
        final BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof CommanderTile)) return;
        final CommanderTile tile = (CommanderTile) te;
        final String command = this.data.getString("C");
        final String args = this.data.getString("A");
        Command command_ = null;
        try
        {
            if (!command.isEmpty()) command_ = Command.valueOf(command);
            tile.setCommand(command_, args);
        }
        catch (final Exception e)
        {
            if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.warn("Invalid Commander Block use at " + tile
                    .getBlockPos(), e);
            tile.getLevel().playSound(null, tile.getBlockPos(), SoundEvents.NOTE_BLOCK_BASEDRUM, SoundSource.BLOCKS,
                    1, 1);
        }
        TileUpdate.sendUpdate(tile);
    }
}
