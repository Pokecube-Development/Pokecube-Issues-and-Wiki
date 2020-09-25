package thut.api.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.ThutCaps;

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
        public AgeableEntity getEntity()
        {
            return null;
        }
    }

    public static class AgeableWrapper extends Impl
    {
        final AgeableEntity wrapped;

        public AgeableWrapper(final AgeableEntity wrapped)
        {
            this.wrapped = wrapped;
        }

        @Override
        public Object getChild(final IBreedingMob male)
        {
            return this.wrapped.func_241840_a((ServerWorld) this.wrapped.getEntityWorld(), male.getEntity());
        }

        @Override
        public boolean canMate(final AgeableEntity other)
        {
            if (this.wrapped instanceof AnimalEntity && other instanceof AnimalEntity)
                return ((AnimalEntity) this.wrapped).canMateWith((AnimalEntity) other);
            return other.getClass() == this.wrapped.getClass();
        }

        @Override
        public boolean canBreed()
        {
            if (this.wrapped instanceof AnimalEntity) return ((AnimalEntity) this.wrapped).canBreed();
            return super.canBreed();
        }

        @Override
        public boolean isBreeding()
        {
            if (this.wrapped instanceof AnimalEntity) return ((AnimalEntity) this.wrapped).isInLove();
            return super.isBreeding();
        }

        @Override
        public void setReadyToMate(@Nullable final PlayerEntity cause)
        {
            if (this.wrapped instanceof AnimalEntity) ((AnimalEntity) this.wrapped).setInLove(cause);
        }

        @Override
        public void resetLoveStatus()
        {
            if (this.wrapped instanceof AnimalEntity) ((AnimalEntity) this.wrapped).resetInLove();
        }

        @Override
        public ServerPlayerEntity getCause()
        {
            if (this.wrapped instanceof AnimalEntity) return ((AnimalEntity) this.wrapped).getLoveCause();
            return super.getCause();
        }
    }

    public static class Storage implements Capability.IStorage<IBreedingMob>
    {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void readNBT(final Capability<IBreedingMob> capability, final IBreedingMob instance,
                final Direction side, final INBT nbt)
        {
            if (instance instanceof ICapabilitySerializable) ((ICapabilitySerializable) instance).deserializeNBT(nbt);
        }

        @Override
        public INBT writeNBT(final Capability<IBreedingMob> capability, final IBreedingMob instance,
                final Direction side)
        {
            if (instance instanceof ICapabilitySerializable<?>) return ((ICapabilitySerializable<?>) instance)
                    .serializeNBT();
            return null;
        }
    }

    public static final ResourceLocation WRAP = new ResourceLocation("thutcore:breedable_wrap");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attachMobs(final AttachCapabilitiesEvent<Entity> event)
    {
        // Check if someone else adds this first (like say an IPokemob
        for (final ICapabilityProvider p : event.getCapabilities().values())
            if (p.getCapability(ThutCaps.BREEDS).isPresent()) return;
        if (event.getObject() instanceof AgeableEntity) event.addCapability(BreedableCaps.WRAP, new AgeableWrapper(
                (AgeableEntity) event.getObject()));
    }

    public static IBreedingMob getBreedable(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.BREEDS).orElse(null);
    }

    public static void setup()
    {
        CapabilityManager.INSTANCE.register(IBreedingMob.class, new Storage(), Impl::new);
        MinecraftForge.EVENT_BUS.register(BreedableCaps.class);
    }
}
