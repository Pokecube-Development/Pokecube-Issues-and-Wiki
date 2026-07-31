package pokecube.gimmicks.pokeplayer;


import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import pokecube.core.PokecubeCore;

import static pokecube.core.init.CoreCreativeTabs.BLOCKS_ITEMS_TAB;
import static pokecube.core.init.CoreCreativeTabs.add;

@EventBusSubscriber(modid = PokecubeCore.MODID)
public class PokeplayerCreativeTabs
{

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTab().equals(BLOCKS_ITEMS_TAB.get()))
        {
            add(event, Pokeplayer.TRANSFORM_BLOCK);
        }

    }

}

