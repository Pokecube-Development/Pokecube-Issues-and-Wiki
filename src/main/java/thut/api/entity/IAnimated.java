package thut.api.entity;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.Entity;
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

    public static class HeadInfo
    {
        public static final HeadInfo DUMMY = new HeadInfo();

        /**
         * This should be updated to match the mob, incase the IModel needs to
         * do custom rendering itself.
         */
        public float headYaw;
        /**
         * This should be updated to match the mob, incase the IModel needs to
         * do custom rendering itself.
         */
        public float headPitch;

        /** This is the current ticksExisted for the object being rendered.. */
        public int currentTick = 0;
        /**
         * This is the ticksExisted before this render tick for the object
         * being rendered
         */
        public int lastTick    = 0;

        public float yawCapMax   = 180;
        public float yawCapMin   = -180;
        public float pitchCapMax = 40;
        public float pitchCapMin = -40;

        public int yawAxis        = 1;
        public int pitchAxis      = 0;
        public int yawDirection   = 1;
        public int pitchDirection = 1;

        public boolean fixed = false;

        public void copyFrom(final HeadInfo other)
        {
            this.yawAxis = other.yawAxis;
            this.yawCapMin = other.yawCapMin;
            this.yawCapMax = other.yawCapMax;
            this.yawDirection = other.yawDirection;

            this.pitchAxis = other.pitchAxis;
            this.pitchCapMin = other.pitchCapMin;
            this.pitchCapMax = other.pitchCapMax;
            this.pitchDirection = other.pitchDirection;
        }
    }

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

        boolean isFixed();

        void setFixed(boolean fixed);

        void overridePlaying(String anim);

        @Nonnull
        HeadInfo getHeadInfo();
    }
}
