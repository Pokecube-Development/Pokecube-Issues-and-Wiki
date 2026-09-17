package pokecube.api.effects.context;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.effects.ParticleEffects;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;

public class PokemobContext implements EffectContext<IPokemob>
{
    public static final Type TYPE = new Type();
    public static final StreamCodec<ByteBuf, PokemobContext> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT,
            PokemobContext::getId, (integer) -> new PokemobContext(Either.right(integer)));

    private Either<Entity, Integer> entityOrId;
    public IPokemob pokemob;

    private PokemobContext(Either<Entity, Integer> entityOrId)
    {
        this.entityOrId = entityOrId;
    }

    public PokemobContext(IPokemob pokemob)
    {
        this.pokemob = pokemob;
    }

    @Override
    public IPokemob getContext(Level level)
    {
        if (pokemob == null && this.entityOrId != null)
        {
            this.resolveEntity(level);
            this.entityOrId.left().ifPresentOrElse(entity -> {
                this.pokemob = PokemobCaps.getPokemobFor(entity);
                this.entityOrId = null;
            }, () -> this.entityOrId = null);
        }
        return pokemob;
    }

    private void resolveEntity(Level level)
    {
        this.entityOrId.right().ifPresent(id -> {
            this.entityOrId = Either.left(PokecubeAPI.getEntityProvider().getEntity(level, id, true));
        });
    }

    private int getId()
    {
        return pokemob != null ? pokemob.getEntity().getId() : -1;
    }

    @Override
    public EffectContextType<? extends EffectContext<?>> getType()
    {
        return TYPE;
    }

    public static class Type implements EffectContextType<PokemobContext>
    {
        @Override
        public StreamCodec<ByteBuf, PokemobContext> streamCodec()
        {
            return PokemobContext.STREAM_CODEC;
        }

        @Override
        public ResourceLocation key()
        {
            return ParticleEffects.POKEMOB_CONTEXT;
        }
    }
}
