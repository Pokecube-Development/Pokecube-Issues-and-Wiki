package pokecube.adventures.proxy;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.NewRegistryEvent;
import pokecube.adventures.PokecubeAdv;
import thut.core.common.Proxy;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.ThutWearables;

public class CommonProxy implements Proxy
{
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeAdv.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void onStart(final NewRegistryEvent event)
        {
            if (PokecubeAdv.proxy == null) PokecubeAdv.proxy = new CommonProxy();
        }
    }

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

    public Wearable getWearable()
    {
        return new Wearable();
    }
}
