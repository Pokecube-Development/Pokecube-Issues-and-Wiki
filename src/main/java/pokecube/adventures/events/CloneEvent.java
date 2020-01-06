package pokecube.adventures.events;

import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.adventures.blocks.genetics.cloner.ClonerTile;
import pokecube.adventures.blocks.genetics.extractor.ExtractorTile;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.blocks.genetics.splicer.SplicerTile;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

@Cancelable
public class CloneEvent<T extends BaseGeneticsTile> extends Event
{
    public static class Extract extends CloneEvent<ExtractorTile>
    {
        ItemStack selector;
        ItemStack input;
        ItemStack bottle;
        ItemStack output;

        public Extract(final ExtractorTile tile)
        {
            super(tile);
        }

        public ItemStack getBottle()
        {
            return this.bottle;
        }

        public ItemStack getInput()
        {
            return this.input;
        }

        public ItemStack getOutput()
        {
            return this.output;
        }

        public ItemStack getSelector()
        {
            return this.selector;
        }

        public void setBottle(final ItemStack bottle)
        {
            this.bottle = bottle;
        }

        public void setInput(final ItemStack input)
        {
            this.input = input;
        }

        public void setOutput(final ItemStack output)
        {
            this.output = output;
        }

        public void setSelector(final ItemStack selector)
        {
            this.selector = selector;
        }
    }

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

    public static class Splice extends CloneEvent<SplicerTile>
    {
        ItemStack selector;
        ItemStack input;
        ItemStack injected;
        ItemStack output;

        public Splice(final SplicerTile tile)
        {
            super(tile);
        }

        public ItemStack getInjected()
        {
            return this.injected;
        }

        public ItemStack getInput()
        {
            return this.input;
        }

        public ItemStack getOutput()
        {
            return this.output;
        }

        public ItemStack getSelector()
        {
            return this.selector;
        }

        public void setInjected(final ItemStack injected)
        {
            this.injected = injected;
        }

        public void setInput(final ItemStack input)
        {
            this.input = input;
        }

        public void setOutput(final ItemStack output)
        {
            this.output = output;
        }

        public void setSelector(final ItemStack selector)
        {
            this.selector = selector;
        }

    }

    public final T tile;

    public CloneEvent(final T tile)
    {
        this.tile = tile;
    }
}
