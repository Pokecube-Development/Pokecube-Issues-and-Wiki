package pokecube.core.world.gen;

import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.world.gen.carver.CanyonCarver;
import pokecube.core.world.gen.carver.CaveCarver;

public class WorldgenFeatures
{
    public static final DeferredRegister<WorldCarver<?>> REG = DeferredRegister.create(ForgeRegistries.WORLD_CARVERS,
            PokecubeCore.MODID);

    public static final RegistryObject<WorldCarver<?>> CAVE   = WorldgenFeatures.REG.register("cave",
            () -> new CaveCarver(ProbabilityConfig.CODEC, 256));
    public static final RegistryObject<WorldCarver<?>> CANYON = WorldgenFeatures.REG.register("canyon",
            () -> new CanyonCarver(ProbabilityConfig.CODEC));

    public static void init(final IEventBus bus)
    {
        WorldgenFeatures.REG.register(bus);
    }
}
