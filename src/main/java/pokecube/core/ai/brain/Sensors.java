package pokecube.core.ai.brain;

import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.InterestingMobs;
import pokecube.core.ai.brain.sensors.NearBlocks;
import pokecube.core.ai.brain.sensors.NearItems;

public class Sensors
{
    public static final SensorType<NearBlocks>      VISIBLE_BLOCKS   = new SensorType<>(NearBlocks::new);
    public static final SensorType<NearItems>       VISIBLE_ITEMS    = new SensorType<>(NearItems::new);
    public static final SensorType<InterestingMobs> INTERESTING_MOBS = new SensorType<>(InterestingMobs::new);

    public static void register(final Register<SensorType<?>> event)
    {
        event.getRegistry().register(Sensors.VISIBLE_BLOCKS.setRegistryName(PokecubeCore.MODID, "visible_blocks"));
        event.getRegistry().register(Sensors.VISIBLE_ITEMS.setRegistryName(PokecubeCore.MODID, "visible_items"));
        event.getRegistry().register(Sensors.INTERESTING_MOBS.setRegistryName(PokecubeCore.MODID, "interesting_mobs"));
    }

}
