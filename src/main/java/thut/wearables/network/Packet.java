package thut.wearables.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thut.core.common.ThutCore;

public abstract class Packet
{
    public Packet()
    {
    }

    public Packet(FriendlyByteBuf buffer)
    {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            final ServerPlayer player = ctx.get().getSender();
            if (ThutCore.proxy.isClientSide()) this.handleClient();
            else this.handleServer(player);
        });
        ctx.get().setPacketHandled(true);
    }

    /*
     * Handles client side interaction.
     */
    public void handleClient()
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
}
