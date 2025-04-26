package pokecube.api.events.pokemobs;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.api.entity.pokemob.IPokemob;

/**
 * Fired whenever the pokemob evolves. Fired on the PokecubeAPI.POKEMOB_BUS
 *
 * @author Thutmose
 */
public abstract class EvolveEvent extends Event implements ICancellableEvent
{

    /** Called after the evolution. */
    public static class Post extends EvolveEvent
    {
        /**
         * @param mob - the result of the evolution.
         */
        public Post(final IPokemob mob)
        {
            super(mob);
        }
    }

    /** Called before the evolution, if canceled, it will not evolve. */
    public static class Pre extends EvolveEvent
    {
        public PokedexEntry forme;
        public EvolutionData evol_info;

        /**
         * @param mob        - The mob doing the evolving.
         * @param evolvingTo - the mob to be evolved to.
         */
        public Pre(final IPokemob mob, final PokedexEntry evolvingTo, final EvolutionData evolInfo)
        {
            super(mob);
            this.forme = evolvingTo;
            this.evol_info = evolInfo;
        }
    }

    public final IPokemob mob;

    public EvolveEvent(final IPokemob mob)
    {
        this.mob = mob;
    }

}
