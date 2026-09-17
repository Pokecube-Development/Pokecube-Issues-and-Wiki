package pokecube.api.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IAnimatedEffects
{
    public static record EffectRecord(String key, IAnimatedEffects effect)
    {
        public int getDuration()
        {
            return effect().getDuration();
        }
        public int getApplicationTick()
        {
            return effect().getApplicationTick();
        }
    }
    /**
     * How far into the duration should the move actually be applied.
     * This is relevant for effects added to attacks
     */
    default int getApplicationTick()
    {
        return 0;
    }
    /**
     * Sets the duration.
     */
    void setDuration(int duration);
    /**
     * How long this animation plays for in world ticks.
     */
    int getDuration();

    /**
     * Whether we do custom rendering beyond simple particle effects, if this is the case, we will have clientAnimation
     * run during the RenderLevelEvent.
     */
    default boolean hasComplexRender()
    {
        return false;
    }

    /** Initialise colours for the move. */
    @OnlyIn(Dist.CLIENT)
    default void reallyInitRGBA()
    {}
    /**
     * Actually plays the animation in the world, this is called every render tick for the number of world ticks
     * specificed in getDuration(); This is used for direct GL call rendering
     */
    @OnlyIn(Dist.CLIENT)
    default void clientAnimation(final PoseStack mat, final MultiBufferSource buffer, final EffectPacketInfo info,
            final float partialTick, int packedLightIn)
    {}

    /**
     * Used to spawn particle effects, etc.
     */
    @OnlyIn(Dist.CLIENT)
    default void spawnClientEntities(final EffectPacketInfo info, float partialTicks)
    {}
}
