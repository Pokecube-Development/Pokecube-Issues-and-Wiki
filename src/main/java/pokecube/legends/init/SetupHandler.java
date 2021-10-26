package pokecube.legends.init;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.legends.Reference;
import pokecube.legends.spawns.WormholeSpawns;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID)
public class SetupHandler
{
    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event)
    {
        WormholeSpawns.registerCapabilities(event);
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        WormholeSpawns.init();

        // FIXME remove this when forge fixes fluids crash
        event.enqueueWork(() ->
        {
            FluidInit.finish();
        });
    }
}
