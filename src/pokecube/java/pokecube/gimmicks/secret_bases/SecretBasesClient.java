package pokecube.gimmicks.secret_bases;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.watch.SecretBaseRadarPage;

@EventBusSubscriber(modid = PokecubeCore.MODID, value = Dist.CLIENT)
public class SecretBasesClient
{
    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event)
    {
        SecretBaseRadarPage.RADAR_MODES.put("_bases_",
                SecretBaseRadarPage.DEFAULT = new SecretBaseRadarPage.RadarMode("base", "_bases_", 1));
    }
}
