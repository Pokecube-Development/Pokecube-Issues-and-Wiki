package pokecube.world.gen.carver;

import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import pokecube.world.PokecubeWorld;

public class PokecubeCarvers
{
    public static final RegistryObject<WorldCarver<?>> CAVE;
    public static final RegistryObject<WorldCarver<?>> CANYON;
    public static final RegistryObject<WorldCarver<?>> OCEAN_CAVE;

    static
    {
        OCEAN_CAVE = PokecubeWorld.CARVERS.register("ocean_cave", () -> new CaveCarver(CaveCarverConfiguration.CODEC));
        CANYON = PokecubeWorld.CARVERS.register("canyon", () -> new CanyonCarver(CanyonCarverConfiguration.CODEC));
        CAVE = PokecubeWorld.CARVERS.register("cave", () -> new CaveCarver(CaveCarverConfiguration.CODEC));
    }

    public static void init(final IEventBus bus)
    {}
}
