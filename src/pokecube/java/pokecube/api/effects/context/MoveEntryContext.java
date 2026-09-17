package pokecube.api.effects.context;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import pokecube.api.effects.ParticleEffects;
import pokecube.api.moves.MoveEntry;

public class MoveEntryContext implements EffectContext<MoveEntry>
{
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
    public ResourceLocation getKey()
    {
        return ParticleEffects.MOVE_CONTEXT;
    }

    private String getName()
    {
        return this.entry.getName();
    }
}
