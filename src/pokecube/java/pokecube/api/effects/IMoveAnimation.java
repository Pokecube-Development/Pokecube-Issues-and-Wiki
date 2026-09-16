package pokecube.api.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import pokecube.api.moves.MoveEntry;
import thut.api.entity.ai.VectorPosWrapper;
import thut.api.entity.multipart.IMultpart;
import thut.api.maths.Vector3;

import java.util.function.Consumer;

public interface IMoveAnimation
{
    public static class TaggedEntityTracker implements PositionTracker
    {
        public static PositionTracker create(Entity attacker)
        {
            if (attacker instanceof IMultpart<?, ?> multi) for (var key : MoveEntry.DEFAULT_MOVE_SOURCES)
            {
                var tracker = new TaggedEntityTracker(multi, key);
                if (tracker.location != null) return tracker;
            }
            return new EntityTracker(attacker, true);
        }

        IMultpart<?,?> entity;
        Vector3f location;

        public TaggedEntityTracker(IMultpart<?, ?> entity, String key)
        {
            this.entity = entity;
            if (entity.getAttachmentPointMap().containsKey(key))
            {
                var points = entity.getAttachmentPointMap().get(key);
                var index = entity.weSelf().getRandom().nextInt(points.size());
                location = points.get(index).mod();
            }
            else location = null;
        }

        @Override
        public Vec3 currentPosition()
        {
            return new Vec3(location.x, location.y, location.z);
        }

        @Override
        public BlockPos currentBlockPosition()
        {
            return null;
        }

        @Override
        public boolean isVisibleBy(LivingEntity entity)
        {
            return false;
        }
    }

    public static class MovePacketInfo
    {
        public final IMoveAnimation animation;
        public final Level level;
        public final PositionTracker source;
        public final PositionTracker target;
        public final float sourceScale;
        public final float targetScale;

        public Consumer<MovePacketInfo> onClientTick = (m)->{};
        public Consumer<MovePacketInfo> onServerTick = (m)->{};
        public float currentTick;
        public float endTick;
        public float removalTick;

        public MovePacketInfo(IMoveAnimation animation, Level level, PositionTracker source, PositionTracker target,
                float sourceScale, float targetScale)
        {
            this.level = level;
            this.sourceScale = sourceScale;
            this.targetScale = targetScale;
            this.source = source;
            this.target = target != null ? target : source;
            this.animation = animation;
            this.removalTick = animation.getDuration();
        }

        public MovePacketInfo(IMoveAnimation animation, Level level, Entity source, Entity target, Vector3 targetPos)
        {
            this(animation, level, TaggedEntityTracker.create(source), target != null
                            ? new EntityTracker(target, true)
                            : targetPos != null ? new VectorPosWrapper(targetPos) : null, source.getBbWidth(),
                    target != null ? target.getBbWidth() : 0.25f);
        }

        public boolean isFinished()
        {
            return currentTick >= removalTick;
        }
    }

    /**
     * How far into the duration should the move actually be applied.
     */
    public int getApplicationTick();
    /**
     * Sets the duration.
     */
    public void setDuration(int duration);
    /**
     * How long this animation plays for in world ticks.
     */
    public int getDuration();

    /** Initialise colours for the move. */
    @OnlyIn(Dist.CLIENT)
    default void reallyInitRGBA()
    {}
    /**
     * Actually plays the animation in the world, this is called every render tick for the number of world ticks
     * specificed in getDuration(); This is used for direct GL call rendering
     */
    @OnlyIn(Dist.CLIENT)
    default public void clientAnimation(final PoseStack mat, final MultiBufferSource buffer, final MovePacketInfo info,
            final float partialTick, int packedLightIn)
    {}

    /**
     * Used if you need to spawn in something like thunder effects.
     */
    @OnlyIn(Dist.CLIENT)
    default public void spawnClientEntities(final MovePacketInfo info, float partialTicks)
    {}
}
