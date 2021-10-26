package thut.api;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.entity.IAnimated;

public class AnimatedCaps
{

    public static class Impl implements IAnimated, ICapabilityProvider
    {
        private final LazyOptional<IAnimated> holder = LazyOptional.of(() -> this);

        protected List<String> anims = Lists.newArrayList();

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
    }

    public static class Storage implements Capability.IStorage<IAnimated>
    {
        @Override
        public void readNBT(final Capability<IAnimated> capability, final IAnimated instance, final Direction side,
                final INBT nbt)
        {
        }

        @Override
        public INBT writeNBT(final Capability<IAnimated> capability, final IAnimated instance, final Direction side)
        {
            return null;
        }
    }

    public static final ResourceLocation WRAP = new ResourceLocation("thutcore:animated_mob");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attachMobs(final AttachCapabilitiesEvent<Entity> event)
    {
        // Check if someone else adds this first (like say an IPokemob
        for (final ICapabilityProvider p : event.getCapabilities().values())
            if (p.getCapability(ThutCaps.ANIMATED).isPresent()) return;
        event.addCapability(AnimatedCaps.WRAP, new Impl());
    }

    public static IAnimated getAnimated(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.ANIMATED).orElse(null);
    }

    public static void setup()
    {
        CapabilityManager.INSTANCE.register(IAnimated.class, new Storage(), Impl::new);
        MinecraftForge.EVENT_BUS.register(AnimatedCaps.class);
    }

}
