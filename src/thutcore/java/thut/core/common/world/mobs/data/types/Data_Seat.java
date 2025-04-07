package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import thut.api.entity.IMultiplePassengerEntity.Seat;
import thut.api.maths.vecmath.Vec3f;
import thut.api.world.mobs.data.Data;

public class Data_Seat extends Data_Base<Seat>
{
    public Data_Seat(String name)
    {
        super(name);
        this.value = new Seat(new Vec3f(), null);
    }

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
    public Data<Seat> set(Seat value)
    {
        if (value != null && value.equals(this.value)) return this;
        this.value = value;
        this.setDirty(true);
        return this;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        if (this.value != null) this.value.writeToBuf(buf);
    }

}
