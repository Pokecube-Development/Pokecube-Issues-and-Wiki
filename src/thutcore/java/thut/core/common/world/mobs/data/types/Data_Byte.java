package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import thut.api.world.mobs.data.Data;

public class Data_Byte extends Data_Base<Byte>
{
    public Data_Byte(String name, byte i)
    {
        super(name);
        value = i;
    }

    public Data_Byte(String name)
    {
        super(name);
        value = 0;
    }

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
    public Data<Byte> set(Byte value)
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
        buf.writeByte(this.value);
    }

}
