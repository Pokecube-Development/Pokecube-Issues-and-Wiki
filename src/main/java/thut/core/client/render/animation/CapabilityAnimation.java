package thut.core.client.render.animation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        private static final List<Animation>         EMPTY  = Collections.emptyList();
        private final LazyOptional<IAnimationHolder> holder = LazyOptional.of(() -> this);

        Map<String, List<Animation>> anims = Maps.newHashMap();

        List<Animation> playingList = DefaultImpl.EMPTY;
        List<Animation> non_static  = Lists.newArrayList();

        String pending = "";
        String playing = "";

        @Override
        public void clean()
        {
            this.pending = "";
            this.playing = "";
            this.playingList = DefaultImpl.EMPTY;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityAnimation.CAPABILITY.orEmpty(cap, this.holder);
        }

        @Override
        public String getPendingAnimations()
        {
            return this.pending;
        }

        @Override
        public List<Animation> getPlaying()
        {
            if (this.non_static.isEmpty() && !this.pending.isEmpty())
            {
                this.playingList = this.anims.getOrDefault(this.pending, DefaultImpl.EMPTY);
                this.playing = this.pending;
                this.non_static.clear();
                for (final Animation a : this.playingList)
                    if (a.getLength() > 0) this.non_static.add(a);
            }
            return this.playingList;
        }

        @Override
        public void setPendingAnimations(final List<Animation> list, final String name)
        {
            this.anims.put(name, Lists.newArrayList(list));
            this.getPlaying();
            this.pending = name;
        }

        @Override
        public void setStep(final Animation animation, final float step)
        {
            // Only reset if we have a pending animation.
            final int l = animation.getLength();
            if (l != 0 && step > l && !this.pending.equals(this.playing)) this.non_static.remove(animation);
        }

        @Override
        public String getAnimation(final Entity entityIn)
        {
            return this.playing;
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
