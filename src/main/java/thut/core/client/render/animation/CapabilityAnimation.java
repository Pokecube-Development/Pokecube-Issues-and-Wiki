package thut.core.client.render.animation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;
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
        private final LazyOptional<IAnimationHolder> holder   = LazyOptional.of(() -> this);

        Map<UUID, Integer>                           stepsMap = Maps.newHashMap();
        List<Animation>                              playing  = Lists.newArrayList();
        private final List<Animation>                pending  = Lists.newArrayList();

        @Override
        public void clean()
        {
            this.stepsMap.clear();
            this.pending.clear();
            ;
            this.playing.clear();
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityAnimation.CAPABILITY.orEmpty(cap, this.holder);
        }

        @Override
        public List<Animation> getPendingAnimations()
        {
            return this.pending;
        }

        @Override
        public List<Animation> getPlaying()
        {
            return this.playing;
        }

        @Override
        public int getStep(final Animation animation)
        {
            int step = 0;
            if (this.stepsMap.containsKey(animation.id)) step = this.stepsMap.get(animation.id);
            return step;
        }

        @Override
        public void setPendingAnimations(final List<Animation> name)
        {
            if (name != null) this.pending.addAll(name);
            else this.pending.clear();
            if (this.playing.isEmpty() && !this.pending.isEmpty())
                if (!this.pending.isEmpty()) this.playing.add(this.pending.remove(0));
        }

        @Override
        public void setStep(final Animation animation, final int step)
        {
            this.stepsMap.put(animation.id, step);
            if (step == animation.length)
            {
                if (!this.pending.isEmpty()) this.playing.add(this.pending.remove(0));
                this.playing.remove(animation);
            }
        }
    }

    public static interface IAnimationHolder
    {
        /** should clear the ticks animations were run on */
        void clean();

        /** Gets the animation about to be run.
         *
         * @return */
        List<Animation> getPendingAnimations();

        List<Animation> getPlaying();

        /** the last tick this animation was run. Should return 0 if the
         * animation hasn't been run.
         *
         * @param animation
         * @return */
        int getStep(Animation animation);

        /** This is the animation about to be run.
         *
         * @param name */
        void setPendingAnimations(List<Animation> name);

        /** Sets the last tick this animation was run. Can set to 0 to count
         * this animation as cleared.
         *
         * @param animation
         * @param step */
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

    private static final Set<Class<? extends Entity>> ANIMATE    = Sets.newHashSet();
    private static final ResourceLocation             ANIM       = new ResourceLocation("thutcore:animations");

    @CapabilityInject(IAnimationHolder.class)
    public static final Capability<IAnimationHolder>  CAPABILITY = null;

    @SubscribeEvent
    public static void attachCap(final AttachCapabilitiesEvent<Entity> event)
    {
        if (CapabilityAnimation.ANIMATE.contains(event.getObject().getClass()))
            event.addCapability(CapabilityAnimation.ANIM, new DefaultImpl());
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
