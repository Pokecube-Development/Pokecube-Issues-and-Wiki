package pokecube.gimmicks.mega;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.gimmicks.mega.MegaCapability.MegaStone;

@EventBusSubscriber(value = Dist.CLIENT, modid = PokecubeCore.MODID)
public class ClientHandler
{
    @SubscribeEvent
    public static void registerColours(final RegisterColorHandlersEvent.Item event)
    {
        event.register((stack, tintIndex) -> {
            MegaStone stone = stack.get(MegaEvolveHelper.MEGA_STONE);
            if (stone == null || stone.colours()==null) return 0xFF888888;
            return stone.colours()[tintIndex];
        }, PokecubeItems.getStack("megastone").getItem());
    }
}
