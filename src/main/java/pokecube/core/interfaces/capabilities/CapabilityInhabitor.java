package pokecube.core.interfaces.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.interfaces.IInhabitor;

public class CapabilityInhabitor
{
    @CapabilityInject(IInhabitor.class)
    public static final Capability<IInhabitor> CAPABILITY = null;

    public static class InhabitorProvider implements ICapabilityProvider
    {
        protected final IInhabitor wrapped;

        private final LazyOptional<IInhabitor> cap_holder;

        public InhabitorProvider(final IInhabitor toWrap)
        {
            this.wrapped = toWrap;
            this.cap_holder = LazyOptional.of(() -> this.wrapped);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityInhabitor.CAPABILITY.orEmpty(cap, this.cap_holder);
        }
    }

    public static class NotInhabitor implements IInhabitor
    {

        @Override
        public GlobalPos getHome()
        {
            return null;
        }

        @Override
        public void onExitHabitat()
        {
        }

        @Override
        public GlobalPos getWorkSite()
        {
            return null;
        }

        @Override
        public void setWorldSite(final GlobalPos site)
        {
        }
    }
}
