package pokecube.mobs.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.core.database.Database;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.mobs.MiscItemHelper;
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
        TypeTrainer.merchant.pokemon.add(Database.getEntry("rattata"));
        TypeTrainer.merchant.overrideLevel = 50;
    }
}
