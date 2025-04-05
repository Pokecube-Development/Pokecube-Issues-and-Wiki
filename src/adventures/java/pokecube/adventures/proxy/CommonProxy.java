package pokecube.adventures.proxy;

import net.minecraft.world.item.ItemStack;
import thut.core.common.Proxy;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;

public class CommonProxy implements Proxy
{
    protected static class Wearable extends IActiveWearable.Default
    {

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
    }

    public Wearable getWearable()
    {
        return new Wearable();
    }
}
