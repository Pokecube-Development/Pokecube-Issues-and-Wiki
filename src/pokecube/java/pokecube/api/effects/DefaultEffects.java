package pokecube.api.effects;

public class DefaultEffects
{
    /**
     * This initialises the default keys, so they are fine server and client side. The client side mod will then replace
     * some of them with things that server can't do.
     */
    public static void init()
    {
        // This one is default set in EvolutionRays, via a call from ClientMod's constructor
        ParticleEffects.registerRecord("pokecube.pokemob.evolution");

        // These ones are default set in PokemobTickParticles, also via similar call
        ParticleEffects.registerRecord("pokecube.pokemob.holiday");
        ParticleEffects.registerRecord("pokecube.pokemob.shadow");
        ParticleEffects.registerRecord("pokecube.pokemob.mating");
        ParticleEffects.registerRecord("pokecube.pokemob.flavour");
    }
}
