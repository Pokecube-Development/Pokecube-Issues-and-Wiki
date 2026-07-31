package pokecube.compat.wearables;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.compat.wearables.sided.Common;
import thut.wearables.ThutWearables;

@EventBusSubscriber(modid = ThutWearables.MODID)
public class Compat
{
    @SubscribeEvent
    public static void registerCommon(final FMLCommonSetupEvent event)
    {
        Common.registerWearable();
    }
}
