package pokecube.mobs;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.core.items.megastuff.ItemMegawearable;
import thut.core.common.Proxy;

public class CommonProxy implements Proxy
{
    public void initWearables()
    {
        ItemMegawearable.registerWearable("tiara", "HAT");
        ItemMegawearable.registerWearable("ankletzinnia", "ANKLE");
        ItemMegawearable.registerWearable("pendant", "NECK");
        ItemMegawearable.registerWearable("earring", "EAR");
        ItemMegawearable.registerWearable("glasses", "EYE");
    }

    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
        MinecraftForge.EVENT_BUS.register(MiscItemHelper.class);
    }
}
