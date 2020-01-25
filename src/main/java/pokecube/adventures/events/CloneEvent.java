package pokecube.adventures.events;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.adventures.blocks.genetics.cloner.ClonerTile;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

@Cancelable
public class CloneEvent<T extends BaseGeneticsTile> extends Event
{
    public static class Pick extends CloneEvent<ClonerTile>
    {
        PokedexEntry entry;

        public Pick(final ClonerTile tile, final PokedexEntry entry)
        {
            super(tile);
            this.setEntry(entry);
        }

        public PokedexEntry getEntry()
        {
            return this.entry;
        }

        public void setEntry(final PokedexEntry entry)
        {
            this.entry = entry;
        }

    }

    public static class Spawn extends CloneEvent<ClonerTile>
    {
        IPokemob pokemob;

        public Spawn(final ClonerTile tile, final IPokemob pokemob)
        {
            super(tile);
            this.setPokemob(pokemob);
        }

        public IPokemob getPokemob()
        {
            return this.pokemob;
        }

        public void setPokemob(final IPokemob pokemob)
        {
            this.pokemob = pokemob;
        }
    }

    public final T tile;

    public CloneEvent(final T tile)
    {
        this.tile = tile;
    }
}
