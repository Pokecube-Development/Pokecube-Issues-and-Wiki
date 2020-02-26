package pokecube.compat.minecolonies;

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
        if (ModList.get().isLoaded("minecolonies")) Impl.register();
    }
}
