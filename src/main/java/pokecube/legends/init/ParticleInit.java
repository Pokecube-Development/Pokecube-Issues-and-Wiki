package pokecube.legends.init;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;

@Mod.EventBusSubscriber(modid = Reference.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleInit
{
    public static final RegistryObject<SimpleParticleType> AGED_LEAF;
    public static final RegistryObject<SimpleParticleType> DYNA_LEAF_RED;
    public static final RegistryObject<SimpleParticleType> DYNA_LEAF_PINK;
    public static final RegistryObject<SimpleParticleType> DYNA_LEAF_PASTEL_PINK;
    public static final RegistryObject<SimpleParticleType> ERROR;
    public static final RegistryObject<SimpleParticleType> GOLD_STAR;
    public static final RegistryObject<SimpleParticleType> INFECTED_FIRE_FLAME;
    public static final RegistryObject<SimpleParticleType> INFECTED_SMOKE;
    public static final RegistryObject<SimpleParticleType> INFECTED_SOUL;
    public static final RegistryObject<SimpleParticleType> INFECTED_SPARK;
    public static final RegistryObject<SimpleParticleType> MUSHROOM;
    public static final RegistryObject<SimpleParticleType> MIRAGE_LEAF;

    static
    {
        AGED_LEAF = PokecubeLegends.PARTICLES.register("aged_leaf", () -> new SimpleParticleType(false));
        DYNA_LEAF_RED = PokecubeLegends.PARTICLES.register("dyna_leaf_red", () -> new SimpleParticleType(false));
        DYNA_LEAF_PINK = PokecubeLegends.PARTICLES.register("dyna_leaf_pink", () -> new SimpleParticleType(false));
        DYNA_LEAF_PASTEL_PINK = PokecubeLegends.PARTICLES.register("dyna_leaf_pastel_pink", () -> new SimpleParticleType(false));
        ERROR = PokecubeLegends.PARTICLES.register("error", () -> new SimpleParticleType(false));
        GOLD_STAR = PokecubeLegends.PARTICLES.register("gold_star", () -> new SimpleParticleType(false));
        INFECTED_FIRE_FLAME = PokecubeLegends.PARTICLES.register("infected_fire_flame", () -> new SimpleParticleType(false));
        INFECTED_SMOKE = PokecubeLegends.PARTICLES.register("infected_smoke", () -> new SimpleParticleType(false));
        INFECTED_SOUL = PokecubeLegends.PARTICLES.register("infected_soul", () -> new SimpleParticleType(false));
        INFECTED_SPARK = PokecubeLegends.PARTICLES.register("infected_spark", () -> new SimpleParticleType(false));
        MUSHROOM = PokecubeLegends.PARTICLES.register("mushroom", () -> new SimpleParticleType(false));
        MIRAGE_LEAF = PokecubeLegends.PARTICLES.register("mirage_leaf", () -> new SimpleParticleType(false));
    }
}
