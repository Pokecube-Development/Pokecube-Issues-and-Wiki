package thut.core.common.network.nbtpacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thut.core.common.network.Packet;

public abstract class NBTPacket extends Packet
{
    protected CompoundTag tag = new CompoundTag();
    private CompoundTag complete = null;
    final PacketAssembly<?> assembler;

    public NBTPacket()
    {
        this.assembler = PacketAssembly.ASSEMBLERS.get(this.getClass());
        if (assembler == null)
        {
            throw new IllegalStateException("Unregistered packet class: " + this.getClass());
        }
    }

    public void read(final FriendlyByteBuf buffer)
    {
        this.tag = buffer.readNbt();
        complete = this.assembler.onRead(this.getTag());
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
            this.setTag(complete);
            this.onCompleteServer(player);
        }
    }

    @Override
    public final void handleClient(Player player)
    {
        if (complete != null)
        {
            this.setTag(complete);
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
