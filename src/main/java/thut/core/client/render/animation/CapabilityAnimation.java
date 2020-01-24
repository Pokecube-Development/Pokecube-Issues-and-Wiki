package thut.core.client.render.animation;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityAnimation
{
    public static class DefaultImpl implements IAnimationHolder, ICapabilityProvider
    {
        private final LazyOptional<IAnimationHolder> holder = LazyOptional.of(() -> this);

        Map<UUID, Integer> stepsMap = Maps.newHashMap();
        Set<Animation>     playing  = Sets.newHashSet();
        private String     pending  = "idle";
        private String     current  = "idle";

        @Override
        public void clean()
        {
            this.stepsMap.clear();
            this.pending = this.current = "idle";
            this.playing.clear();
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityAnimation.CAPABILITY.orEmpty(cap, this.holder);
        }

        @Override
        public String getCurrentAnimation()
        {
            return this.current;
        }

        @Override
        public String getPendingAnimation()
        {
            return this.pending;
        }

        @Override
        public Set<Animation> getPlaying()
        {
            return this.playing;
        }

        @Override
        public int getStep(final Animation animation)
        {
            int step = 0;
            if (this.stepsMap.containsKey(animation.id)) step = this.stepsMap.get(animation.id);
            if (!this.pending.equals(this.current)) if (step == animation.length) this.current = this.pending;
            return step;
        }

        @Override
        public void setCurrentAnimation(final String name)
        {
            this.current = name;
        }

        @Override
        public void setPendingAnimation(final String name)
        {
            this.pending = name;
        }

        @Override
        public void setStep(final Animation animation, final int step)
        {
            this.stepsMap.put(animation.id, step);
        }
    }

    public static interface IAnimationHolder
    {
        /** should clear the ticks animations were run on */
        void clean();

        /**
         * Gets the animation currently being run.
         *
         * @return
         */
        String getCurrentAnimation();

        /**
         * Gets the animation about to be run.
         *
         * @return
         */
        String getPendingAnimation();

        Set<Animation> getPlaying();

        /**
         * the last tick this animation was run. Should return 0 if the
         * animation hasn't been run.
         *
         * @param animation
         * @return
         */
        int getStep(Animation animation);

        /**
         * This is the animation that is currently being run.
         *
         * @param name
         */
        void setCurrentAnimation(String name);

        /**
         * This is the animation about to be run.
         *
         * @param name
         */
        void setPendingAnimation(String name);

        /**
         * Sets the last tick this animation was run. Can set to 0 to count
         * this animation as cleared.
         *
         * @param animation
         * @param step
         */
        void setStep(Animation animation, int step);
    }

    private static class Storage implements Capability.IStorage<IAnimationHolder>
    {
        @Override
        public void readNBT(final Capability<IAnimationHolder> capability, final IAnimationHolder instance,
                final Direction side, final INBT nbt)
        {
        }

        @Override
        public INBT writeNBT(final Capability<IAnimationHolder> capability, final IAnimationHolder instance,
                final Direction side)
        {
            return null;
        }
    }

    private static final Set<Class<? extends Entity>> ANIMATE = Sets.newHashSet();
    private static final ResourceLocation             ANIM    = new ResourceLocation("thutcore:animations");

    @CapabilityInject(IAnimationHolder.class)
    public static final Capability<IAnimationHolder> CAPABILITY = null;

    @SubscribeEvent
    public static void attachCap(final AttachCapabilitiesEvent<Entity> event)
    {
        if (CapabilityAnimation.ANIMATE.contains(event.getObject().getClass())) event.addCapability(
                CapabilityAnimation.ANIM, new DefaultImpl());
    }

    public static void registerAnimateClass(final Class<? extends Entity> clazz)
    {
        CapabilityAnimation.ANIMATE.add(clazz);
    }

    public static void setup()
    {
        CapabilityManager.INSTANCE.register(IAnimationHolder.class, new Storage(), DefaultImpl::new);
        MinecraftForge.EVENT_BUS.register(CapabilityAnimation.class);
    }
}
