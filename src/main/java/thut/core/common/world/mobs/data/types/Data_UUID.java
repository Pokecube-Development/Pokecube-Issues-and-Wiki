package thut.core.common.world.mobs.data.types;

import java.util.UUID;

import io.netty.buffer.ByteBuf;

public class Data_UUID extends Data_Base<UUID>
{
    UUID value = null;

    @Override
    public UUID get()
    {
        return this.value;
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        final long lsb = buf.readLong();
        final long msb = buf.readLong();
        if (lsb != -1 && msb != -1) this.value = new UUID(msb, lsb);
        else this.value = null;
    }

    @Override
    public void set(UUID value)
    {
        if (value == null)
        {
            if (this.value == null) return;
            this.value = null;
            return;
        }
        if (value.equals(this.value)) return;
        this.value = value;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        long lsb = -1;
        long msb = -1;
        if (this.value != null)
        {
            lsb = this.value.getLeastSignificantBits();
            msb = this.value.getMostSignificantBits();
        }
        buf.writeLong(lsb);
        buf.writeLong(msb);
    }

}
