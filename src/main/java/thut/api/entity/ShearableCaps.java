package thut.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShearableCaps
{
    public static class Sheep extends Impl
    {
        final SheepEntity sheep;

        public Sheep(final SheepEntity sheep)
        {
            this.sheep = sheep;
        }

        @Override
        public boolean isSheared()
        {
            return this.sheep.isSheared();
        }

        @Override
        public void shear()
        {
            this.sheep.setSheared(true);
        }
    }

    public static class Wrapper extends Impl
    {
        final IShearable wrapped;

        public Wrapper(final IShearable wrapped)
        {
            this.wrapped = wrapped;
        }

        @Override
        public boolean isSheared()
        {
            return this.wrapped.isSheared();
        }

        @Override
        public void shear()
        {
            this.wrapped.shear();
        }

        @Override
        public void shear(final ItemStack shears)
        {
            this.wrapped.shear(shears);
        }
    }

    public static class Impl implements IShearable, ICapabilityProvider
    {

        private final LazyOptional<IShearable> holder = LazyOptional.of(() -> this);

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ShearableCaps.CAPABILITY.orEmpty(cap, this.holder);
        }

        @Override
        public boolean isSheared()
        {
            return false;
        }

        @Override
        public void shear()
        {
        }

    }

    public static class Storage implements Capability.IStorage<IShearable>
    {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void readNBT(final Capability<IShearable> capability, final IShearable instance, final Direction side,
                final INBT nbt)
        {
            if (instance instanceof ICapabilitySerializable) ((ICapabilitySerializable) instance).deserializeNBT(nbt);
        }

        @Override
        public INBT writeNBT(final Capability<IShearable> capability, final IShearable instance, final Direction side)
        {
            if (instance instanceof ICapabilitySerializable<?>) return ((ICapabilitySerializable<?>) instance)
                    .serializeNBT();
            return null;
        }
    }

    @CapabilityInject(IShearable.class)
    public static final Capability<IShearable> CAPABILITY = null;
    public static final ResourceLocation       LOC        = new ResourceLocation("thutcore:shearable");

    public static IShearable get(final ICapabilityProvider in)
    {
        return in.getCapability(ShearableCaps.CAPABILITY).orElse(null);
    }

    @SubscribeEvent
    public static void attachMobs(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getCapabilities().containsKey(ShearableCaps.LOC)) return;
        if (event.getObject() instanceof SheepEntity) event.addCapability(ShearableCaps.LOC, new Sheep(
                (SheepEntity) event.getObject()));
    }

    public static void setup()
    {
        CapabilityManager.INSTANCE.register(IShearable.class, new Storage(), Impl::new);
        MinecraftForge.EVENT_BUS.register(ShearableCaps.class);
    }
}
