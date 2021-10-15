package thut.core.client.render.animation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityAnimation
{
    public static class DefaultImpl implements IAnimationHolder, ICapabilitySerializable<CompoundNBT>
    {
        private static final List<Animation>         EMPTY  = Collections.emptyList();
        private final LazyOptional<IAnimationHolder> holder = LazyOptional.of(() -> this);

        Map<String, List<Animation>> anims = Maps.newHashMap();

        List<Animation> playingList = DefaultImpl.EMPTY;

        Object2IntOpenHashMap<UUID> non_static = new Object2IntOpenHashMap<>();
        List<Animation>             keys       = Lists.newArrayList();

        String pending = "";
        String playing = "";

        boolean fixed = false;

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
            if (this.keys.isEmpty() && !this.pending.isEmpty())
            {
                this.playingList = this.anims.getOrDefault(this.pending, DefaultImpl.EMPTY);
                this.playing = this.pending;
                this.non_static.clear();
                for (final Animation a : this.playingList)
                    if (a.getLength() > 0)
                    {
                        this.non_static.put(a._uuid, 0);
                        this.keys.add(a);
                    }
            }
            return this.playingList;
        }

        @Override
        public void setPendingAnimations(final List<Animation> list, final String name)
        {
            this.anims.put(name, Lists.newArrayList(list));
            if (this.fixed) this.pending = this.playing;
            else this.pending = name;
            this.getPlaying();
        }

        @Override
        public void setStep(final Animation animation, final float step)
        {
            if (this.fixed) return;
            // Only reset if we have a pending animation.
            final int l = animation.getLength();
            final boolean finished = l != 0 && step > l || animation.hasLimbBased;
            if (finished && (!animation.loops || !this.pending.equals(this.playing))) this.non_static.put(
                    animation._uuid, 0);
            else this.non_static.put(animation._uuid, l != 0 ? l : 10);
        }

        @Override
        public String getAnimation(final Entity entityIn)
        {
            return this.playing;
        }

        @Override
        public void preRun()
        {
            this.non_static.replaceAll((a, i) -> 0);
        }

        @Override
        public void postRun()
        {
            this.keys.removeIf(a ->
            {
                final int i = this.non_static.getInt(a._uuid);
                if (i <= 0)
                {
                    this.non_static.removeInt(a._uuid);
                    return true;
                }
                return false;
            });
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT tag = new CompoundNBT();
            tag.putString("pl", this.playing);
            tag.putString("pn", this.pending);
            tag.putBoolean("f", this.fixed);
            return tag;
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            this.playing = nbt.getString("pl");
            this.pending = nbt.getString("pn");
            this.fixed = nbt.getBoolean("f");
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
