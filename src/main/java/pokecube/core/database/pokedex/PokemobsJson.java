package pokecube.core.database.pokedex;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader.XMLPokedexEntry;
import thut.core.common.ThutCore;

public class PokemobsJson implements Comparable<PokemobsJson>
{
    public Integer priority = 100;

    public boolean hotload = false;

    public boolean register = false;

    public ResourceLocation _file;

    public List<String> requiredMods = Lists.newArrayList();

    public List<XMLPokedexEntry> pokemon = Lists.newArrayList();

    public Map<String, XMLPokedexEntry> __map__ = Maps.newHashMap();

    public void addEntry(final XMLPokedexEntry toAdd)
    {
        toAdd.name = ThutCore.trim(toAdd.name);
        if (this.__map__.containsKey(toAdd.name)) this.pokemon.remove(this.__map__.remove(toAdd.name));
        this.__map__.put(toAdd.name, toAdd);
        this.pokemon.add(toAdd);
        this.pokemon.removeIf(value ->
        {
            if (value.number == null)
            {
                final PokedexEntry entry = Database.getEntry(value.name);
                if (entry != null)
                {
                    value.number = entry.getPokedexNb();
                    value.name = entry.getTrimmedName();
                    return false;
                }
                PokecubeAPI.LOGGER.error(
                        "Error with entry for {}, it is missing a Number for sorting! removing on add!", value.name);
                return true;
            }
            return false;
        });
        Collections.sort(this.pokemon, PokedexEntryLoader.ENTRYSORTER);
    }

    public void init()
    {
        for (final XMLPokedexEntry e : this.pokemon)
            this.__map__.put(e.name = ThutCore.trim(e.name), e);
    }

    @Override
    public int compareTo(final PokemobsJson o)
    {
        if (o.register && !this.register) return 1;
        if (this.register && !o.register) return -1;
        return this.priority.compareTo(o.priority);
    }
}
