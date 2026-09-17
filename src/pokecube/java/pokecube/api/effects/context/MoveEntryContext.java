package pokecube.api.effects.context;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import pokecube.api.effects.ParticleEffects;
import pokecube.api.moves.MoveEntry;

public class MoveEntryContext implements EffectContext<MoveEntry>
{
    public static final Type TYPE = new Type();
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

    private String getName()
    {
        return this.entry.getName();
    }

    @Override
    public EffectContextType<? extends EffectContext<?>> getType()
    {
        return TYPE;
    }

    public static class Type implements EffectContextType<MoveEntryContext>
    {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, MoveEntryContext> streamCodec()
        {
            return MoveEntryContext.STREAM_CODEC;
        }

        @Override
        public ResourceLocation key()
        {
            return ParticleEffects.MOVE_CONTEXT;
        }
    }
}
