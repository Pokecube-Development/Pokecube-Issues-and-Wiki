package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import thut.api.entity.IMultiplePassengerEntity.Seat;

public class Data_Seat extends Data_Base<Seat>
{
    Seat value = null;

    @Override
    public Seat get()
    {
        return this.value;
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        if (buf.isReadable()) this.value = new Seat(buf);
        else this.value = null;
    }

    @Override
    public void set(Seat value)
    {
        if (value != null && value.equals(this.value)) return;
        this.value = value;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        if (this.value != null) this.value.writeToBuf(buf);
    }

}
