package pokecube.adventures.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;

public class TrainerEntryLoader
{
    public static class Bag extends Drop
    {
    }

    public static class Held extends Drop
    {
    }

    public static class TrainerEntry
    {
        String          tradeTemplate = "default";
        String          type;
        String          pokemon;
        List<SpawnRule> spawns        = Lists.newArrayList();
        String          gender;
        Bag             bag;
        boolean         belt          = true;
        Held            held;
        Held            reward;

        @Override
        public String toString()
        {
            return this.type + " " + this.spawns;
        }
    }

    public static class XMLDatabase
    {
        private final List<TrainerEntry> trainers = Lists.newArrayList();
    }

    static XMLDatabase database;

    private static XMLDatabase loadDatabase(final ResourceLocation file) throws Exception
    {
        final InputStream res = Database.resourceManager.getResource(file).getInputStream();
        final Reader reader = new InputStreamReader(res);
        final XMLDatabase database = PokedexEntryLoader.gson.fromJson(reader, XMLDatabase.class);
        for (final TrainerEntry entry : database.trainers)
        {
            if (entry.type != null) entry.type = entry.type.trim();
            if (entry.pokemon != null) entry.pokemon = entry.pokemon.trim();
            if (entry.gender != null) entry.gender = entry.gender.trim();
        }
        reader.close();
        return database;
    }

    public static void makeEntries(final ResourceLocation file)
    {
        if (TrainerEntryLoader.database == null) try
        {
            TrainerEntryLoader.database = TrainerEntryLoader.loadDatabase(file);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.warn(file + "", e);
            return;
        }
        else try
        {
            PokecubeCore.LOGGER.debug("Loading Database: " + file);
            final XMLDatabase newDatabase = TrainerEntryLoader.loadDatabase(file);
            for (final TrainerEntry entry : newDatabase.trainers)
            {
                for (final TrainerEntry old : TrainerEntryLoader.database.trainers)
                    if (old.type.equals(entry.type))
                    {
                        TrainerEntryLoader.database.trainers.remove(old);
                        break;
                    }
                TrainerEntryLoader.database.trainers.add(entry);
            }
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.warn(file + "", e);
            return;
        }
        for (final TrainerEntry entry : TrainerEntryLoader.database.trainers)
        {
            final String name = entry.type;
            PokecubeCore.LOGGER.debug("Loaded Type: " + name);
            TypeTrainer type = TypeTrainer.typeMap.get(name);
            if (type == null) type = new TypeTrainer(name);
            type.matchers.clear();
            type.pokemon.clear();
            final byte male = 1;
            final byte female = 2;
            type.tradeTemplate = entry.tradeTemplate;
            type.hasBag = entry.bag != null;
            if (type.hasBag)
            {
                final ItemStack bag = Tools.getStack(entry.bag.getValues());
                type.bag = bag;
            }
            if (entry.spawns != null) for (final SpawnRule rule : entry.spawns)
            {
                Float weight;
                try
                {
                    weight = Float.parseFloat(rule.values.get(new QName("rate")));
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.warn("Error with weight for " + type.getName() + " " + rule.values + " "
                            + entry.spawns, e);
                    continue;
                }
                final SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(rule);
                type.matchers.put(matcher, weight);
            }

            type.hasBelt = entry.belt;
            if (entry.gender != null) type.genders = entry.gender.equalsIgnoreCase("male") ? male
                    : entry.gender.equalsIgnoreCase("female") ? female : male + female;
            final String[] pokeList = entry.pokemon == null ? new String[] {} : entry.pokemon.split(",");
            if (entry.held != null)
            {
                final ItemStack held = Tools.getStack(entry.held.getValues());
                type.held = held;
            }
            if (pokeList.length == 0) continue;
            if (!pokeList[0].startsWith("-")) for (final String s : pokeList)
            {
                final PokedexEntry e = Database.getEntry(s);
                if (e != null && !type.pokemon.contains(e)) type.pokemon.add(e);
                else if (e == null)
                {
                    // System.err.println("Error in reading of "+s);
                }
            }
            else
            {
                final String[] types = pokeList[0].replace("-", "").split(":");
                if (types[0].equalsIgnoreCase("all"))
                {
                    for (final PokedexEntry s : Database.spawnables)
                        if (!s.legendary && s.getPokedexNb() != 151) type.pokemon.add(s);
                }
                else for (final String type2 : types)
                {
                    final PokeType pokeType = PokeType.getType(type2);
                    if (pokeType != PokeType.unknown) for (final PokedexEntry s : Database.spawnables)
                        if (s.isType(pokeType) && !s.legendary && s.getPokedexNb() != 151) type.pokemon.add(s);
                }
            }
        }
    }
}
