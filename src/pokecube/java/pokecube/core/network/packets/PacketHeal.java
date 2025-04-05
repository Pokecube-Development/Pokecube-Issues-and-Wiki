package pokecube.core.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import pokecube.api.blocks.IHealer;
import thut.core.common.network.Packet;

public class PacketHeal extends Packet
{
    public PacketHeal()
    {}

    public void read(final FriendlyByteBuf buffer)
    {}

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final AbstractContainerMenu cont = player.containerMenu;
        if (cont instanceof IHealer healer) healer.heal(player.level());
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {}

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:use_healer"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
