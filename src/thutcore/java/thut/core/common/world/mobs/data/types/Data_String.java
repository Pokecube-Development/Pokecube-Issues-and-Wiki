package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import thut.api.world.mobs.data.Data;

public class Data_String extends Data_Base<String>
{
    public Data_String(String name)
    {
        super(name);
        this.value = "";
    }

    @Override
    public String get()
    {
        return this.value;
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        final int len = buf.readInt();
        final byte[] arr = new byte[len];
        buf.readBytes(arr);
        this.value = new String(arr);
    }

    @Override
    public Data<String> set(String value)
    {
        if (this.value.equals(value)) return this;
        if (value == null)
        {
            this.value = "";
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
        final byte[] arr = this.value.getBytes();
        buf.writeInt(arr.length);
        buf.writeBytes(arr);
    }

}
