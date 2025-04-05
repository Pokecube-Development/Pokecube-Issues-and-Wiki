package pokecube.core.ai.brain;

import java.util.function.Supplier;

import net.minecraft.world.entity.ai.sensing.SensorType;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.InterestingMobs;
import pokecube.core.ai.brain.sensors.NearBlocks;

public class Sensors
{
    public static final Supplier<SensorType<NearBlocks>> VISIBLE_BLOCKS;
    public static final Supplier<SensorType<InterestingMobs>> INTERESTING_ENTITIES;

    static
    {
        VISIBLE_BLOCKS = PokecubeCore.SENSORS.register("visible_blocks", () -> new SensorType<>(NearBlocks::new));
        INTERESTING_ENTITIES = PokecubeCore.SENSORS.register("interesting_mobs",
                () -> new SensorType<>(InterestingMobs::new));
    }

    public static void init()
    {
        // NO-OP, this is here to ensure the above static block is called.
    }
}
