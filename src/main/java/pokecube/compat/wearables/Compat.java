package pokecube.compat.wearables;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent
    public static void register(final CompatEvent event)
    {
        if (ModList.get().isLoaded("thut_wearables")) Impl.register();
    }

}
