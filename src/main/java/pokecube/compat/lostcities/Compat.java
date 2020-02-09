package pokecube.compat.lostcities;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@Mod.EventBusSubscriber
public class Compat
{

    @SubscribeEvent
    public static void loadComplete(final FMLLoadCompleteEvent event)
    {
        if (ModList.get().isLoaded("lostcities")) Impl.register();
    }

}
