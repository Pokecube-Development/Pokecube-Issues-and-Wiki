package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import thut.api.world.mobs.data.Data;

public class Data_ItemStack extends Data_Base<ItemStack>
{
    public Data_ItemStack(String name)
    {
        super(name);
        this.value = ItemStack.EMPTY;
    }

    @Override
    public ItemStack get()
    {
        return this.value;
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        final int num = buf.readInt();
        final FriendlyByteBuf wrapped = new FriendlyByteBuf(Unpooled.buffer(0));
        final byte[] dst = new byte[num];
        buf.readBytes(dst);
        try
        {
            wrapped.writeBytes(dst);
            byte value = wrapped.readByte();
            if (value == 1) this.value = ItemStack.parseOptional(provider, wrapped.readNbt());
            else this.value = ItemStack.EMPTY;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Data<ItemStack> set(ItemStack value)
    {
        if (value.isEmpty())
        {
            if (this.value.isEmpty()) return this;
            this.value = ItemStack.EMPTY;
            this.setDirty(true);
            return this;
        }
        if (ItemStack.matches(value, this.value)) return this;
        this.value = value;
        this.setDirty(true);
        return this;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        final FriendlyByteBuf wrapped = new FriendlyByteBuf(Unpooled.buffer(0));
        ItemStack tmp = this.value.copy();
        // Seems a rare race condition can make the stack empty
        // between this check and the writeNbt?
        if (tmp.isEmpty())
        {
            wrapped.writeByte(0);
        }
        else
        {
            wrapped.writeByte(1);
            wrapped.writeNbt(tmp.save(provider));
        }
        final int num = wrapped.readableBytes();
        buf.writeInt(num);
        buf.writeBytes(wrapped);

    }

}
