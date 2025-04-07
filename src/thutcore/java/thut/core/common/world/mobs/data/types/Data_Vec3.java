package thut.core.common.world.mobs.data.types;

import java.util.Optional;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.phys.Vec3;
import thut.api.world.mobs.data.Data;

public class Data_Vec3 extends Data_Base<Optional<Vec3>>
{
    public Data_Vec3(String name)
    {
        super(name);
        value = Optional.empty();
    }

    @Override
    public Optional<Vec3> get()
    {
        return this.value;
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        if (!buf.isReadable()) this.value = Optional.empty();
        else this.value = Optional.of(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    @Override
    public Data<Optional<Vec3>> set(Optional<Vec3> value)
    {
        if (this.value.equals(value)) return this;
        if (value.isEmpty())
        {
            if (this.value.isEmpty()) return this;
            this.value = Optional.empty();
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
        if (this.value.isPresent())
        {
            var value = this.value.get();
            buf.writeDouble(value.x);
            buf.writeDouble(value.y);
            buf.writeDouble(value.z);
        }
    }

}
