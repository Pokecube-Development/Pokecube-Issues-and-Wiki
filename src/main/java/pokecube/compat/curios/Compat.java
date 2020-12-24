package pokecube.compat.curios;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pokecube.adventures.events.CompatEvent;

@Mod.EventBusSubscriber
public class Compat
{
    static
    {
        pokecube.compat.Compat.BUS.register(Compat.class);
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(Compat::onIMC);
    }

    @SubscribeEvent
    public static void register(final CompatEvent event)
    {
        if (ModList.get().isLoaded("curios")) Impl.register();
    }

    private static void onIMC(final InterModEnqueueEvent event)
    {
        if (ModList.get().isLoaded("curios")) Impl.onIMC(event);
    }
}
