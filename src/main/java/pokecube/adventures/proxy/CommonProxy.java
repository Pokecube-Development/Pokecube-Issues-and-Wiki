package pokecube.adventures.proxy;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import thut.core.common.Proxy;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.ThutWearables;

public class CommonProxy implements Proxy
{
    protected static class Wearable extends IActiveWearable.Default implements ICapabilityProvider
    {
        private final LazyOptional<IActiveWearable> holder = LazyOptional.of(() -> this);

        @Override
        public EnumWearable getSlot(final ItemStack stack)
        {
            return EnumWearable.BACK;
        }

        @Override
        public boolean dyeable(final ItemStack stack)
        {
            return true;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutWearables.WEARABLE_CAP.orEmpty(cap, this.holder);
        }
    }

    public ResourceLocation getTrainerSkin(final LivingEntity mob, final TypeTrainer type, final byte gender)
    {
        return null;
    }

    public Wearable getWearable()
    {
        return new Wearable();
    }
}
