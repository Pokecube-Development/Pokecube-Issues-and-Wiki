package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import thut.core.common.network.Packet;

public class PacketTeleport extends Packet
{
    int index;

    public PacketTeleport()
    {
    }

    public PacketTeleport(int index)
    {
        this.index = index;
    }

    public PacketTeleport(FriendlyByteBuf buffer)
    {
        this.index = buffer.readInt();
    }

    @Override
    public void handleServer(ServerPlayer player)
    {
        TeleportHandler.setTeleIndex(player.getStringUUID(), this.index);
    }

    @Override
    public void write(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.index);
    }

}
