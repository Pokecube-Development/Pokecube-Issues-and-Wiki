package pokecube.api.effects;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import pokecube.api.effects.context.EffectContext;
import pokecube.api.effects.context.EntityContext;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.core.PokecubeCore;
import thut.api.entity.multipart.IMultpart;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EffectPacketInfo
{
    public static PositionSource getEntityCentre(Entity entity)
    {
        return new EntityPositionSource(entity, entity.getBbHeight() / 2);
    }
    public static PositionSource getEntityCentrePlusLook(Entity entity)
    {
        return new EntityPositionSource(entity, entity.getBbHeight() / 2);
    }

    public static class TaggedEntityTracker implements PositionSource
    {
        public static final MapCodec<TaggedEntityTracker> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        UUIDUtil.CODEC.fieldOf("source_entity").forGetter(TaggedEntityTracker::getUuid),
                        Codec.STRING.fieldOf("key").orElse("head").forGetter(tracker -> tracker.key)).apply(instance,
                        (uuid, string) -> new TaggedEntityTracker(Either.right(Either.left(uuid)), string)));
        public static final StreamCodec<ByteBuf, TaggedEntityTracker> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, TaggedEntityTracker::getId, ByteBufCodecs.STRING_UTF8, tracker -> tracker.key,
                (integer, string) -> new TaggedEntityTracker(Either.right(Either.right(integer)), string));

        public static PositionSource create(Entity attacker)
        {
            return create(attacker, MoveEntry.DEFAULT_MOVE_SOURCES);
        }

        public static PositionSource create(Entity attacker, List<String> validTags)
        {
            if (attacker instanceof IMultpart<?, ?>) for (var key : validTags)
            {
                var tracker = new TaggedEntityTracker(attacker, key);
                if (tracker.location != null) return tracker;
            }
            return getEntityCentre(attacker);
        }

        public static final Supplier<PositionSourceType<TaggedEntityTracker>> TYPE;

        static
        {
            TYPE = PokecubeCore.POSITION_SOURCES.register("entity_locator", TaggedEntityTracker.Type::new);
        }
        public static void init(){}

        private Either<Entity, Either<UUID, Integer>> entityOrUuidOrId;
        String key;
        Vector3f location;

        public TaggedEntityTracker(Entity entity, String key)
        {
            this(Either.left(entity), key);
            if (entity instanceof IMultpart<?, ?> multi && multi.getAttachmentPointMap().containsKey(key))
            {
                var points = multi.getAttachmentPointMap().get(key);
                var index = entity.getRandom().nextInt(points.size());
                location = points.get(index).mod();
            }
            else location = null;
        }

        private TaggedEntityTracker(Either<Entity, Either<UUID, Integer>> entityOrUuidOrId, String key) {
            this.entityOrUuidOrId = entityOrUuidOrId;
            this.key = key;
        }

        @Override
        public Optional<Vec3> getPosition(Level level)
        {
            if (location == null && this.entityOrUuidOrId != null)
            {
                this.resolveEntity(level);
                this.entityOrUuidOrId.left().ifPresentOrElse(entity -> {
                    if (entity instanceof IMultpart<?, ?> multi && multi.getAttachmentPointMap().containsKey(key))
                    {
                        var points = multi.getAttachmentPointMap().get(key);
                        var index = entity.getRandom().nextInt(points.size());
                        location = points.get(index).mod();
                    }
                    else location = null;
                    this.entityOrUuidOrId = null;
                }, () -> this.entityOrUuidOrId = null);
            }
            if (location == null) return Optional.empty();
            return Optional.of(new Vec3(location.x, location.y, location.z));
        }

        private void resolveEntity(Level level)
        {
            this.entityOrUuidOrId.map(Optional::of, either -> Optional.ofNullable(
                    either.map(uuid -> level instanceof ServerLevel serverlevel ? serverlevel.getEntity(uuid) : null,
                            level::getEntity))).ifPresent(entity -> this.entityOrUuidOrId = Either.left(entity));
        }

        private UUID getUuid()
        {
            return this.entityOrUuidOrId.map(Entity::getUUID,
                    either -> either.map(Function.identity(), id -> {
                        throw new RuntimeException("Unable to get entityId from uuid");
                    }));
        }

        private int getId()
        {
            return this.entityOrUuidOrId.map(Entity::getId, either -> either.map(uuid -> {
                throw new IllegalStateException("Unable to get entityId from uuid");
            }, Function.identity()));
        }

        @Override
        public PositionSourceType<TaggedEntityTracker> getType()
        {
            return TaggedEntityTracker.TYPE.get();
        }

        public static class Type implements PositionSourceType<TaggedEntityTracker>
        {
            @Override
            public MapCodec<TaggedEntityTracker> codec()
            {
                return TaggedEntityTracker.CODEC;
            }

            @Override
            public StreamCodec<ByteBuf, TaggedEntityTracker> streamCodec()
            {
                return TaggedEntityTracker.STREAM_CODEC;
            }
        }
    }

    public final IAnimatedEffects.EffectRecord animation;
    public final Level level;
    private final PositionSource source;
    private final PositionSource target;
    public final float sourceScale;
    public final float targetScale;

    public Consumer<EffectPacketInfo> onClientTick = (m)->{};
    public Consumer<EffectPacketInfo> onServerTick = (m)->{};
    public Consumer<EffectPacketInfo> onClientEnd = (m)->{};
    public EffectContext<?> context;

    public Object processedContext = null;
    public int currentTick;
    public int endTick;
    public int removalTick;

    private int _lastTickCheck;
    public Vector3f lastTickSource;
    private Vector3f thisTickSource;

    public EffectPacketInfo(IAnimatedEffects.EffectRecord animation, Level level, PositionSource source,
            PositionSource target, float sourceScale, float targetScale)
    {
        this.level = level;
        this.sourceScale = sourceScale;
        this.targetScale = targetScale;
        this.source = source;
        this.target = target != null ? target : source;
        this.animation = animation;
        this.removalTick = animation.getDuration();
        this.endTick = this.removalTick;
        _lastTickCheck = 0;
        lastTickSource = this.getSource();
    }

    public EffectPacketInfo(IAnimatedEffects.EffectRecord animation, Entity source, List<String> locators)
    {
        this(animation, source.level(), TaggedEntityTracker.create(source, locators), getEntityCentrePlusLook(source),
                source.getBbWidth(), source.getBbWidth());
        this.setContext(new EntityContext(source));
    }

    public EffectPacketInfo(IAnimatedEffects.EffectRecord animation, Level level, Entity source, Entity target,
            Vector3f targetPos)
    {
        this(animation, level, TaggedEntityTracker.create(source), target != null
                        ? getEntityCentre(target)
                        : targetPos != null ? new VectorPositionSource(targetPos) : null, source.getBbWidth(),
                target != null ? target.getBbWidth() : 0.25f);
        this.setContext(new EntityContext(source));
    }

    public EffectPacketInfo setContext(EffectContext<?> context)
    {
        this.context = context;
        return this;
    }

    public Vector3f getSource()
    {
        var pos = source.getPosition(level);
        if (pos.isEmpty())
        {
            this.currentTick = this.removalTick + 1;
            return thisTickSource = new Vector3f();
        }
        if (_lastTickCheck != currentTick)
        {
            _lastTickCheck = currentTick;
            lastTickSource = thisTickSource;
        }
        thisTickSource = pos.get().toVector3f();
        return thisTickSource;
    }

    public Vector3f getPrevSource()
    {
        return lastTickSource;
    }

    public Vector3f getTarget()
    {
        var pos = target.getPosition(level);
        if (pos.isEmpty())
        {
            this.currentTick = this.removalTick + 1;
            return new Vector3f();
        }
        return pos.get().toVector3f();
    }

    public boolean isFinished()
    {
        // Some manual overrides for entity and pokemob contexts
        if (context instanceof Entity e && !e.isAlive()) return true;
        if (context instanceof IPokemob e && e.getTrackedEntity().isAlive()) return true;
        return currentTick >= removalTick;
    }

    public void terminate()
    {
        this.removalTick = -1;
        onClientEnd.accept(this);
    }
}
