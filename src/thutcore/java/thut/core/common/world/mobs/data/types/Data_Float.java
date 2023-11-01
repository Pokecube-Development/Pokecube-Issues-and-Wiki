package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;

public class Data_Float extends Data_Base<Float>
{
    Float value = 0f;

    @Override
    public Float get()
    {
        return this.value;
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        this.value = buf.readFloat();
    }

    @Override
    public void set(Float value)
    {
        if (this.value.equals(value)) return;
        if (value == null)
        {
            this.value = 0f;
            return;
        }
        this.value = value;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        buf.writeFloat(this.value);
    }

}
