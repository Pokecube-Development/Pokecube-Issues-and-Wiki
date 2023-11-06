package pokecube.core.database.pokedex;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.SpawnData;
import pokecube.api.data.pokedex.DefaultFormeHolder;
import pokecube.api.data.pokedex.InteractsAndEvolutions.DyeInfo;
import pokecube.api.data.pokedex.InteractsAndEvolutions.Evolution;
import pokecube.api.data.pokedex.InteractsAndEvolutions.FormeItem;
import pokecube.api.data.pokedex.InteractsAndEvolutions.Interact;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader.IMergeable;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.legacy.RegistryChangeFixer;
import thut.api.entity.multipart.GenericPartEntity.BodyNode;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

/**
 * This class is the primary data structure used to load the pokedex entries
 * from json files, and to convert them back into json.
 *
 */
public class JsonPokedexEntry
        implements Consumer<PokedexEntry>, IMergeable<JsonPokedexEntry>, Comparable<JsonPokedexEntry>
{
    /**
     * Holder for the physical size of the pokemob, this is intended as the
     * outer extent of their hitboxes, and should be generally based on their
     * pokedex listed size.
     *
     */
    public static class Sizes implements Consumer<PokedexEntry>
    {
        float height;
        float width = -1;
        float length = -1;

        @Override
        public void accept(PokedexEntry t)
        {
            if (width == -1) width = height;
            if (length == -1) length = width;

            t.height = this.height;
            t.width = this.width;
            t.length = this.length;
        }
    }

    /**
     * Holder for stats for the pokemob, standard pokemon stats listings.
     *
     */
    public static class Stats implements Consumer<PokedexEntry>
    {
        int hp = 0;
        int attack = 0;
        int defense = 0;
        int special_attack = 0;
        int special_defense = 0;
        int speed = 0;

        @Override
        public void accept(PokedexEntry t)
        {
            int[] stats =
            { hp, attack, defense, special_attack, special_defense, speed };
            t.stats = stats;
        }

        public void set(int[] stats)
        {
            this.hp = stats[0];
            this.attack = stats[0];
            this.defense = stats[0];
            this.special_attack = stats[0];
            this.special_defense = stats[0];
            this.speed = stats[0];
        }
    }

    /**
     * Holder for EVs provided by the pokemob on death, same format as Stats.
     *
     */
    public static class EVs extends Stats
    {
        @Override
        public void accept(PokedexEntry t)
        {
            byte[] stats =
            { (byte) hp, (byte) attack, (byte) defense, (byte) special_attack, (byte) special_defense, (byte) speed };
            t.evs = stats;
        }

        public void set(byte[] stats)
        {
            this.hp = stats[0];
            this.attack = stats[0];
            this.defense = stats[0];
            this.special_attack = stats[0];
            this.special_defense = stats[0];
            this.speed = stats[0];
        }
    }

    /**
     * Abilities for the pokemon, split into lists for normal abilities and
     * hidden abilities. Normal abilities have higher probability of occuring
     * naturally.
     *
     */
    public static class Abilities implements Consumer<PokedexEntry>
    {
        public List<String> normal = new ArrayList<>();
        public List<String> hidden = new ArrayList<>();

        @Override
        public void accept(PokedexEntry t)
        {
            t.abilities.clear();
            t.abilities.addAll(normal);
            t.abilitiesHidden.clear();
            t.abilitiesHidden.addAll(hidden);
        }
    }

    /**
     * moves listings holder, this contains level up moves, which the mob learns
     * while levelling, and misc moves, which are all other moves the mob should
     * be able to know, via breeding, move tutor, tm, etc.
     *
     */
    public static class Moves implements Consumer<PokedexEntry>
    {
        public static class LevelMoves
        {
            public int L;
            public List<String> moves = new ArrayList<>();
        }

        public List<String> misc = new ArrayList<>();
        public List<LevelMoves> level_up = new ArrayList<>();

        @Override
        public void accept(PokedexEntry t)
        {
            Map<Integer, ArrayList<String>> lvlUpMoves = new HashMap<>();
            Set<String> allMoves = Sets.newHashSet();
            misc.forEach(s -> allMoves.add(Database.convertMoveName(s)));
            for (LevelMoves m : level_up)
            {
                ArrayList<String> forLevel = new ArrayList<>();
                m.moves.forEach(s -> {
                    allMoves.add(Database.convertMoveName(s));
                    forLevel.add(Database.convertMoveName(s));
                });
                lvlUpMoves.put(m.L, forLevel);
            }
            if (lvlUpMoves.isEmpty()) lvlUpMoves = null;
            ArrayList<String> _allMoves = new ArrayList<>(allMoves);
            if (_allMoves.isEmpty()) _allMoves = null;
            else _allMoves.sort(null);
            t.addMoves(_allMoves, lvlUpMoves);
        }
    }

    public static JsonPokedexEntry fromPokedexEntry(PokedexEntry e)
    {
        JsonPokedexEntry made = null;

        if (e._root_json != null)
        {
            made = e._root_json;
        }
        else
        {
            made = new JsonPokedexEntry();
            made.name = e.getTrimmedName();
            made.modid = e.getModId();
            made.stock = e.stock;
            made.id = e.pokedexNb;
            made.is_default = e.base;

            made.size = new Sizes();
            made.size.height = e.height;
            made.size.width = e.width;
            made.size.length = e.length;

            made.stats = new Stats();
            made.stats.set(e.stats);

            made.evs = new EVs();
            made.evs.set(e.evs);

            made.gender_rate = e.sexeRatio;
            made.capture_rate = e.catchRate;
            made.base_experience = e.baseXP;
            made.mass = (float) e.mass;
        }
        return made;
    }

    public static void loadFromJson(JsonPokedexEntry json)
    {
        var entry = json.toPokedexEntry();
        json.initStage2(entry);
    }

    public static String ENTIRE_DATABASE_CACHE = "";

    public static List<ResourceLocation> _compound_files = new ArrayList<>();

    public boolean replace = false;
    public boolean remove = false;

    public int priority = 100;

    public String name;
    public int id = 0;
    public boolean stock = true;
    public int base_experience = -1;
    public Sizes size = null;
    public float mass = -1;
    public boolean is_default = false;
    public boolean is_extra_form = false;
    public int capture_rate = -1;
    public int base_happiness = -1;
    public String growth_rate = null;
    public int gender_rate = -1;
    public Stats stats = null;
    public EVs evs = null;
    public List<String> types = null;
    public Abilities abilities = null;
    public Moves moves = null;
    public String old_name = null;

    public String loot_table = null;
    public String held_table = null;
    public String prey = null;

    public DyeInfo dye = null;

    public Boolean no_shiny = null;

    public String sound = null;

    public Map<String, JsonElement> pose_shapes = null;
    public List<JsonElement> ridden_offsets = null;

    public List<FormeItem> forme_items = null;

    // Options related to custom models
    public String model_path = null;
    public String tex_path = null;
    public String anim_path = null;
    public String modid = "pokecube_mobs";

    public DefaultFormeHolder model = null;
    public DefaultFormeHolder male_model = null;
    public DefaultFormeHolder female_model = null;

    public List<DefaultFormeHolder> models = null;

    public String base_form = null;

    public List<SpawnRule> spawn_rules = null;
    public List<Interact> interactions = null;

    // Evolution stuff
    public List<Evolution> evolutions = null;

    public List<ResourceLocation> __loaded_from = new ArrayList<>();

    /**
     * Blank constructor for json factory.
     */
    public JsonPokedexEntry()
    {}

    public PokedexEntry toPokedexEntry()
    {
        if (remove) return Database.missingno;
        PokedexEntry old = Database.getEntry(this.name);
        if (old != null && old != Database.missingno && !registered)
        {
            PokecubeAPI.LOGGER.warn("Duplicate entry for {}", this.name);
        }
        PokedexEntry entry = old == null ? new PokedexEntry(id, name, this.is_extra_form) : old;
        entry._root_json = this;
        entry.stock = this.stock;
        entry.base = this.is_default;
        // We may have overriden this for the update, so set it again anyway.
        entry.generated = this.is_extra_form;
        if (this.old_name != null) RegistryChangeFixer.registerRename(this.old_name, name);
        if (entry.base && !registered)
        {
            Database.baseFormes.put(id, entry);
            Database.addEntry(entry);
        }
        this.accept(entry);
        return entry;
    }

    @Override
    public int compareTo(JsonPokedexEntry o)
    {
        if (o.id != this.id) return Integer.compare(id, o.id);
        if (o.is_default != this.is_default) return this.is_default ? 1 : -1;
        return Integer.compare(priority, o.priority);
    }

    @Override
    public JsonPokedexEntry mergeFrom(JsonPokedexEntry other)
    {
        if (this.remove) return this;
        if (other.replace || other.remove)
        {
            return other;
        }
        for (var l : other.__loaded_from) if (!this.__loaded_from.contains(l)) this.__loaded_from.add(l);
        for (var l : this.__loaded_from) if (!other.__loaded_from.contains(l)) other.__loaded_from.add(l);
        mergeBasic(other);
        return this;
    }

    @Override
    public void accept(PokedexEntry entry)
    {
        if (this.remove)
        {
            entry.dummy = true;
            return;
        }
        if (this.base_experience != -1) entry.baseXP = this.base_experience;
        if (this.mass != -1) entry.mass = this.mass;
        if (this.capture_rate != -1) entry.catchRate = this.capture_rate;
        if (this.base_happiness != -1) entry.baseHappiness = this.base_happiness;
        if (this.gender_rate != -1) entry.sexeRatio = this.gender_rate;
        if (this.growth_rate != null) entry.evolutionMode = Tools.getType(this.growth_rate);
        if (this.loot_table != null) entry.lootTable = new ResourceLocation(loot_table);
        if (this.held_table != null) entry.heldTable = new ResourceLocation(held_table);
        if (this.types != null)
        {
            entry.type1 = null;
            entry.type2 = null;
            if (types.size() > 0) entry.type1 = PokeType.getType(types.get(0));
            if (types.size() > 1) entry.type2 = PokeType.getType(types.get(1));

            if (entry.type1 == null) entry.type1 = PokeType.unknown;
            if (entry.type2 == null) entry.type2 = PokeType.unknown;
        }
        if (this.forme_items != null) entry._forme_items = this.forme_items;
    }

    private void initStage2(PokedexEntry entry)
    {
        // This can be the case if the entry was removed earlier.
        if (entry == null) return;

        if (this.interactions != null) entry.addInteractions(this.interactions);

        if (this.no_shiny != null) entry.hasShiny = !this.no_shiny;

        if (this.size != null) this.size.accept(entry);
        if (this.moves != null) this.moves.accept(entry);
        if (this.stats != null) this.stats.accept(entry);
        if (this.evs != null) this.evs.accept(entry);
        if (this.abilities != null) this.abilities.accept(entry);

        if (this.model != null) entry._default_holder = this.model;

        if (this.sound != null) entry.customSound = this.sound;
        if (this.prey != null) entry.food = this.prey.trim().split(" ");

        if (this.dye != null) this.dye.accept(entry);
        entry.setModId(this.modid);
        if (this.model_path != null && this.tex_path != null)
        {
            final String tex = this.tex_path;
            final String model = this.model_path;
            String anim = this.anim_path;
            if (anim == null) anim = model;
            entry.texturePath = tex;
            entry.modelPath = model;

            entry.model = new ResourceLocation(this.modid, model + entry.getTrimmedName() + entry.modelExt);
            entry.texture = new ResourceLocation(this.modid, tex + entry.getTrimmedName() + ".png");
            entry.animation = new ResourceLocation(this.modid, anim + entry.getTrimmedName() + ".xml");

            if (PokecubeCore.getConfig().debug_data)
                PokecubeAPI.logInfo("Paths for {}: {} {} {}", entry.model, entry.texture, entry.animation);
        }

        if (this.pose_shapes != null && !this.pose_shapes.isEmpty())
        {
            entry.poseShapes = Maps.newHashMap();
            this.pose_shapes.forEach((s, n) -> {
                try
                {
                    BodyNode b = JsonUtil.gson.fromJson(n, BodyNode.class);
                    b.onLoad();
                    entry.poseShapes.put(s, b);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
        if (this.ridden_offsets != null && !this.ridden_offsets.isEmpty())
        {
            entry.passengerOffsets = new double[this.ridden_offsets.size()][3];
            int i = 0;
            for (var obj : this.ridden_offsets)
            {
                if (obj.isJsonArray())
                {
                    double x = 0, y = 0, z = 0;
                    try
                    {
                        var arr = obj.getAsJsonArray();

                        if (arr.size() == 1)
                        {
                            y = arr.get(0).getAsDouble();
                        }
                        else if (arr.size() == 3)
                        {
                            x = arr.get(0).getAsDouble();
                            y = arr.get(1).getAsDouble();
                            z = arr.get(2).getAsDouble();
                        }
                        else throw new IllegalArgumentException("Needs 1 or 3 entries for a ridden_offset!");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    entry.passengerOffsets[i][0] = x;
                    entry.passengerOffsets[i][1] = z;
                    entry.passengerOffsets[i][2] = y;
                }
                i++;
            }
        }

        if (this.base_form != null)
        {
            final PokedexEntry base = Database.getEntry(this.base_form);
            if (base == null) PokecubeAPI.LOGGER.error("Error with base form {} for {}", this.base_form, entry);
            else entry.setBaseForme(base);
        }
        if (this.model != null) PokedexEntryLoader.initFormeModel(entry, model);
        if (this.female_model != null) PokedexEntryLoader.initFormeModel(entry, female_model);
        if (this.male_model != null) PokedexEntryLoader.initFormeModel(entry, male_model);
        if (this.models != null) PokedexEntryLoader.initFormeModels(entry, this.models);

        // If it had gendered models, mark them accordingly so they get updated
        entry.setGenderedForm(male_model, IPokemob.MALE);
        entry.setGenderedForm(female_model, IPokemob.FEMALE);
    }

    public void handleSpawns(PokedexEntry entry)
    {
        if (this.spawn_rules == null) return;
        final boolean overwrite = this.replace;
        if (overwrite) entry.setSpawnData(new SpawnData(entry));
        for (final SpawnRule rule : this.spawn_rules)
        {
            final SpawnBiomeMatcher matcher = SpawnBiomeMatcher.get(rule);
            PokedexEntryLoader.handleAddSpawn(entry, matcher);
            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Handling Spawns for {}", entry);
        }
    }

    private static boolean registered = false;

    public static void loadPokedex()
    {
        loadPokedex(l -> true, true);
    }

    public static void loadPokedex(Predicate<ResourceLocation> valid, boolean updateCache)
    {
        final String path = "database/pokemobs/pokedex_entries/";
        final Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources(path);
        Map<String, List<JsonPokedexEntry>> toLoad = Maps.newHashMap();
        if (updateCache) _compound_files.clear();
        var oldLoaded = LOADED;
        if (updateCache) LOADED = new ArrayList<>();
        resources.forEach((l, r) -> {
            try
            {
                if (!valid.test(l)) return;
                List<JsonPokedexEntry> entries = loadDatabase(ResourceHelper.getStream(r), l);
                entries.forEach(entry -> {
                    toLoad.compute(entry.name, (key, list) -> {
                        var ret = list;
                        if (ret == null) ret = Lists.newArrayList();
                        ret.add(entry);
                        return ret;
                    });
                });
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error("Error with pokemob file {}", l, e);
            }
        });

        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Loaded {} Pokemobs", toLoad.size());

        List<JsonPokedexEntry> loaded = LOADED;
        loaded.clear();

        toLoad.forEach((k, v) -> {
            v.sort(null);
            JsonPokedexEntry e = null;
            for (JsonPokedexEntry e1 : v)
            {
                if (e == null) e = e1;
                else
                {
                    if (e.compareTo(e1) > 0)
                    {
                        e = e.mergeFrom(e1);
                    }
                    else
                    {
                        e = e1.mergeFrom(e);
                    }
                }
            }
            loaded.add(e);
        });
        loaded.sort(null);

        // Now we init the cache for telling clients about it
        if (updateCache) ENTIRE_DATABASE_CACHE = JsonUtil.smol_gson.toJson(loaded);

        // Stage 1, create the pokedex entries
        for (var load : loaded) load.toPokedexEntry();
        // Stage 2 initialise them
        for (var load : loaded) load.initStage2(Database.getEntry(load.name));

        if (updateCache) registered = true;
        if (updateCache) LOADED = oldLoaded;
    }

    public static List<JsonPokedexEntry> LOADED = Lists.newArrayList();

    public static void populateFromArray(JsonArray array, List<JsonPokedexEntry> list, ResourceLocation source)
    {
        JsonPokedexEntry database = null;
        int priorities = Integer.MIN_VALUE;
        int start_i = 0;
        var firstEntry = array.get(0).getAsJsonObject();
        if (firstEntry.has("priority"))
        {
            priorities = firstEntry.get("priority").getAsInt();
            if (!firstEntry.has("name")) start_i = 1;
        }
        for (int i = start_i; i < array.size(); i++)
        {
            var json = array.get(i);

            try
            {
                database = JsonUtil.gson.fromJson(json, JsonPokedexEntry.class);
                if (priorities != Integer.MIN_VALUE) database.priority = priorities;
                database.name = ThutCore.trim(database.name);
                list.add(database);
            }
            catch (JsonSyntaxException e)
            {
                PokecubeAPI.LOGGER.error("Error with pokemob entry in file {}, {}", source, json, e);
            }
        }
    }

    private static List<JsonPokedexEntry> loadDatabase(final InputStream stream, ResourceLocation source)
            throws Exception
    {
        ArrayList<JsonPokedexEntry> list = new ArrayList<>();
        final InputStreamReader reader = new InputStreamReader(stream);
        JsonElement json = JsonUtil.gson.fromJson(reader, JsonElement.class);
        reader.close();
        if (json.isJsonArray())
        {
            if (!_compound_files.contains(source)) _compound_files.add(source);
            var array = json.getAsJsonArray();
            populateFromArray(array, list, source);
        }
        else
        {
            JsonPokedexEntry entry = null;
            entry = JsonUtil.gson.fromJson(json, JsonPokedexEntry.class);
            entry.__loaded_from.add(source);
            list.add(entry);
        }
        return list;
    }
}
