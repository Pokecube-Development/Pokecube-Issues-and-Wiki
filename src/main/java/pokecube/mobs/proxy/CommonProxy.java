package pokecube.mobs.proxy;

import pokecube.core.items.megastuff.ItemMegawearable;

public class CommonProxy
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
