package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class Data_ItemStack extends Data_Base<ItemStack>
{
    ItemStack value = ItemStack.EMPTY;

    public Data_ItemStack()
    {
        this.initLast(this.value);
    }

    @Override
    public ItemStack get()
    {
        return this.value;
    }

    @Override
    protected boolean isDifferent(ItemStack last, ItemStack value)
    {
        return !ItemStack.areItemStacksEqual(last, value);
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        final int num = buf.readInt();
        final PacketBuffer wrapped = new PacketBuffer(Unpooled.buffer(0));
        final byte[] dst = new byte[num];
        buf.readBytes(dst);
        try
        {
            wrapped.writeBytes(dst);
            this.value = wrapped.readItemStack();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void set(ItemStack value)
    {
        if (value.isEmpty())
        {
            if (this.value.isEmpty()) return;
            this.value = ItemStack.EMPTY;
            return;
        }
        if (value.equals(this.value)) return;
        this.value = value;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        final PacketBuffer wrapped = new PacketBuffer(Unpooled.buffer(0));
        wrapped.writeItemStack(this.value);
        final int num = wrapped.readableBytes();
        buf.writeInt(num);
        buf.writeBytes(wrapped);

    }

}
