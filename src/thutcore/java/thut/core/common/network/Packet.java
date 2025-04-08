package thut.core.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import thut.core.common.ThutCore;

public abstract class Packet
        implements CustomPacketPayload, IPayloadHandler<Packet>, StreamCodec<FriendlyByteBuf, Packet>
{
    public Packet()
    {}

    @Override
    public Packet decode(FriendlyByteBuf buffer)
    {
        try
        {
            Packet resp = this.getClass().getConstructor().newInstance();
            resp.read(buffer);
            return resp;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer, Packet value)
    {
        value.write(buffer);
    }

    public void handle(Packet payload, IPayloadContext context)
    {
        var player = context.player();
        if (ThutCore.proxy.isClientSide()) payload.handleClient(player);
        else payload.handleServer((ServerPlayer) player);
    }

    /*
     * Handles client side interaction.
     */
    public void handleClient(Player player)
    {

    }

    /*
     * Handles Server side interaction.
     */
    public void handleServer(ServerPlayer player)
    {

    }

    /**
     * Write to the buffer.
     * 
     * @param buffer
     */
    public abstract void write(FriendlyByteBuf buffer);

    public abstract void read(FriendlyByteBuf buffer);
}
