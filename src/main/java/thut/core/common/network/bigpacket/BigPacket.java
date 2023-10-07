package thut.core.common.network.bigpacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import thut.core.common.network.Packet;

public abstract class BigPacket extends Packet
{
    protected CompoundTag tag = new CompoundTag();
    protected byte[] data = null;
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

    public BigPacket(final FriendlyByteBuf buffer)
    {
        this();
        this.tag = buffer.readNbt();
        if (this.assembler != null) this.data = this.assembler.onRead(this.getTag());
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
        final byte[] complete = this.assembler.onRead(this.getTag());
        if (complete != null)
        {
            this.setData(complete);
            this.onCompleteServer(player);
        }
    }

    @Override
    public final void handleClient()
    {
        final byte[] complete = this.data == null ? this.assembler.onRead(this.getTag()) : this.data;
        if (complete != null)
        {
            this.setData(complete);
            this.onCompleteClient();
        }
    }

    protected void onCompleteClient()
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
