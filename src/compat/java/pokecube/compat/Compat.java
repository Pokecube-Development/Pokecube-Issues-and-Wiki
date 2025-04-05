package pokecube.compat;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;

public class Compat
{
    public static IEventBus BUS = BusBuilder.builder().build();
}
