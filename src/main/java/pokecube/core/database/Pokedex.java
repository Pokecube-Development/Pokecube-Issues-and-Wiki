/**
 *
 */
package pokecube.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import pokecube.core.PokecubeCore;

/** @author Manchou */
public class Pokedex
{
    private static Pokedex instance;

    public static Pokedex getInstance()
    {
        if (Pokedex.instance == null) Pokedex.instance = new Pokedex();
        return Pokedex.instance;
    }

    private final ArrayList<PokedexEntry>    entries;
    private final Map<PokedexEntry, Integer> entryIndecies;
    private final HashSet<PokedexEntry>      registeredFormes;

    /**
     *
     */
    private Pokedex()
    {
        this.entries = Lists.newArrayList();
        this.entryIndecies = Maps.newHashMap();
        this.registeredFormes = Sets.newHashSet();
    }

    public List<PokedexEntry> getEntries()
    {
        return this.entries;
    }

    public PokedexEntry getEntry(Integer pokedexNb)
    {
        final PokedexEntry ret = Database.getEntry(pokedexNb);
        if (ret == null) return ret;
        return ret.getBaseForme() != null ? ret.getBaseForme() : ret;
    }

    public PokedexEntry getFirstEntry()
    {
        if (this.entries.isEmpty()) return Database.missingno;
        if (this.entries.get(0) == Database.missingno && this.entries.size() > 1) return this.entries.get(1);
        return this.entries.get(0);
    }

    public Integer getIndex(PokedexEntry entry)
    {
        final Integer ret = this.entryIndecies.get(entry);
        return ret == null ? 0 : ret;
    }

    public PokedexEntry getLastEntry()
    {
        if (this.entries.isEmpty()) return this.getFirstEntry();
        return this.entries.get(this.entries.size() - 1);
    }

    public PokedexEntry getNext(PokedexEntry pokedexEntry, int i)
    {
        if (!pokedexEntry.base) pokedexEntry = pokedexEntry.getBaseForme();
        Integer index = this.entryIndecies.get(pokedexEntry);
        if (index == null)
        {
            PokecubeCore.LOGGER.error("Attempt to get a non existant entry: " + pokedexEntry,
                    new NullPointerException());
            return this.getFirstEntry();
        }
        while (index + i < 0)
            i += this.entries.size();
        index = (index + i) % this.entries.size();
        if (this.entries.get(index) == Database.missingno)
        {
            i = (int) Math.signum(i);
            while (index + i < 0)
                i += this.entries.size();
            index = (index + i) % this.entries.size();
        }
        return this.entries.get(index);
    }

    public PokedexEntry getPrevious(PokedexEntry pokedexEntry, int i)
    {
        return this.getNext(pokedexEntry, -i);
    }

    public Set<PokedexEntry> getRegisteredEntries()
    {
        return this.registeredFormes;
    }

    public boolean isRegistered(PokedexEntry entry)
    {
        return this.registeredFormes.contains(entry);
    }

    public void registerPokemon(PokedexEntry entry)
    {
        if (entry == null) return;
        if (!this.entries.contains(entry) && entry.base) this.entries.add(entry);
        this.registeredFormes.add(entry);
        this.resort();
    }

    private void resort()
    {
        Collections.sort(this.entries, Database.COMPARATOR);
        this.entryIndecies.clear();
        for (int i = 0; i < this.entries.size(); i++)
            this.entryIndecies.put(this.entries.get(i), i);
    }
}
