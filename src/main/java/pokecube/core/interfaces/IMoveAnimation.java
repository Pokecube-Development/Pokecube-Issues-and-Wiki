package pokecube.core.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.maths.Vector3;

public interface IMoveAnimation
{
    public static class MovePacketInfo
    {
        public final Move_Base move;
        public final Entity    attacker;
        public final Entity    attacked;
        public final Vector3   source;
        public final Vector3   target;
        public int             currentTick;

        public MovePacketInfo(final Move_Base move, final Entity attacker, final Entity attacked, final Vector3 source, final Vector3 target)
        {
            this.move = move;
            this.attacked = attacked;
            this.attacker = attacker;
            this.source = source;
            this.target = target;
        }
    }

    /**
     * Actually plays the animation in the world, this is called every render
     * tick for the number of world ticks specificed in getDuration(); This is
     * used for direct GL call rendering
     *
     * @param info
     * @param world
     * @param partialTick
     */
    @OnlyIn(Dist.CLIENT)
    default public void clientAnimation(final PoseStack mat, final MultiBufferSource buffer,
            final MovePacketInfo info, final float partialTick)
    {
    }

    /**
     * How far into the duration should the move actually be applied.
     *
     * @return
     */
    public int getApplicationTick();

    /**
     * How long this animation plays for in world ticks.
     *
     * @return
     */
    public int getDuration();

    @OnlyIn(Dist.CLIENT)
    /** Initialise colours for the move. */
    default void reallyInitRGBA()
    {
    }

    /**
     * Sets the duration.
     *
     * @param duration
     */
    public void setDuration(int duration);

    /**
     * Used if you need to spawn in something like thunder effects.
     *
     * @param info
     */
    @OnlyIn(Dist.CLIENT)
    default public void spawnClientEntities(final MovePacketInfo info)
    {
    }
}
