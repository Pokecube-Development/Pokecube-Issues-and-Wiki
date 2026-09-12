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
            if (entity.getAttachmentPoints().containsKey(key))
            {
                // TODO decide if to randomise this?
                location = entity.getAttachmentPoints().get(key).getFirst();
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

        public MovePacketInfo(final MoveEntry move, final Entity attacker, final Entity attacked, final Vector3 source,
                final Vector3 target)
        {
            this.move = move;
            this.level = attacker.level();
            this.attackerScale = attacker.getBbWidth();
            this.attackedScale = attacked != null ? attacked.getBbWidth() : 0.25f;
            this.source = TaggedEntityTracker.create(move, attacker);
            this.target = target == null
                    ? attacked != null ? new EntityTracker(attacked, true) : null
                    : new VectorPosWrapper(target);
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
