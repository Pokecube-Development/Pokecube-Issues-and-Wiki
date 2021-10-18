package pokecube.core.network.packets;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
        final CompoundTag tag1 = new CompoundTag();
        data.writeToNBT(tag1);
        packet.data.put("data", tag1);
        packet.data.putUUID("uuid", owner);
        return packet;
    }

    public static void syncData(final PlayerData data, final UUID owner, final ServerPlayer sendTo,
            final boolean toTracking)
    {
        PokecubeCore.packets.sendTo(PacketDataSync.makePacket(data, owner), sendTo);
        if (toTracking) PokecubeCore.packets.sendToTracking(PacketDataSync.makePacket(data, owner), sendTo);
    }

    public static void syncData(final Player player, final String dataType)
    {
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        final PlayerData data = manager.getData(dataType);
        if (data == null)
        {
            PokecubeCore.LOGGER.error("No datatype for " + dataType);
            return;
        }
        PacketDataSync.syncData(data, player.getUUID(), (ServerPlayer) player, true);
    }

    public CompoundTag data = new CompoundTag();

    public PacketDataSync()
    {
        super(null);
    }

    public PacketDataSync(final FriendlyByteBuf buffer)
    {
        super(buffer);
        this.data = buffer.readNbt();
    }

    @Override
    public void handleClient()
    {
        final UUID id = this.data.hasUUID("uuid") ? this.data.getUUID("uuid") : null;
        final Player player = id == null ? PokecubeCore.proxy.getPlayer() : PokecubeCore.proxy.getPlayer(id);
        if(player==null) return;
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        manager.getData(this.data.getString("type")).readFromNBT(this.data.getCompound("data"));
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeNbt(this.data);
    }
}
