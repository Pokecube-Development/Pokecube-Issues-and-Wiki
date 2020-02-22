package pokecube.core.events.pokemob;

import net.minecraftforge.eventbus.api.Cancelable;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.interfaces.IPokemob;

/**
 * Fired whenever the pokemob evolves. Fired on the PokecubeCore.POKEMOB_BUS
 *
 * @author Thutmose
 */
public class EvolveEvent extends LevelUpEvent
{
    /** Called after the evolution. */
    public static class Post extends EvolveEvent
    {
        /**
         * @param mob
         *            - the result of the evolution.
         */
        public Post(final IPokemob mob)
        {
            super(mob);
        }
    }

    /** Called before the evolution, if canceled, it will not evolve. */
    @Cancelable
    public static class Pre extends EvolveEvent
    {
        public PokedexEntry  forme;
        public EvolutionData evol_info;

        /**
         * @param mob
         *            - The mob doing the evolving.
         * @param evolvingTo
         *            - the mob to be evolved to.
         */
        public Pre(final IPokemob mob, final PokedexEntry evolvingTo, final EvolutionData evolInfo)
        {
            super(mob);
            this.forme = evolvingTo;
            this.evol_info = evolInfo;
        }
    }

    public EvolveEvent(final IPokemob mob)
    {
        super(mob, mob.getLevel(), mob.getLevel());
    }

}
