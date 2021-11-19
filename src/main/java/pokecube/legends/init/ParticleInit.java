package pokecube.legends.init;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;

@Mod.EventBusSubscriber(modid = Reference.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleInit
{
    public static final RegistryObject<SimpleParticleType> INFECTED_FIRE_FLAME;

    static
    {
        INFECTED_FIRE_FLAME =
            PokecubeLegends.PARTICLES.register("infected_fire_flame", () -> new SimpleParticleType(false));
    }
}
