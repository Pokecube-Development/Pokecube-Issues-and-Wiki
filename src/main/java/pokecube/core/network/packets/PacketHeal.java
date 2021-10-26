package pokecube.core.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import pokecube.core.interfaces.IHealer;
import thut.core.common.network.Packet;

public class PacketHeal extends Packet
{
    public PacketHeal()
    {
    }

    public PacketHeal(final FriendlyByteBuf buffer)
    {
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final AbstractContainerMenu cont = player.containerMenu;
        if (cont instanceof IHealer) ((IHealer) cont).heal(player.getCommandSenderWorld());
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
    }

}
