package thut.api.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.nfunk.jep.JEP;

import net.minecraft.world.entity.Entity;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.IAnimationChanger;

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

    /**
     * List of non-looping animations to run during an existing animation.
     * 
     * @return
     */
    List<String> transientAnimations();

    /**
     * The thing we animate for, for mobs, this is the Entity itself.
     * 
     * @return
     */
    Object getContext();

    Map<Object, Object> activeParticles();

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
         * This is the ticksExisted before this render tick for the object being
         * rendered
         */
        public int lastTick = 0;

        public float yawCapMax = 180;
        public float yawCapMin = -180;
        public float pitchCapMax = 40;
        public float pitchCapMin = -40;

        public int yawAxis = 2;
        public int pitchAxis = 0;
        public int yawDirection = -1;
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

    public static class MolangVars
    {
        public static final Map<String, String> MOLANG_MAP = new HashMap<>();
        public static final Map<String, Double> JEP_VARS = new HashMap<>();

        static
        {
            MOLANG_MAP.put("query.anim_time", "(t/20)");
            MOLANG_MAP.put("query.ground_speed", "l");
            MOLANG_MAP.put("query.health", "health");
            MOLANG_MAP.put("query.max_health", "max_health");
            MOLANG_MAP.put("query.is_in_water", "is_in_water");
            MOLANG_MAP.put("query.is_in_water_or_rain", "is_in_water_or_rain");
            MOLANG_MAP.put("query.is_on_fire", "is_on_fire");
            MOLANG_MAP.put("query.is_on_fire", "is_on_fire");
            MOLANG_MAP.put("query.on_fire_time", "on_fire_time");
            MOLANG_MAP.put("query.is_on_ground", "is_on_ground");
            MOLANG_MAP.put("query.yaw_speed", "yaw_speed");

            Set<String> vars = new HashSet<>(MOLANG_MAP.keySet());
            for (String s : vars) MOLANG_MAP.put(s.replace("query.", "q."), MOLANG_MAP.get(s));

            JEP_VARS.put("t", 0.);
            JEP_VARS.put("l", 0.);
            JEP_VARS.put("health", 20.);
            JEP_VARS.put("max_health", 20.);
            JEP_VARS.put("is_in_water", 0.);
            JEP_VARS.put("is_in_water_or_rain", 0.);
            JEP_VARS.put("is_on_fire", 0.);
            JEP_VARS.put("on_fire_time", 0.);
            JEP_VARS.put("is_on_ground", 1.);
            JEP_VARS.put("yaw_speed", 0.);
        }

        public double t = 0;
        public double l = 0;

        public double health = 20;
        public double max_health = 20;

        public double is_in_water = 0;
        public double is_in_water_or_rain = 0;

        public double is_on_fire = 0;
        public double on_fire_time = 0;

        public double is_on_ground = 1;
        public double yaw_speed = 0;

        public void updateJEP(JEP jep, double anim_time, double walk_time)
        {
            jep.setVarValue("t", anim_time);
            jep.setVarValue("l", walk_time);
            jep.setVarValue("health", health);
            jep.setVarValue("max_health", max_health);
            jep.setVarValue("is_in_water", is_in_water);
            jep.setVarValue("is_in_water_or_rain", is_in_water_or_rain);
            jep.setVarValue("is_on_fire", is_on_fire);
            jep.setVarValue("on_fire_time", on_fire_time);
            jep.setVarValue("is_on_ground", is_on_ground);
            jep.setVarValue("yaw_speed", yaw_speed);
        }

        protected double t_0 = 0;

        public void startTimer(float timer)
        {
            t_0 = timer;
        }

        public double getAnimTime()
        {
            return t - t_0;
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

        Collection<Animation> getTransientPlaying();

        void setContext(IAnimated context);

        IAnimated getContext();

        void setAnimationChanger(IAnimationChanger changer);

        /**
         * This is the animation about to be run.
         *
         * @param name
         */
        void setPendingAnimations(final List<Animation> list, final String name);

        /**
         * This should get whatever animation we think the entity should be
         * doing.
         *
         * @param entityIn
         * @return
         */
        String getAnimation(Entity entityIn);

        void preRunAll();

        void postRunAll();

        void preRunAnim(Animation animation);

        void postRunAnim(Animation animation);

        boolean isFixed();

        void setFixed(boolean fixed);

        void overridePlaying(String anim);

        @Nonnull
        HeadInfo getHeadInfo();

        @Nonnull
        MolangVars getMolangVars();

        void initAnimations(Map<String, List<Animation>> map, String _default);

        default void initHeadInfoAndMolangs(Entity entityIn, final float limbSwing, final float limbSwingAmount,
                final float ageInTicks, final float netHeadYaw, final float headPitch)
        {
            HeadInfo info = this.getHeadInfo();
            MolangVars molangs = this.getMolangVars();
            if (!info.fixed)
            {
                info.headPitch = headPitch;
                info.headYaw = netHeadYaw;
            }
            info.currentTick = entityIn.tickCount;
            info.lastTick = entityIn.tickCount;

            final float limbSpeedFactor = 3f;
            molangs.l = limbSpeedFactor * limbSwing;
            molangs.t = ageInTicks;
            if (molangs.t < 0) molangs.t = 0;

            molangs.is_on_ground = entityIn.isOnGround() ? 1 : 0;
            molangs.is_in_water = entityIn.isInWater() ? 1 : 0;
            molangs.is_on_fire = entityIn.isOnFire() ? 1 : 0;

            molangs.yaw_speed = entityIn.yRot - entityIn.yRotO;

            molangs.on_fire_time = entityIn.getRemainingFireTicks();
        }
    }
}
