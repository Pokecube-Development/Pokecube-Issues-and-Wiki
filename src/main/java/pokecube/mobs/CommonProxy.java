package pokecube.mobs;

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
}
