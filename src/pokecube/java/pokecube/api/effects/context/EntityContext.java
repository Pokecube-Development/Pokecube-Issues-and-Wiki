package pokecube.api.effects.context;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;

public class EntityContext implements EffectContext<Entity>
{
    public static final StreamCodec<ByteBuf, EntityContext> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT,
            EntityContext::getId, (integer) -> new EntityContext(Either.right(integer)));

    private Either<Entity, Integer> entityOrId;
    public Entity entity;

    private EntityContext(Either<Entity, Integer> entityOrId)
    {
        this.entityOrId = entityOrId;
    }

    public EntityContext(Entity entity)
    {
        this.entity = entity;
    }

    @Override
    public Entity getContext(Level level)
    {
        if (entity == null && this.entityOrId != null)
        {
            this.resolveEntity(level);
            this.entityOrId.left().ifPresentOrElse(entity -> {
                this.entity = entity;
                this.entityOrId = null;
            }, () -> this.entityOrId = null);
        }
        return entity;
    }

    @Override
    public Entity getContext()
    {
        return entity;
    }

    @Override
    public void write(ByteBuf buffer)
    {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public ResourceLocation getKey()
    {
        return ENTITY;
    }

    private void resolveEntity(Level level)
    {
        this.entityOrId.right().ifPresent(id -> {
            this.entityOrId = Either.left(PokecubeAPI.getEntityProvider().getEntity(level, id, true));
        });
    }

    private int getId()
    {
        return entity != null ? entity.getId() : -1;
    }
}
