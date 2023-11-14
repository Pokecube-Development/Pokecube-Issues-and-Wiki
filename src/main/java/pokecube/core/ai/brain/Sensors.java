package pokecube.core.ai.brain;

import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.InterestingMobs;
import pokecube.core.ai.brain.sensors.NearBlocks;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;
import pokecube.gimmicks.nests.tasks.burrows.BurrowTasks;

public class Sensors
{
    public static final RegistryObject<SensorType<NearBlocks>> VISIBLE_BLOCKS;
    public static final RegistryObject<SensorType<InterestingMobs>> INTERESTING_ENTITIES;

    static
    {
        VISIBLE_BLOCKS = PokecubeCore.SENSORS.register("visible_blocks", () -> new SensorType<>(NearBlocks::new));
        INTERESTING_ENTITIES = PokecubeCore.SENSORS.register("interesting_mobs",
                () -> new SensorType<>(InterestingMobs::new));
    }

    public static void init()
    {
        BeeTasks.init();
        AntTasks.init();
        BurrowTasks.init();
    }
}
