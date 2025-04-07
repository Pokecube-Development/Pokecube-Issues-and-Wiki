package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import thut.api.world.mobs.data.Data;

public class Data_Int extends Data_Base<Integer>
{
    public Data_Int(String name, int i)
    {
        super(name);
        this.value = i;
    }

    public Data_Int(String name)
    {
        super(name);
        this.value = 0;
    }

    @Override
    public Integer get()
    {
        return this.value;
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        this.value = buf.readInt();
    }

    @Override
    public Data<Integer> set(Integer value)
    {
        if (this.value.equals(value)) return this;
        if (value == null)
        {
            this.value = 0;
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
        buf.writeInt(this.value);
    }

}
