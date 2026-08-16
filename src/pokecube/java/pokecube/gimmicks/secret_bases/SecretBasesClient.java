package pokecube.gimmicks.secret_bases;

import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.watch.SecretBaseRadarPage;
import pokecube.gimmicks.secret_bases.dimension.SecretBaseDimension;

@EventBusSubscriber(modid = PokecubeCore.MODID, value = Dist.CLIENT)
public class SecretBasesClient
{
    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event)
    {
        SecretBaseRadarPage.RADAR_MODES.put("_bases_",
                SecretBaseRadarPage.DEFAULT = new SecretBaseRadarPage.RadarMode("base", "_bases_", 1));
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Pre event)
    {
        final Level world = PokecubeCore.proxy.getWorld();
        if (world == null) return;
        if (world.getWorldBorder().getSize() != SecretBaseDimension.WORLDSIZE
                && world.dimension().compareTo(SecretBaseDimension.WORLD_KEY) == 0)
            world.getWorldBorder().setSize(SecretBaseDimension.WORLDSIZE);
    }
}
