package thut.wearables.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thut.core.common.ThutCore;

public abstract class Packet
{
    public Packet()
    {
    }

    public Packet(PacketBuffer buffer)
    {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            final ServerPlayerEntity player = ctx.get().getSender();
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
    public void handleServer(ServerPlayerEntity player)
    {

    }

    /**
     * Write to the buffer.
     * 
     * @param buffer
     */
    public abstract void write(PacketBuffer buffer);
}
