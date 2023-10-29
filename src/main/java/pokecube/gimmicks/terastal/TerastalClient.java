package pokecube.gimmicks.terastal;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.mobs.overlays.Status;
import pokecube.core.client.render.mobs.overlays.Status.StatusOverlay;
import pokecube.core.client.render.mobs.overlays.Status.StatusTexturer;
import pokecube.core.utils.Resources;
import pokecube.gimmicks.terastal.TeraTypeGene.TeraType;

@Mod.EventBusSubscriber(bus = Bus.MOD, modid = PokecubeCore.MODID, value = Dist.CLIENT)
public class TerastalClient
{
    public static final StatusOverlay TERATEX = new StatusOverlay(new StatusTexturer(Resources.STATUS_TERA), 0.15f);

    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        TERATEX.texturer().rate = 0;
        TERATEX.texturer().animated = false;
        Status.PROVIDERS.add(pokemob -> {
            TeraType type = TerastalMechanic.getTera(pokemob.getEntity());
            if (type != null && type.isTera)
            {
                return TERATEX;
            }
            return null;
        });
    }
}
