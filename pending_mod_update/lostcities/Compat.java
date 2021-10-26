package pokecube.compat.lostcities;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import pokecube.adventures.events.CompatEvent;

@Mod.EventBusSubscriber
public class Compat
{
    static
    {
        pokecube.compat.Compat.BUS.register(Compat.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void loadComplete(final CompatEvent event)
    {
        if (ModList.get().isLoaded("lostcities")) Impl.register();
    }

}
