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
    public static PacketDataSync makePacket(final PlayerData data, final UUID owner)
    {
        final PacketDataSync packet = new PacketDataSync();
        packet.data.putString("type", data.getIdentifier());
        final CompoundNBT tag1 = new CompoundNBT();
        data.writeToNBT(tag1);
        packet.data.put("data", tag1);
        packet.data.putUniqueId("uuid", owner);
        return packet;
    }

    public static void syncData(final PlayerData data, final UUID owner, final ServerPlayerEntity sendTo,
            final boolean toTracking)
    {
        PokecubeCore.packets.sendTo(PacketDataSync.makePacket(data, owner), sendTo);
        if (toTracking) PokecubeCore.packets.sendToTracking(PacketDataSync.makePacket(data, owner), sendTo);
    }

    public static void syncData(final PlayerEntity player, final String dataType)
    {
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        final PlayerData data = manager.getData(dataType);
        if (data == null)
        {
            PokecubeCore.LOGGER.error("No datatype for " + dataType);
            return;
        }
        PacketDataSync.syncData(data, player.getUniqueID(), (ServerPlayerEntity) player, true);
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
