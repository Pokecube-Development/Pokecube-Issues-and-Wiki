package pokecube.api.effects.context;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.ParticleEffects;
import pokecube.api.moves.MoveEntry;

import java.util.function.Consumer;
import java.util.function.Function;

public class MoveEntryContext implements EffectContext<MoveEntry>
{
    public static Function<MoveEntry, Consumer<EffectPacketInfo>> MOVE_ANIMATION_CLIENT_FACTORY = moveEntry -> (m) -> {};
    public static Function<MoveEntry, Consumer<EffectPacketInfo>> MOVE_ANIMATION_SERVER_FACTORY = moveEntry -> (m) -> {};

    public static final StreamCodec<ByteBuf, MoveEntryContext> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MoveEntryContext::getName,
            (string) -> new MoveEntryContext(MoveEntry.get(string)));

    public MoveEntry entry;

    public MoveEntryContext(MoveEntry entry)
    {
        if (entry == null) entry = MoveEntry.CONFUSED;
        this.entry = entry;
    }

    @Override
    public MoveEntry getContext(Level level)
    {
        return this.entry;
    }

    @Override
    public MoveEntry getContext()
    {
        return entry;
    }

    @Override
    public void write(ByteBuf buffer)
    {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public void onAttach(EffectPacketInfo info)
    {
        info.onClientTick = MOVE_ANIMATION_CLIENT_FACTORY.apply(this.entry);
        info.onServerTick = MOVE_ANIMATION_SERVER_FACTORY.apply(this.entry);
    }

    @Override
    public ResourceLocation getKey()
    {
        return ParticleEffects.MOVE_CONTEXT;
    }

    private String getName()
    {
        return this.entry.getName();
    }
}
