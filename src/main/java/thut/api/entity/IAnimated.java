package thut.api.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import thut.api.entity.animation.Animation;

public interface IAnimated
{
    /**
     * This should return a prioritised list of possible animations for the mob.
     * The lower the index on the list, the higher the priority to use. The
     * renderer will walk up the list and pick the first value that it actually
     * has an animation for.
     *
     * @return
     */
    List<String> getChoices();



    public static interface IAnimationHolder
    {
        /** should clear the ticks animations were run on */
        void clean();

        /**
         * Gets the animation about to be run.
         *
         * @return
         */
        String getPendingAnimations();

        List<Animation> getPlaying();

        /**
         * This is the animation about to be run.
         *
         * @param name
         */
        void setPendingAnimations(final List<Animation> list, final String name);

        /**
         * Sets the last tick this animation was run. Can set to 0 to count
         * this animation as cleared.
         *
         * @param animation
         * @param step
         */
        void setStep(Animation animation, float step);

        /**
         * This should get whatever animation we think the entity should be
         * doing.
         *
         * @param entityIn
         * @return
         */
        String getAnimation(Entity entityIn);

        void preRun();

        void postRun();
    }
}
