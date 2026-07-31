package pokecube.legends.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import pokecube.legends.Reference;
import pokecube.legends.spawns.WormholeSpawns;

@EventBusSubscriber(modid = Reference.ID)
public class SetupHandler
{
    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event)
    {
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
    }
}
