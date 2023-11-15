package pokecube.gimmicks.nests.tasks.bees;

import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.api.PokecubeAPI;
import pokecube.api.events.pokemobs.ai.HarvestCheckEvent;
import pokecube.gimmicks.nests.tasks.bees.sensors.FlowerSensor;

public class BeeEventsHandler
{

    public static void init()
    {
        PokecubeAPI.POKEMOB_BUS.addListener(BeeEventsHandler::onBeeGatherBlocks);
    }

    /**
     * Here we check if the harvesting block to test is a flower, we
     * specifically prevent those here, as we polinate them instead!
     */
    private static void onBeeGatherBlocks(final HarvestCheckEvent event)
    {
        if (!BeeTasks.isValid(event.getEntity())) return;
        if (FlowerSensor.flowerPredicate.test(event.state)) event.setResult(Result.DENY);
    }
}
