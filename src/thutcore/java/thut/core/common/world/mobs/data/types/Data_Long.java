package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import thut.api.world.mobs.data.Data;

public class Data_Long extends Data_Base<Long>
{
    public Data_Long() {this.value = 0l;}

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
    public Data<Long> set(Long value)
    {
        if (this.value.equals(value)) return this;
        if (value == null)
        {
            this.value = 0l;
            this.setDirty(true);
            return this;
        }
        this.value = value;
        this.setDirty(true);
        return this;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        buf.writeLong(this.value);
    }

}
