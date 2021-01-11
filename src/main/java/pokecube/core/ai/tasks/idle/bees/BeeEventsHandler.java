package pokecube.core.ai.tasks.idle.bees;

import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.idle.bees.sensors.FlowerSensor;
import pokecube.core.events.HarvestCheckEvent;

public class BeeEventsHandler
{

    public static void init()
    {
        PokecubeCore.POKEMOB_BUS.addListener(BeeEventsHandler::onBeeGatherBlocks);
    }

    /**
     * Here we check if the harvesting block to test is a flower, we
     * specifically prevent those here, as we polinate them instead!
     */
    private static void onBeeGatherBlocks(final HarvestCheckEvent event)
    {
        if (!BeeTasks.isValidBee(event.getEntity())) return;
        if (FlowerSensor.flowerPredicate.test(event.state)) event.setResult(Result.DENY);
    }
}
