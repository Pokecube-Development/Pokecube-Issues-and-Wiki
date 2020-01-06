package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;

public class Data_Byte extends Data_Base<Byte>
{
    Byte value = 0;

    @Override
    public Byte get()
    {
        return this.value;
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        this.value = buf.readByte();
    }

    @Override
    public void set(Byte value)
    {
        if (this.value.equals(value)) return;
        if (value == null)
        {
            this.value = 0;
            return;
        }
        this.value = value;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        buf.writeByte(this.value);
    }

}
