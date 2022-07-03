package pokecube.mobs.proxy;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.NewRegistryEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.events.onload.RegisterMiscItems;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.mobs.PokecubeMobs;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeMobs.MODID)
public class CommonProxy
{
    @SubscribeEvent
    public static void onStart(final NewRegistryEvent event)
    {
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.LOW, CommonProxy::initWearables);
    }

    public static void initWearables(final RegisterMiscItems event)
    {
        ItemMegawearable.registerWearable("tiara", "HAT");
        ItemMegawearable.registerWearable("ankletzinnia", "ANKLE");
        ItemMegawearable.registerWearable("pendant", "NECK");
        ItemMegawearable.registerWearable("earring", "EAR");
        ItemMegawearable.registerWearable("glasses", "EYE");
    }
}
