package pokecube.core.network.pokemobs;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
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

    public PacketTeleport(PacketBuffer buffer)
    {
        this.index = buffer.readInt();
    }

    @Override
    public void handleServer(ServerPlayerEntity player)
    {
        TeleportHandler.setTeleIndex(player.getStringUUID(), this.index);
    }

    @Override
    public void write(PacketBuffer buffer)
    {
        buffer.writeInt(this.index);
    }

}
