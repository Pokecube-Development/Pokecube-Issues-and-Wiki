package pokecube.core.network.packets;

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
    public static void sendInitPacket(PlayerEntity player, String dataType)
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
        PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
    }

    public CompoundNBT data = new CompoundNBT();

    public PacketDataSync()
    {
        super(null);
    }

    public PacketDataSync(PacketBuffer buffer)
    {
        super(buffer);
        this.data = buffer.readCompoundTag();
    }

    @Override
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        manager.getData(this.data.getString("type")).readFromNBT(this.data.getCompound("data"));
    }

    @Override
    public void write(PacketBuffer buffer)
    {
        buffer.writeCompoundTag(this.data);
    }
}
