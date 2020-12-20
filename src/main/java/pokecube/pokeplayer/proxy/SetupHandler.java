package pokecube.pokeplayer.proxy;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.pokeplayer.Reference;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID)
public class SetupHandler
{
	@SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
    }
}
