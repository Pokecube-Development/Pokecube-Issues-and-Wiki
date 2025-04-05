package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.entity.pokemob.commandhandlers.TeleportHandler;
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

    public void read(FriendlyByteBuf buffer)
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

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:set_tele_index"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

}
