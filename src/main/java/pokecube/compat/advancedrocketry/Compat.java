package pokecube.compat.advancedrocketry;

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

    @SubscribeEvent
    public static void register(final CompatEvent event)
    {
        if (ModList.get().isLoaded("advancedrocketry")) Impl.register();
    }
}
