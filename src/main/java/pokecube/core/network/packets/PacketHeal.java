package pokecube.core.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import pokecube.core.interfaces.IHealer;
import thut.core.common.network.Packet;

public class PacketHeal extends Packet
{
    public PacketHeal()
    {
    }

    public PacketHeal(final PacketBuffer buffer)
    {
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final Container cont = player.openContainer;
        if (cont instanceof IHealer) ((IHealer) cont).heal(player.getEntityWorld());
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
    }

}
