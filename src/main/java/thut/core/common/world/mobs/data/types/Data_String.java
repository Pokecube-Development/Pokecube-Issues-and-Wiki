package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;

public class Data_String extends Data_Base<String>
{
    String value = "";

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
    public void set(String value)
    {
        if (this.value.equals(value)) return;
        if (value == null)
        {
            this.value = "";
            return;
        }
        this.value = value;
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
