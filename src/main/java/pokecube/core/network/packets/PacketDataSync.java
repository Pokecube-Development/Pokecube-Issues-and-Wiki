package pokecube.core.network.packets;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import pokecube.core.PokecubeCore;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.core.common.handlers.PlayerDataHandler.PlayerDataManager;
import thut.core.common.network.Packet;

public class PacketDataSync extends Packet
{
    public static void sendInitPacket(final PlayerEntity player, final String dataType)
    {
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        final PlayerData data = manager.getData(dataType);
        if (data == null)
        {
            PokecubeCore.LOGGER.error("No datatype for " + dataType);
            return;
        }
        final PacketDataSync packet = new PacketDataSync();
        packet.data.putString("type", dataType);
        final CompoundNBT tag1 = new CompoundNBT();
        data.writeToNBT(tag1);
        packet.data.put("data", tag1);
        PokecubeCore.packets.sendTo(packet, (ServerPlayerEntity) player);
    }

    public static void sendUpdatePacket(final ServerPlayerEntity playerSend, final ServerPlayerEntity playerTo,
            final String dataType)
    {
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(playerSend);
        final PlayerData data = manager.getData(dataType);
        if (data == null)
        {
            PokecubeCore.LOGGER.error("No datatype for " + dataType);
            return;
        }
        final PacketDataSync packet = new PacketDataSync();
        packet.data.putUniqueId("uuid", playerSend.getUniqueID());
        packet.data.putString("type", dataType);
        final CompoundNBT tag1 = new CompoundNBT();
        data.writeToNBT(tag1);
        packet.data.put("data", tag1);
        PokecubeCore.packets.sendTo(packet, playerTo);
    }

    public static void sendUpdatePacket(final ServerPlayerEntity player, final String dataType, final boolean toOthers)
    {
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        final PlayerData data = manager.getData(dataType);
        if (data == null)
        {
            PokecubeCore.LOGGER.error("No datatype for " + dataType);
            return;
        }
        final PacketDataSync packet = new PacketDataSync();
        packet.data.putUniqueId("uuid", player.getUniqueID());
        packet.data.putString("type", dataType);
        final CompoundNBT tag1 = new CompoundNBT();
        data.writeToNBT(tag1);
        packet.data.put("data", tag1);
        PokecubeCore.packets.sendTo(packet, player);
        if (toOthers) PokecubeCore.packets.sendToTracking(packet, player);
    }

    public CompoundNBT data = new CompoundNBT();

    public PacketDataSync()
    {
        super(null);
    }

    public PacketDataSync(final PacketBuffer buffer)
    {
        super(buffer);
        this.data = buffer.readCompoundTag();
    }

    @Override
    public void handleClient()
    {
        final UUID id = this.data.hasUniqueId("uuid") ? this.data.getUniqueId("uuid") : null;
        final PlayerEntity player = id == null ? PokecubeCore.proxy.getPlayer() : PokecubeCore.proxy.getPlayer(id);
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        manager.getData(this.data.getString("type")).readFromNBT(this.data.getCompound("data"));
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeCompoundTag(this.data);
    }
}
