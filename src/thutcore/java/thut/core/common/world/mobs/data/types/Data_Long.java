package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;

public class Data_Long extends Data_Base<Long>
{
    Long value = 0l;

    @Override
    public Long get()
    {
        return this.value;
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        this.value = buf.readLong();
    }

    @Override
    public void set(Long value)
    {
        if (this.value.equals(value)) return;
        if (value == null)
        {
            this.value = 0l;
            return;
        }
        this.value = value;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        buf.writeLong(this.value);
    }

}
