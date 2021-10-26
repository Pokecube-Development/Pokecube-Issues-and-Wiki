package pokecube.core.network.packets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import pokecube.core.inventory.tms.TMContainer;
import thut.core.common.network.Packet;

public class PacketTMs extends Packet
{
    public CompoundTag data = new CompoundTag();

    public PacketTMs()
    {
    }

    public PacketTMs(final FriendlyByteBuf buf)
    {
        this.data = buf.readNbt();
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final AbstractContainerMenu cont = player.containerMenu;
        if (!(cont instanceof TMContainer)) return;
        final TMContainer container = (TMContainer) cont;
        final String[] moves = container.moves;
        final int index = this.data.getInt("m");
        if (index < moves.length) container.getInv().setItem(0, container.tile.addMoveToTM(
                moves[index], container.getInv().getItem(0)));
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        buffer.writeNbt(this.data);
    }
}
