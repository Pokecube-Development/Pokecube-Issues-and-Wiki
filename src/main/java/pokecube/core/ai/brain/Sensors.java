package pokecube.core.ai.brain;

import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.InterestingMobs;
import pokecube.core.ai.brain.sensors.NearBlocks;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.bees.BeeTasks;

public class Sensors
{
    public static final SensorType<NearBlocks> VISIBLE_BLOCKS = new SensorType<>(NearBlocks::new);

    public static final SensorType<InterestingMobs> INTERESTING_ENTITIES = new SensorType<>(InterestingMobs::new);

    public static void register(final Register<SensorType<?>> event)
    {
        event.getRegistry().register(Sensors.VISIBLE_BLOCKS.setRegistryName(PokecubeCore.MODID, "visible_blocks"));
        event.getRegistry().register(Sensors.INTERESTING_ENTITIES.setRegistryName(PokecubeCore.MODID,
                "interesting_mobs"));
        BeeTasks.registerSensors(event);
        AntTasks.registerSensors(event);
    }

}
