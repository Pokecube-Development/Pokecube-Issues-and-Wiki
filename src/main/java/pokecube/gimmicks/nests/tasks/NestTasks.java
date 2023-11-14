package pokecube.gimmicks.nests.tasks;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.NewRegistryEvent;
import pokecube.core.PokecubeCore;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;
import pokecube.gimmicks.nests.tasks.burrows.BurrowTasks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
public class NestTasks
{
    static
    {
        init();
    }

    @SubscribeEvent
    /**
     * Dummy event handler for ensuring this class has the static init called.
     * 
     * @param event
     */
    public static void dummyHandler(final NewRegistryEvent event)
    {

    }

    public static void init()
    {
        BeeTasks.init();
        AntTasks.init();
        BurrowTasks.init();
    }
}
