package thut.core.common.network.bigpacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thut.core.common.network.Packet;

public abstract class BigPacket extends Packet
{
    protected CompoundTag tag = new CompoundTag();
    protected byte[] data = null;
    private byte[] complete = null;
    final PacketAssembly<?> assembler;

    public BigPacket()
    {
        this.assembler = PacketAssembly.ASSEMBLERS.get(this.getClass());
    }

    public BigPacket(final CompoundTag tag)
    {
        this();
        this.tag = tag;
    }

    public void read(final FriendlyByteBuf buffer)
    {
        this.tag = buffer.readNbt();
        complete = this.assembler.onRead(this.getTag());
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }

    @Override
    public final void write(final FriendlyByteBuf buffer)
    {
        buffer.writeNbt(this.getTag());
    }

    public final void setTag(final CompoundTag tag)
    {
        this.tag = tag;
    }

    @Override
    public final void handleServer(final ServerPlayer player)
    {
        if (complete != null)
        {
            this.setData(complete);
            this.onCompleteServer(player);
        }
    }

    @Override
    public final void handleClient(Player player)
    {
        if (complete != null)
        {
            this.setData(complete);
            this.onCompleteClient(player);
        }
    }

    protected void onCompleteClient(Player player)
    {

    }

    protected void onCompleteServer(final ServerPlayer player)
    {

    }

    public final CompoundTag getTag()
    {
        return this.tag;
    }

}
