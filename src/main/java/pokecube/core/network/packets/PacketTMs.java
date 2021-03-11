package pokecube.core.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import pokecube.core.inventory.tms.TMContainer;
import thut.core.common.network.Packet;

public class PacketTMs extends Packet
{
    public CompoundNBT data = new CompoundNBT();

    public PacketTMs()
    {
    }

    public PacketTMs(final PacketBuffer buf)
    {
        this.data = buf.readNbt();
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final Container cont = player.containerMenu;
        if (!(cont instanceof TMContainer)) return;
        final TMContainer container = (TMContainer) cont;
        final String[] moves = container.moves;
        final int index = this.data.getInt("m");
        if (index < moves.length) container.getInv().setItem(0, container.tile.addMoveToTM(
                moves[index], container.getInv().getItem(0)));
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeNbt(this.data);
    }
}
