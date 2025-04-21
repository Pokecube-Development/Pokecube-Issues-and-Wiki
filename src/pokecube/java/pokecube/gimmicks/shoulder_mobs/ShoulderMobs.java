package pokecube.gimmicks.shoulder_mobs;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.events.pokemobs.InitAIEvent;
import pokecube.core.PokecubeCore;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
public class ShoulderMobs
{
    public static final AIRoutine SHOULDER = new AIRoutine("SHOULDER", true, p -> p.getPokedexEntry().canSitShoulder);

    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        PokecubeAPI.POKEMOB_BUS.addListener(ShoulderMobs::addAI);
    }

    private static void addAI(InitAIEvent.Init event)
    {
        if (event.type == InitAIEvent.Init.Type.IDLE)
        {
            var pokemob = event.getPokemob();
            // Jump on shoulder if able to
            if (pokemob.getPokedexEntry().canSitShoulder)
                event.add(new IdleJumpOnShoulderTask(pokemob).setPriority(15));
        }
    }
}
