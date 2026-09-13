package pokecube.api.moves.utils;

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

public interface IMoveAnimation
{
    public static class TaggedEntityTracker implements PositionTracker
    {
        public static PositionTracker create(MoveEntry move, Entity attacker)
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
        public final MoveEntry move;
        public final Level level;
        public final PositionTracker source;
        public final PositionTracker target;
        public final float attackerScale;
        public final float attackedScale;
        public float currentTick;

        public float lastApplyTimer = -1;

        public MovePacketInfo(final MoveEntry move, Level level, PositionTracker source, PositionTracker target,
                float sourceScale, float targetScale)
        {
            this.move = move;
            this.level = level;
            this.attackerScale = sourceScale;
            this.attackedScale = targetScale;
            this.source = source;
            this.target = target != null ? target : source;
        }

        public MovePacketInfo(final MoveEntry move, Level level, Entity source, Entity target, Vector3 targetPos)
        {
            this(move, level, TaggedEntityTracker.create(move, source), target != null
                            ? new EntityTracker(target, true)
                            : targetPos != null ? new VectorPosWrapper(targetPos) : null, source.getBbWidth(),
                    target != null ? target.getBbWidth() : 0.25f);
        }
    }

    /**
     * Actually plays the animation in the world, this is called every render tick for the number of world ticks
     * specificed in getDuration(); This is used for direct GL call rendering
     */
    @OnlyIn(Dist.CLIENT)
    default public void clientAnimation(final PoseStack mat, final MultiBufferSource buffer, final MovePacketInfo info,
            final float partialTick, int packedLightIn)
    {}

    /**
     * How far into the duration should the move actually be applied.
     */
    public int getApplicationTick();

    /**
     * How long this animation plays for in world ticks.
     */
    public int getDuration();

    /** Initialise colours for the move. */
    @OnlyIn(Dist.CLIENT)
    default void reallyInitRGBA()
    {}

    /**
     * Sets the duration.
     */
    public void setDuration(int duration);

    /**
     * Used if you need to spawn in something like thunder effects.
     */
    @OnlyIn(Dist.CLIENT)
    default public void spawnClientEntities(final MovePacketInfo info, float partialTicks)
    {}
}
