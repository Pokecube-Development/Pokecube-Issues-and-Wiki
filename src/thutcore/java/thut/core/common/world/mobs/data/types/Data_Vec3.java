package thut.core.common.world.mobs.data.types;

import java.util.Optional;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.phys.Vec3;

public class Data_Vec3 extends Data_Base<Optional<Vec3>>
{
    Optional<Vec3> value = Optional.empty();

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
    public void set(Optional<Vec3> value)
    {
        if (this.value.equals(value)) return;
        if (value == null)
        {
            this.value = Optional.empty();
            return;
        }
        this.value = value;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        if (!this.value.isEmpty())
        {
            var value = this.value.get();
            buf.writeDouble(value.x);
            buf.writeDouble(value.y);
            buf.writeDouble(value.z);
        }
    }

}
