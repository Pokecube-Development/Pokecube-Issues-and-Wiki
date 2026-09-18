package pokecube.api.effects.context;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.EvolutionEffect;
import pokecube.api.effects.ParticleEffects;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;

public class PokemobContext implements EffectContext<IPokemob>
{
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

    @Override
    public IPokemob getContext()
    {
        return pokemob;
    }

    @Override
    public void onAttach(EffectPacketInfo info)
    {
        if (info.processedContext == null) info.processedContext = EvolutionEffect.EvoContext.fromMoveInfo(info);
    }

    @Override
    public void write(ByteBuf buffer)
    {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public ResourceLocation getKey()
    {
        return ParticleEffects.POKEMOB_CONTEXT;
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
}
