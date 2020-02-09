package pokecube.adventures.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    public static void sendOpenPacket(final BlockPos pos, final ServerPlayerEntity player)
    {
        PokecubeAdv.packets.sendTo(new PacketCommander(pos), player);
    }

    public CompoundNBT data = new CompoundNBT();

    public PacketCommander()
    {
    }

    public PacketCommander(final BlockPos pos)
    {
        this.data.putInt("x", pos.getX());
        this.data.putInt("y", pos.getY());
        this.data.putInt("z", pos.getZ());
    }

    public PacketCommander(final PacketBuffer buf)
    {
        this.data = buf.readCompoundTag();
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeCompoundTag(this.data);
    }

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void handleClient()
    {
        // Open the gui.
        final World world = PokecubeCore.proxy.getWorld();
        final BlockPos pos = new BlockPos(this.data.getInt("x"), this.data.getInt("y"), this.data.getInt("z"));
        final TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof CommanderTile)) return;
        net.minecraft.client.Minecraft.getInstance().displayGuiScreen(new Commander(pos));
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final World world = player.getEntityWorld();
        final BlockPos pos = new BlockPos(this.data.getInt("x"), this.data.getInt("y"), this.data.getInt("z"));
        final TileEntity te = world.getTileEntity(pos);
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
                    .getPos(), e);
            tile.getWorld().playSound(null, tile.getPos(), SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.BLOCKS,
                    1, 1);
        }
        TileUpdate.sendUpdate(tile);
    }
}
