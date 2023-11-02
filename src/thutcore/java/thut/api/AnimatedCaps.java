package thut.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import thut.api.entity.IAnimated;
import thut.core.common.ThutCore;

public class AnimatedCaps
{

    public static class Impl implements IAnimated, ICapabilityProvider
    {
        private final LazyOptional<IAnimated> holder = LazyOptional.of(() -> this);

        private final List<String> anims = Lists.newArrayList();
        private final List<String> transients = Lists.newArrayList();
        private final Map<Object, Object> particles = new HashMap<>();
        private final Object context;

        public Impl(Object context)
        {
            this.context = context;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutCaps.ANIMATED.orEmpty(cap, this.holder);
        }

        @Override
        public List<String> getChoices()
        {
            return this.anims;
        }

        @Override
        public List<String> transientAnimations()
        {
            return transients;
        }

        @Override
        public Object getContext()
        {
            return context;
        }

        @Override
        public Map<Object, Object> activeParticles()
        {
            return particles;
        }
    }

    public static final ResourceLocation WRAP = new ResourceLocation("thutcore:animated_mob");

    private static void attachMobs(final AttachCapabilitiesEvent<Entity> event)
    {
        // Check if someone else adds this first (like say an IPokemob
        for (final ICapabilityProvider p : event.getCapabilities().values())
            if (p.getCapability(ThutCaps.ANIMATED).isPresent()) return;
        event.addCapability(AnimatedCaps.WRAP, new Impl(event.getObject()));
    }

    public static IAnimated getAnimated(final ICapabilityProvider in)
    {
        return ThutCaps.getAnimated(in);
    }

    public static void setup()
    {
        ThutCore.FORGE_BUS.addGenericListener(Entity.class, EventPriority.LOWEST, AnimatedCaps::attachMobs);
    }

}
