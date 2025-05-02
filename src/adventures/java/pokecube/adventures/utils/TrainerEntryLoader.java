package pokecube.adventures.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.tags.Tags;
import pokecube.core.entity.npc.NpcType;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;
import thut.wearables.EnumWearable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrainerEntryLoader
{
    public static class TrainerSpawn extends SpawnRule
    {
        public float rate = 1.0f;
    }

    public static class Worn
    {
        public String key;
        public List<JsonElement> options = Lists.newArrayList();
    }

    public static class TrainerEntry
    {
        String tradeTemplate = "default";
        String type;
        String pokemon;
        String gender;
        boolean belt = true;
        boolean holdReward = true;
        JsonElement held;
        Boolean replace;
        String team;

        List<TrainerSpawn> spawns = Lists.newArrayList();
        List<String> tags = Lists.newArrayList();
        List<Worn> worn = Lists.newArrayList();

        @Override
        public String toString()
        {
            return this.type + " " + this.spawns;
        }
    }

    public static class XMLDatabase
    {
        private final List<TrainerEntry> trainers = Lists.newArrayList();
        Map<String, TrainerEntry> _trainer_map = Maps.newHashMap();

        public void addEntry(final TrainerEntry entry)
        {
            final boolean replace = entry.replace != null && entry.replace;
            if (!this._trainer_map.containsKey(entry.type) || replace)
            {
                this.trainers.add(entry);
                this._trainer_map.put(entry.type, entry);
            }
        }
    }

    static XMLDatabase database;

    private static XMLDatabase loadDatabase()
    {
        final XMLDatabase full = new XMLDatabase();
        final Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources(NpcType.DATALOC);
        resources.forEach((file, resource) -> {
            JsonObject loaded;
            try
            {
                final BufferedReader reader = PackFinder.getReader(file);
                if (reader == null) throw new FileNotFoundException(file.toString());
                loaded = JsonUtil.gson.fromJson(reader, JsonObject.class);
                reader.close();
                if (loaded.has("trainers"))
                {
                    final XMLDatabase database = JsonUtil.gson.fromJson(loaded, XMLDatabase.class);
                    for (final TrainerEntry entry : database.trainers)
                    {
                        if (entry.type != null) entry.type = ThutCore.trim(entry.type);
                        else continue;
                        if (entry.pokemon != null) entry.pokemon = entry.pokemon.trim();
                        if (entry.gender != null) entry.gender = entry.gender.trim();
                        full.addEntry(entry);
                    }
                }
                // Try loading it directly as a trainer?
                else if (loaded.has("type"))
                {
                    var entry = JsonUtil.gson.fromJson(loaded, TrainerEntry.class);
                    entry.type = ThutCore.trim(entry.type);
                    if (entry.pokemon != null) entry.pokemon = entry.pokemon.trim();
                    if (entry.gender != null) entry.gender = entry.gender.trim();
                    full.addEntry(entry);
                }
            }
            catch (final Exception e)
            {
                PokecubeAPI.LOGGER.error("Error loading trainer database from {}", file, e);
            }
        });
        return full;
    }

    public static void makeEntries()
    {
        TrainerEntryLoader.database = TrainerEntryLoader.loadDatabase();
        for (final TrainerEntry entry : TrainerEntryLoader.database.trainers)
        {
            final String name = entry.type;
            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Loaded Type: " + name);
            final TypeTrainer type = TypeTrainer.getTrainer(name, true);
            type.spawns.clear();
            type.pokemon.clear();
            type.wornItems.clear();
            type.defaultTeam = entry.team;
            final byte male = 1;
            final byte female = 2;
            type.tradeTemplate = entry.tradeTemplate;
            if (entry.tags != null) entry.tags.forEach(s -> type.tags.add(ResourceLocation.parse(s)));

            for (Worn w : entry.worn)
            {
                var list = new ArrayList<ItemStack>();
                for (JsonElement d : w.options)
                {
                    ItemStack stack = Tools.getStack(d);
                    list.add(stack);
                }
                if (EnumWearable.slotsNames.containsKey(w.key)) type.wornItems.put(w.key, list);
                else PokecubeAPI.LOGGER.warn("Invalid key {} for worn items for {}", w.key, name);
            }
            if (entry.spawns != null) entry.spawns.forEach(rule -> {
                final SpawnBiomeMatcher matcher = SpawnBiomeMatcher.get(rule);
                type.spawns.put(matcher, rule.rate);
            });
            type.hasBelt = entry.belt;
            type.holdsReward = entry.holdReward;
            if (entry.gender != null) type.genders = entry.gender.equalsIgnoreCase("male")
                    ? male
                    : entry.gender.equalsIgnoreCase("female") ? female : male + female;
            if (entry.held != null)
            {
                type.held = Tools.getStack(entry.held);
            }
            if (entry.pokemon == null) type.pokelist = new String[] {};
            else
            {
                var arr = entry.pokemon.split(",");
                var list = new ArrayList<String>();
                for (var _entry : arr)
                {
                    if (_entry.startsWith("#"))
                    {
                        _entry = _entry.substring(1);
                        if (!_entry.contains(":")) _entry = "pokecube:" + _entry;
                        var tag = Tags.POKEMOB.getValues(_entry);
                        tag.forEach(v -> list.add(v.name));
                    }
                    else list.add(_entry);
                }
                type.pokelist = list.toArray(new String[0]);
            }
        }
    }
}
