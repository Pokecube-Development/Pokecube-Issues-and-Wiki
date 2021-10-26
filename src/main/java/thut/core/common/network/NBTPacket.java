package thut.core.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public abstract class NBTPacket extends Packet
{
    protected CompoundTag   tag = new CompoundTag();
    final PacketAssembly<?> assembler;

    public NBTPacket()
    {
        this.assembler = PacketAssembly.ASSEMBLERS.get(this.getClass());
    }

    public NBTPacket(final CompoundTag tag)
    {
        this();
        this.tag = tag;
    }

    public NBTPacket(final FriendlyByteBuf buffer)
    {
        this();
        this.tag = buffer.readNbt();
        if (this.assembler != null) this.assembler.onRead(this.getTag());
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
        final CompoundTag complete = this.assembler.onRead(this.getTag());
        if (complete != null)
        {
            this.tag = complete;
            if (complete != null) this.onCompleteServer(player);
        }
    }

    @Override
    public final void handleClient()
    {
        final CompoundTag complete = this.assembler.onRead(this.getTag());
        if (complete != null)
        {
            this.tag = complete;
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
