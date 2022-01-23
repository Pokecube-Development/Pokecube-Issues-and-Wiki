package pokecube.legends.init;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;

@Mod.EventBusSubscriber(modid = Reference.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleInit
{
    public static final RegistryObject<SimpleParticleType> INFECTED_FIRE_FLAME;
    public static final RegistryObject<SimpleParticleType> INFECTED_SMOKE;
    public static final RegistryObject<SimpleParticleType> INFECTED_SOUL;
    public static final RegistryObject<SimpleParticleType> MUSHROOM;

    static
    {
        INFECTED_FIRE_FLAME = PokecubeLegends.PARTICLES.register("infected_fire_flame", () -> new SimpleParticleType(false));

        INFECTED_SMOKE = PokecubeLegends.PARTICLES.register("infected_smoke", () -> new SimpleParticleType(false));

        INFECTED_SOUL = PokecubeLegends.PARTICLES.register("infected_soul", () -> new SimpleParticleType(false));

        MUSHROOM = PokecubeLegends.PARTICLES.register("mushroom", () -> new SimpleParticleType(false));
    }
}
