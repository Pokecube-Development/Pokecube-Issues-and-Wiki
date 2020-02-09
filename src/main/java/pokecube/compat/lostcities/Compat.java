package pokecube.compat.lostcities;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

@Mod.EventBusSubscriber
public class Compat
{
    @SubscribeEvent
    public static void serverAboutToStart(final FMLServerAboutToStartEvent event)
    {
        if (ModList.get().isLoaded("lostcities")) Impl.register();
    }

}
