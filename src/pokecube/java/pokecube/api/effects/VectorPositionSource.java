package pokecube.api.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import pokecube.core.PokecubeCore;

import java.util.Optional;
import java.util.function.Supplier;

public class VectorPositionSource implements PositionSource
{
    public static final MapCodec<VectorPositionSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Vec3.CODEC.fieldOf("pos").forGetter(source -> source.pos))
                    .apply(instance, VectorPositionSource::new));
    public static final StreamCodec<ByteBuf, VectorPositionSource> STREAM_CODEC = new StreamCodec<>()
    {
        public VectorPositionSource decode(ByteBuf buffer)
        {
            return new VectorPositionSource(FriendlyByteBuf.readVector3f(buffer));
        }

        public void encode(ByteBuf buffer, VectorPositionSource source)
        {
            FriendlyByteBuf.writeVector3f(buffer, source.pos.toVector3f());
        }
    };

    public static final Supplier<PositionSourceType<VectorPositionSource>> TYPE;

    static
    {
        TYPE = PokecubeCore.POSITION_SOURCES.register("vec3", Type::new);
    }
    public static void init(){}

    private final Vec3 pos;

    public VectorPositionSource(Vector3f pos)
    {
        this.pos = new Vec3(pos);
    }

    public VectorPositionSource(Vec3 pos)
    {
        this.pos = pos;
    }

    @Override
    public Optional<Vec3> getPosition(Level level)
    {
        return Optional.of(this.pos);
    }

    @Override
    public PositionSourceType<VectorPositionSource> getType()
    {
        return VectorPositionSource.TYPE.get();
    }

    public static class Type implements PositionSourceType<VectorPositionSource>
    {
        @Override
        public MapCodec<VectorPositionSource> codec()
        {
            return VectorPositionSource.CODEC;
        }

        @Override
        public StreamCodec<ByteBuf, VectorPositionSource> streamCodec()
        {
            return VectorPositionSource.STREAM_CODEC;
        }
    }
}
