package pokecube.api.moves.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import pokecube.api.moves.MoveEntry;
import thut.api.maths.Vector3;

public interface IMoveAnimation
{
    public static class MovePacketInfo
    {
        public final MoveEntry move;
        public final Entity attacker;
        public final Entity attacked;
        public final Vector3 source;
        public final Vector3 target;
        public float currentTick;

        public float lastApplyTimer = -1;

        public MovePacketInfo(final MoveEntry move, final Entity attacker, final Entity attacked, final Vector3 source,
                final Vector3 target)
        {
            this.move = move;
            this.attacked = attacked;
            this.attacker = attacker;
            this.source = source;
            this.target = target;
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
