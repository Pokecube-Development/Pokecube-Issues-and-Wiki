package thut.api.entity;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import thut.api.ThutCaps;
import thut.core.common.ThutCore;

public class ShearableCaps
{
    public static class SheepImpl extends Impl
    {
        final Sheep sheep;

        public SheepImpl(final Sheep sheep)
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
            return ThutCaps.SHEARABLE.orEmpty(cap, this.holder);
        }

        @Override
        public boolean isSheared()
        {
            return false;
        }

        @Override
        public void shear()
        {}

    }

    public static final ResourceLocation LOC = new ResourceLocation("thutcore:shearable");

    public static IShearable get(final ICapabilityProvider in)
    {
        return ThutCaps.getShearable(in);
    }

    private static void attachMobs(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getCapabilities().containsKey(ShearableCaps.LOC)) return;
        if (event.getObject() instanceof Sheep sheep) event.addCapability(ShearableCaps.LOC, new SheepImpl(sheep));
    }

    public static void setup()
    {
        ThutCore.FORGE_BUS.addGenericListener(Entity.class, ShearableCaps::attachMobs);
    }
}
