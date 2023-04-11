package pokecube.core.database.pokedex;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.SpawnData;
import pokecube.api.data.pokedex.DefaultFormeHolder;
import pokecube.api.data.pokedex.InteractsAndEvolutions.BaseMegaRule;
import pokecube.api.data.pokedex.InteractsAndEvolutions.DyeInfo;
import pokecube.api.data.pokedex.InteractsAndEvolutions.Evolution;
import pokecube.api.data.pokedex.InteractsAndEvolutions.FormeItem;
import pokecube.api.data.pokedex.InteractsAndEvolutions.Interact;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader.IMergeable;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.legacy.RegistryChangeFixer;
import thut.api.entity.multipart.GenericPartEntity.BodyNode;
import thut.api.util.JsonUtil;
import thut.lib.ResourceHelper;

public class JsonPokedexEntry
        implements Consumer<PokedexEntry>, IMergeable<JsonPokedexEntry>, Comparable<JsonPokedexEntry>
{
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
    }

    public static class EVs extends Stats
    {
        @Override
        public void accept(PokedexEntry t)
        {
            byte[] stats =
            { (byte) hp, (byte) attack, (byte) defense, (byte) special_attack, (byte) special_defense, (byte) speed };
            t.evs = stats;
        }
    }

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

    public Boolean mega = null;
    public Boolean gmax = null;
    public Boolean no_shiny = null;

    public String sound = null;

    public Map<String, BodyNode> pose_shapes = null;

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
    public List<BaseMegaRule> mega_rules = null;
    public List<Interact> interactions = null;

    // Evolution stuff
    public List<Evolution> evolutions = null;

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
        PokedexEntry entry = old == null ? new PokedexEntry(id, name) : old;
        entry._root_json = this;
        entry.stock = this.stock;
        entry.base = this.is_default;
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
        if (this.mega_rules != null) entry._loaded_megarules.addAll(this.mega_rules);

        if (this.mega != null) entry.setMega(this.mega);
        if (this.gmax != null) entry.setGMax(this.gmax);
        if (this.no_shiny != null) entry.hasShiny = !this.no_shiny;

        if (this.size != null) this.size.accept(entry);
        if (this.moves != null) this.moves.accept(entry);
        if (this.stats != null) this.stats.accept(entry);
        if (this.evs != null) this.evs.accept(entry);
        if (this.abilities != null) this.abilities.accept(entry);

        if (this.model != null) entry._default_holder = this.model;
        if (this.male_model != null) entry._male_holder = this.male_model;
        if (this.female_model != null) entry._female_holder = this.female_model;

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
            entry.poseShapes = null;
            this.pose_shapes.forEach((s, n) -> n.onLoad());
            entry.poseShapes = this.pose_shapes;
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
        PokedexEntryLoader.parseEvols(entry, this.evolutions, false);
    }

    public void postInit(PokedexEntry entry)
    {
        PokedexEntryLoader.parseEvols(entry, this.evolutions, true);
        this.handleSpawns(entry);
    }

    private void handleSpawns(PokedexEntry entry)
    {
        if (this.spawn_rules == null) return;
        final boolean overwrite = this.replace;
        if (overwrite) entry.setSpawnData(new SpawnData(entry));
        for (final SpawnRule rule : this.spawn_rules)
        {
            final FormeHolder holder = rule.getForme(entry);
            if (holder != null) Database.registerFormeHolder(entry, holder);
            final SpawnBiomeMatcher matcher = SpawnBiomeMatcher.get(rule);
            PokedexEntryLoader.handleAddSpawn(entry, matcher);
            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Handling Spawns for {}", entry);
        }
    }

    private static boolean registered = false;

    public static void loadPokedex()
    {
        final String path = "database/pokemobs/pokedex_entries/";
        final Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources(path);
        Map<String, List<JsonPokedexEntry>> toLoad = Maps.newHashMap();
        resources.forEach((l, r) -> {
            try
            {
                final JsonPokedexEntry entry = loadDatabase(ResourceHelper.getStream(r));
                toLoad.compute(entry.name, (key, list) -> {
                    var ret = list;
                    if (ret == null) ret = Lists.newArrayList();
                    ret.add(entry);
                    return ret;
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

        // Stage 1, create the pokedex entries
        for (var load : loaded) load.toPokedexEntry();
        // Stage 2 initialise them
        for (var load : loaded) load.initStage2(Database.getEntry(load.name));

        registered = true;
    }

    public static void postInit()
    {
        for (var load : LOADED)
        {
            load.postInit(Database.getEntry(load.name));
        }
    }

    public static List<JsonPokedexEntry> LOADED = Lists.newArrayList();

    private static JsonPokedexEntry loadDatabase(final InputStream stream) throws Exception
    {
        JsonPokedexEntry database = null;
        final InputStreamReader reader = new InputStreamReader(stream);
        database = JsonUtil.gson.fromJson(reader, JsonPokedexEntry.class);
        reader.close();
        return database;
    }
}
