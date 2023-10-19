/**
 *
 */
package pokecube.api.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import pokecube.api.PokecubeAPI;
import pokecube.core.database.Database;

/** @author Manchou */
public class Pokedex
{
    private static Pokedex instance;

    public static Pokedex getInstance()
    {
        if (Pokedex.instance == null) Pokedex.instance = new Pokedex();
        return Pokedex.instance;
    }

    private final ArrayList<PokedexEntry> entries;
    private final Map<PokedexEntry, Integer> entryIndecies;
    private final HashSet<PokedexEntry> registeredFormes;

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

    public PokedexEntry getFirstEntry()
    {
        if (this.entries.isEmpty()) return Database.missingno;
        if (this.entries.get(0) == Database.missingno && this.entries.size() > 1) return this.entries.get(1);
        return this.entries.get(0);
    }

    public Integer getIndex(final PokedexEntry entry)
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
            PokecubeAPI.LOGGER.error("Attempt to get a non existant entry: " + pokedexEntry,
                    new NullPointerException());
            return this.getFirstEntry();
        }
        while (index + i < 0) i += this.entries.size();
        index = (index + i) % this.entries.size();
        if (this.entries.get(index) == Database.missingno)
        {
            i = (int) Math.signum(i);
            while (index + i < 0) i += this.entries.size();
            index = (index + i) % this.entries.size();
        }
        PokedexEntry entry = this.entries.get(index);
        entry = getFirstForm(entry);
        return entry;
    }

    public PokedexEntry getPrevious(final PokedexEntry pokedexEntry, final int i)
    {
        return this.getNext(pokedexEntry, -i);
    }

    public PokedexEntry getFirstForm(PokedexEntry input)
    {
        PokedexEntry test = input;
        PokedexEntry prev = input;
        prev = getPreviousForm(test);
        List<PokedexEntry> formes = Lists.newArrayList(Database.getSortedFormes());
        int i_test = formes.indexOf(test);
        int i_prev = formes.indexOf(prev);
        while (i_prev < i_test)
        {
            test = prev;
            prev = getPreviousForm(test);
            i_test = formes.indexOf(test);
            i_prev = formes.indexOf(prev);
        }
        return test;
    }

    public PokedexEntry getNextForm(PokedexEntry entry)
    {
        List<PokedexEntry> formes = Lists.newArrayList(Database.getSortedFormes());
        int i_next = formes.indexOf(entry) + 1;
        i_next = i_next >= formes.size() ? 0 : i_next;
        PokedexEntry newEntry = formes.get(i_next);
        boolean skip = newEntry.default_holder != null && newEntry.default_holder._entry != newEntry;
        skip = skip || (newEntry.male_holder != null && newEntry.male_holder != newEntry.default_holder);
        if (skip)
        {
            i_next = formes.indexOf(newEntry) + 1;
            i_next = i_next >= formes.size() ? 0 : i_next;
            newEntry = formes.get(i_next);
        }
        if (newEntry.getPokedexNb() != entry.getPokedexNb()) newEntry = getFirstForm(entry);
        return newEntry;
    }

    public PokedexEntry getPreviousForm(PokedexEntry entry)
    {
        List<PokedexEntry> formes = Lists.newArrayList(Database.getSortedFormes());
        int i_next = formes.indexOf(entry) - 1;
        i_next = i_next < 0 ? formes.size() - 1 : i_next;
        PokedexEntry newEntry = formes.get(i_next);
        boolean skip = newEntry.default_holder != null && newEntry.default_holder._entry != newEntry;
        skip = skip || (newEntry.male_holder != null && newEntry.male_holder != newEntry.default_holder);
        if (skip)
        {
            i_next = formes.indexOf(newEntry) - 1;
            i_next = i_next < 0 ? formes.size() - 1 : i_next;
            newEntry = formes.get(i_next);
        }
        if (newEntry.getPokedexNb() != entry.getPokedexNb())
        {
            i_next = formes.indexOf(entry);
            for (int i = i_next; i < formes.size(); i++)
            {
                var e = formes.get(i);
                if (e.getPokedexNb() == entry.getPokedexNb())
                {
                    i_next = i;
                }
                else break;
            }
            newEntry = formes.get(i_next);
        }
        return newEntry;
    }

    public Set<PokedexEntry> getRegisteredEntries()
    {
        return this.registeredFormes;
    }

    public boolean isRegistered(final PokedexEntry entry)
    {
        return this.registeredFormes.contains(entry);
    }

    public void registerPokemon(final PokedexEntry entry)
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
        for (int i = 0; i < this.entries.size(); i++) this.entryIndecies.put(this.entries.get(i), i);
    }
}
