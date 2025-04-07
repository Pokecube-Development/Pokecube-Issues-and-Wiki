package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import thut.api.world.mobs.data.Data;

public class Data_Float extends Data_Base<Float>
{
    public Data_Float() {this.value = 0f;}

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
    public Data<Float> set(Float value)
    {
        if (this.value.equals(value)) return this;
        if (value == null)
        {
            this.value = 0f;
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
        buf.writeFloat(this.value);
    }

}
