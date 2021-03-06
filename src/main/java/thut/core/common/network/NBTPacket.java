package thut.core.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public abstract class NBTPacket extends Packet
{
    protected CompoundNBT   tag = new CompoundNBT();
    final PacketAssembly<?> assembler;

    public NBTPacket()
    {
        this.assembler = PacketAssembly.ASSEMBLERS.get(this.getClass());
    }

    public NBTPacket(final CompoundNBT tag)
    {
        this();
        this.tag = tag;
    }

    public NBTPacket(final PacketBuffer buffer)
    {
        this();
        this.tag = buffer.readCompoundTag();
        this.assembler.onRead(this.getTag());
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeCompoundTag(this.getTag());
    }

    public void setTag(final CompoundNBT tag)
    {
        this.tag = tag;
    }

    @Override
    public final void handleServer(final ServerPlayerEntity player)
    {
        final CompoundNBT complete = this.assembler.onRead(this.getTag());
        if (complete != null)
        {
            this.tag = complete;
            if (complete != null) this.onCompleteServer(player);
        }
    }

    @Override
    public final void handleClient()
    {
        final CompoundNBT complete = this.assembler.onRead(this.getTag());
        if (complete != null)
        {
            this.tag = complete;
            this.onCompleteClient();
        }
    }

    protected void onCompleteClient()
    {

    }

    protected void onCompleteServer(final ServerPlayerEntity player)
    {

    }

    public CompoundNBT getTag()
    {
        return this.tag;
    }

}
