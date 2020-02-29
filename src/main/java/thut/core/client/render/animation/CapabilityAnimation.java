package thut.core.client.render.animation;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

        Map<Animation, Float>        stepsMap = Maps.newHashMap();
        Set<Animation>               playing  = Sets.newHashSet();
        private final Set<Animation> pending  = Sets.newHashSet();

        @Override
        public void clean()
        {
            this.stepsMap.clear();
            this.pending.clear();
            this.playing.clear();
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityAnimation.CAPABILITY.orEmpty(cap, this.holder);
        }

        @Override
        public Set<Animation> getPendingAnimations()
        {
            return this.pending;
        }

        @Override
        public Set<Animation> getPlaying()
        {
            return this.playing;
        }

        @Override
        public void setPendingAnimations(final List<Animation> name, final float step)
        {
            if (name != null)
            {
                this.pending.addAll(name);
                for (final Animation anim : name)
                    this.stepsMap.put(anim, step);
            }
            else this.pending.clear();
            if (this.playing.isEmpty()) this.playing.addAll(name);
            else this.pending.addAll(name);
        }

        @Override
        public void setStep(final Animation animation, final float step)
        {
            final float time = step - this.stepsMap.put(animation, step);
            if (time > animation.getLength())
            {
                this.playing.remove(animation);
                if (this.playing.isEmpty())
                {
                    this.playing.addAll(this.pending);
                    this.pending.clear();
                }
            }
        }

        @Override
        public String getAnimation(final Entity entityIn)
        {
            // TODO Auto-generated method stub
            return "idle";
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
        Set<Animation> getPendingAnimations();

        Set<Animation> getPlaying();

        /**
         * This is the animation about to be run.
         *
         * @param name
         */
        void setPendingAnimations(List<Animation> name, float step);

        /** Sets the last tick this animation was run. Can set to 0 to count
         * this animation as cleared.
         *
         * @param animation
         * @param step */
        void setStep(Animation animation, float step);

        /**
         * This should get whatever animation we think the entity should be
         * doing.
         *
         * @param entityIn
         * @return
         */
        String getAnimation(Entity entityIn);
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
