package pokecube.compat.wearables;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import pokecube.api.events.init.CompatEvent;
import pokecube.compat.wearables.sided.Common;

@EventBusSubscriber
public class Compat
{
    static
    {
        pokecube.compat.Compat.BUS.register(Compat.class);
    }

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent
    public static void registerRender(final CompatEvent event)
    {
        if (ModList.get().isLoaded("thut_wearables")) Impl.register();
    }

    @SubscribeEvent
    public static void registerCommon(final CompatEvent event)
    {
        Common.registerWearable();
    }

}
