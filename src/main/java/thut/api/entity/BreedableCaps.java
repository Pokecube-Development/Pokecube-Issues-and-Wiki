package thut.api.entity;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import thut.api.ThutCaps;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

public class BreedableCaps
{
    public static class Impl implements IBreedingMob, ICapabilityProvider
    {
        private final LazyOptional<IBreedingMob> holder = LazyOptional.of(() -> this);

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutCaps.BREEDS.orEmpty(cap, this.holder);
        }

        @Override
        public AgeableMob getEntity()
        {
            return null;
        }
    }

    public static class AgeableWrapper extends Impl
    {
        final AgeableMob wrapped;

        public AgeableWrapper(final AgeableMob wrapped)
        {
            this.wrapped = wrapped;
        }

        @Override
        public Object getChild(final IBreedingMob male)
        {
            return this.wrapped.getBreedOffspring((ServerLevel) this.wrapped.level(), male.getEntity());
        }

        @Override
        public boolean canMate(final AgeableMob other)
        {
            try
            {
                if (this.wrapped instanceof Animal a && other instanceof Animal b) return a.canMate(b);
            }
            catch (final Exception e)
            {
                if (!ThutCore.conf.supress_warns)
                    ThutCore.LOGGER.warn("Warning, Mob {} has messed up canMateWith check!",
                            RegHelper.getKey(this.wrapped.getType()));
                return false;
            }
            return other.getClass() == this.wrapped.getClass();
        }

        @Override
        public boolean canBreed()
        {
            if (this.wrapped instanceof Animal animal) return animal.canBreed();
            return super.canBreed();
        }

        @Override
        public boolean isBreeding()
        {
            if (this.wrapped instanceof Animal animal) return animal.isInLove();
            return super.isBreeding();
        }

        @Override
        public void setReadyToMate(@Nullable final Player cause)
        {
            if (this.wrapped instanceof Animal animal) animal.setInLove(cause);
        }

        @Override
        public void resetLoveStatus()
        {
            if (this.wrapped instanceof Animal animal) animal.resetLove();
        }

        @Override
        public ServerPlayer getCause()
        {
            if (this.wrapped instanceof Animal animal) return animal.getLoveCause();
            return super.getCause();
        }
    }

    public static final ResourceLocation WRAP = new ResourceLocation("thutcore:breedable_wrap");

    private static void attachMobs(final AttachCapabilitiesEvent<Entity> event)
    {
        // Check if someone else adds this first (like say an IPokemob
        for (final ICapabilityProvider p : event.getCapabilities().values())
            if (p.getCapability(ThutCaps.BREEDS).isPresent()) return;
        if (event.getObject() instanceof AgeableMob mob)
            event.addCapability(BreedableCaps.WRAP, new AgeableWrapper(mob));
    }

    public static IBreedingMob getBreedable(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.BREEDS).orElse(null);
    }

    public static void setup()
    {
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, EventPriority.LOWEST, BreedableCaps::attachMobs);
    }
}
