package pokecube.wiki;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.XMLPokedexEntry;
import pokecube.core.database.pokedex.PokemobsDatabases;
import pokecube.core.database.pokedex.PokemobsJson;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.MovesUtils;
import thut.core.common.ThutCore;

public class JsonHelper
{
    public static class TrofersGenerator
    {
        static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

        public static JsonObject fromInfo(final PokedexEntry entry)
        {
            final JsonObject name = new JsonObject();
            final JsonArray item = new JsonArray();
            final JsonObject pokemobName = new JsonObject();
            name.addProperty("color", "#D30000");
            name.addProperty("translate", "trophy.trofers.composed");
            pokemobName.addProperty("translate", entry.getUnlocalizedName());
            item.add(pokemobName);
            name.add("with", item);
            return name;
        }

        public static JsonObject fromEffects(final PokedexEntry entry)
        {
            final JsonObject effects = new JsonObject();
            final JsonObject sound = new JsonObject();
            final JsonObject rewards = new JsonObject();
            effects.add("sound", sound);
            effects.add("rewards", rewards);
            sound.addProperty("soundEvent", "" + entry.sound);
            rewards.addProperty("lootTable", "" + entry.lootTable);
            rewards.addProperty("cooldown", 9600);
            return effects;
        }

        public static String makeJson(final PokedexEntry entry, final String id)
        {
            float mobScale = 1;
            float scale = 1;
            final thut.api.maths.vecmath.Vector3f dimensions = entry.getModelSize();
            mobScale = Math.max(dimensions.z, Math.max(dimensions.y, dimensions.x));
            if (dimensions.x > 1 || dimensions.y > 1 || dimensions.z > 1) scale = 1 / (mobScale * 2);
            else if (dimensions.x > 0.5 || dimensions.y > 0.5 || dimensions.z > 0.5) scale = mobScale / (mobScale * 2);
            else scale = 1.0f;

            final JsonObject json = new JsonObject();
            final JsonObject display = new JsonObject();
            final JsonObject entity = new JsonObject();
            final JsonObject colors = new JsonObject();
            json.add("name", TrofersGenerator.fromInfo(entry));
            json.add("effects", TrofersGenerator.fromEffects(entry));
            json.add("display", display);
            json.add("entity", entity);
            json.add("colors", colors);
            display.addProperty("scale", scale);
            entity.addProperty("type", "pokecube:" + entry.getTrimmedName());
            colors.addProperty("base", "#606060");
            colors.addProperty("accent", "#D30000");
            return TrofersGenerator.GSON.toJson(json);
        }

        public static JsonObject fromAllPokemobs(final PokedexEntry entry)
        {
            final JsonObject name = new JsonObject();
            final JsonObject stats = new JsonObject();
            name.addProperty("name", entry.getTrimmedName());
            name.add("stats", stats);
            stats.addProperty("lootTable", "pokecube:entities/" + entry.getTrimmedName());
            return name;
        }

        public static String makeDrops(final PokedexEntry entry, final String id)
        {
            final JsonObject json = new JsonObject();
            final JsonArray item = new JsonArray();
            for (final PokedexEntry e : Pokedex.getInstance().getRegisteredEntries())
                item.add(TrofersGenerator.fromAllPokemobs(e));
            json.add("pokemon", item);
            return TrofersGenerator.GSON.toJson(json);
        }

        public static String[][] makeRequirements(final PokedexEntry entry)
        {
            return new String[][]
            {
                    { entry.getTrimmedName() } };
        }
    }

    protected static void makeTrofers(final PokedexEntry entry, final String id, final String path)
    {
        final ResourceLocation key = new ResourceLocation(entry.getModId(), entry.getTrimmedName());
        final String json = JsonHelper.TrofersGenerator.makeJson(entry, id);
        final File dir = new File("./mods/" + path + "/");
        if (!dir.exists()) dir.mkdirs();
        final File file = new File(dir, key.getPath() + ".json");
        FileWriter write;
        try
        {
            write = new FileWriter(file);
            write.write(json);
            write.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    protected static void makeLootTable(final PokedexEntry entry, final String id, final String path)
    {
        final ResourceLocation key = new ResourceLocation(entry.getModId(), entry.getTrimmedName());
        String loot_table = "{\"type\": \"minecraft:entity\",\"pools\": [{\"name\": \"main\",\"rolls\": 1.0,\"entries\": ["
                + "{\"type\": \"minecraft:item\",\"functions\": [{\"function\": \"minecraft:set_count\",\"count\": "
                + "{\"type\": \"minecraft:uniform\",\"min\": 0.0,\"max\": 1.0},\"add\": false},{\"function\": "
                + "\"minecraft:looting_enchant\",\"count\": {\"type\": \"minecraft:uniform\",\"min\": 0.0,\"max\": 1.0}}],"
                + "\"name\": \"trofers:small_plate\"}],\"conditions\": [{\"condition\": \"trofers:random_trophy_chance\"}],"
                + "\"functions\": [{\"function\": \"minecraft:set_nbt\",\"tag\":\"{BlockEntityTag:{Trophy:"
                + "\\\"pokecube:" + entry.getTrimmedName()
                + "\\\"}}\"}]},{\"name\": \"pool\",\"rolls\": 1,\"entries\": [{"
                + "\"type\": \"loot_table\",\"name\": \"" + entry.lootTable + "\",\"weight\": 1}]}]}";
        final JsonObject obj = JsonHelper.TrofersGenerator.GSON.fromJson(loot_table, JsonObject.class);
        loot_table = JsonHelper.TrofersGenerator.GSON.toJson(obj);
        final File dir = new File("./mods/" + path + "/");
        if (!dir.exists()) dir.mkdirs();
        final File file = new File(dir, key.getPath() + ".json");
        FileWriter write;
        try
        {
            write = new FileWriter(file);
            write.write(loot_table);
            write.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    protected static void makePokemobDrops(final PokedexEntry entry, final String id, final String path)
    {
        final ResourceLocation key = new ResourceLocation(entry.getModId(), "pokemobs_drops");
        final String json = JsonHelper.TrofersGenerator.makeDrops(entry, id);
        final File dir = new File("./mods/" + path + "/");
        if (!dir.exists()) dir.mkdirs();
        final File file = new File(dir, key.getPath() + ".json");
        FileWriter write;
        try
        {
            write = new FileWriter(file);
            write.write(json);
            write.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Merge "source" into "target". If fields have equal name, merge them
     * recursively.
     *
     * @return the merged object (target).
     */
    public static JsonObject deepMerge(final JsonObject source, final JsonObject target) throws Exception
    {
        for (final Entry<String, JsonElement> entry : source.entrySet())
        {
            final String key = entry.getKey();
            final JsonElement value = entry.getValue();
            if (!target.has(key)) // new value for "key":
                target.add(key, value);
            else // existing value for "key" - recursively deep merge:
                if (value instanceof JsonObject)
            {
                final JsonObject valueJson = (JsonObject) value;
                JsonHelper.deepMerge(valueJson, target.getAsJsonObject(key));
            }
                else target.add(key, value);
        }
        return target;
    }

    public static JsonObject database = new JsonObject();

    public static boolean mergeIn(final JsonObject source, final JsonObject target, final String... path)
    {
        JsonObject o = source;
        JsonObject o1 = target;

        JsonObject o_1 = source;
        JsonObject o1_1 = target;
        for (int i = 0; i < path.length; i++)
        {
            final boolean atTarget = i == path.length - 1;
            final String property = path[i];
            final JsonElement value = o.get(property);
            if (value == null) return false;
            if (value.isJsonArray() && value.getAsJsonArray().size() == 0)
            {
                o.remove(property);
                return false;
            }
            if (atTarget)
            {
                if (!property.equals("name")) o.remove(property);
                o1.add(property, value);
                return true;
            }
            else if (value.isJsonObject())
            {
                o_1 = value.getAsJsonObject();
                if (o_1 == null) return false;
                o = o_1;
                if (o1.has(property)) o1_1 = o1.getAsJsonObject(property);
                else
                {
                    o1_1 = new JsonObject();
                    o1.add(property, o1_1);
                }
                o1 = o1_1;
            }
            else System.out.println("Error with merging " + o);
        }
        return true;
    }

    public static void cleanMember(final JsonObject object, final String propery, final Object default_)
    {
        final JsonElement value = object.get(propery);
        if (value == null) return;
        boolean remove = default_ instanceof String && default_.equals(value.getAsString());
        try
        {
            if (!remove && (boolean.class.isInstance(default_) || Boolean.class.isInstance(default_)))
                remove = value.getAsBoolean() == (boolean) default_;
            if (!remove && (float.class.isInstance(default_) || Float.class.isInstance(default_)))
                remove = value.getAsFloat() == (float) default_;
        }
        catch (final Exception e)
        {}
        if (remove) object.remove(propery);
    }

    public static void cleanEmptyLists(final JsonObject object)
    {
        final Set<String> stale = Sets.newHashSet();
        for (final Entry<String, JsonElement> entry : object.entrySet())
        {
            final JsonElement value = entry.getValue();
            if (value.isJsonArray())
            {
                if (value.getAsJsonArray().size() == 0) stale.add(entry.getKey());
                else
                {
                    final Iterator<JsonElement> iter = value.getAsJsonArray().iterator();
                    iter.forEachRemaining(e -> {
                        if (e.isJsonObject()) JsonHelper.cleanEmptyLists(e.getAsJsonObject());
                    });
                }
            }
            else if (value.isJsonObject()) JsonHelper.cleanEmptyLists(value.getAsJsonObject());
        }
        stale.forEach(s -> object.remove(s));
    }

    private static void cleanEntry(final JsonObject o)
    {
        JsonHelper.cleanMember(o, "override", false);
        JsonHelper.cleanMember(o, "dummy", false);
        JsonHelper.cleanMember(o, "starter", false);
        JsonHelper.cleanMember(o, "legend", false);
        JsonHelper.cleanMember(o, "breed", true);
        JsonHelper.cleanMember(o, "hasShiny", true);
        JsonHelper.cleanMember(o, "stock", true);
        JsonHelper.cleanMember(o, "ridable", true);
        JsonHelper.cleanMember(o, "mega", false);
        JsonHelper.cleanMember(o, "gmax", false);
        JsonHelper.cleanMember(o, "gender", "");
        JsonHelper.cleanMember(o, "genderBase", "");
        JsonHelper.cleanMember(o, "baseForm", "");
        JsonHelper.cleanMember(o, "modelType", "");
        JsonHelper.cleanMember(o, "ridden_offsets", "0.75");
        JsonHelper.cleanEmptyLists(o);
        o.addProperty("name", ThutCore.trim(o.get("name").getAsString()));
    }

    public static void load(final ResourceLocation location)
    {
        final List<PokedexEntry> pokedexEntries = Database.getSortedFormes();
        for (final PokedexEntry entry : pokedexEntries)
        {
            JsonHelper.makeTrofers(entry, "pokecube", "trofers");
            JsonHelper.makeLootTable(entry, "pokecube", "loot_tables/entities");
            JsonHelper.makePokemobDrops(entry, "pokecube", "loot_tables/drops");
        }

        final Path path = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("json_tests");
        path.toFile().mkdirs();

        final Map<String, String[][]> tags = Maps.newHashMap();

        tags.put("pokemobs_spawns", new String[][]
        {
                { "stats", "spawnRules" } });
        tags.put("pokemobs_formes", new String[][]
        {
            // @formatter:off
            { "models" },
            { "male_model" },
            { "female_model" },
            { "model" },
            { "mega" },
            { "gmax" },
            { "baseForm" }
            // @formatter:on
        });
        tags.put("pokemobs_drops", new String[][]
        {
                { "stats", "lootTable" },
                { "stats", "heldTable" } });
        tags.put("pokemobs_moves", new String[][]
        {
                { "moves" } });

        tags.put("pokemobs_interacts", new String[][]
        {
            // @formatter:off
            { "dye" },
            { "stats", "evolutions" },
            { "stats", "formeItems" } ,
            { "stats", "foodMat" } ,
            { "stats", "megaRules" } ,
            { "stats", "hatedMaterials" } ,
            { "stats", "activeTimes" } ,
            { "stats", "prey" } ,
            { "stats", "interactions" }
            // @formatter:on
        });
        tags.put("pokemobs_offsets", new String[][]
        {
                { "ridden_offsets" } });

        PokemobsDatabases.compound.pokemon.forEach(e -> {
            final PokedexEntry entry = Database.getEntry(e.name);
            entry.setGMax(entry.isGMax() || e.name.contains("_gigantamax"));

            e.mega = entry.isMega();
            e.gmax = entry.isGMax();

            if (!e.mega) e.mega = null;
            if (!e.gmax) e.gmax = null;

            if (entry.isMega() || entry.isGMax() || entry.isGenderForme) e.baseForm = entry.getBaseName();
        });

        try
        {
            final JsonElement obj = PokedexEntryLoader.gson.toJsonTree(PokemobsDatabases.compound);

            final Iterator<JsonElement> iter = obj.getAsJsonObject().getAsJsonArray("pokemon").iterator();

            iter.forEachRemaining(e -> {
                final JsonObject o = e.getAsJsonObject();

                // Cleanup some values if present
                JsonHelper.cleanEntry(o);

            });

            final String json = PokedexEntryLoader.gson.toJson(obj);
            final File dir = path.resolve("pokemobs_all" + ".json").toFile();
            final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dir),
                    Charset.forName("UTF-8").newEncoder());
            out.write(json);
            out.close();
        }
        catch (final Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JsonElement obj = PokedexEntryLoader.gson.toJsonTree(PokemobsDatabases.compound);
        final PokemobsJson data = PokedexEntryLoader.gson.fromJson(obj, PokemobsJson.class);

        {
            final Iterator<JsonElement> iter = obj.getAsJsonObject().getAsJsonArray("pokemon").iterator();

            iter.forEachRemaining(e -> {
                final JsonObject o = e.getAsJsonObject();

                // Cleanup some values if present
                JsonHelper.cleanEntry(o);

            });
        }

        for (final XMLPokedexEntry val : data.pokemon)
        {
            if (val.stats == null) continue;

            if (val.stats.spawnRules != null && !val.stats.spawnRules.isEmpty()) val.stats.spawnRules.removeIf(r -> {
                double rate = 0;
                // 0 spawn rate rules are done by legends, so lets remove them
                // from here.
                if (r.values.containsKey("rate"))
                {
                    final String val2 = r.values.get("rate");
                    rate = Float.parseFloat(val2);
                }
                return rate <= 0;
            });
        }

        obj = PokedexEntryLoader.gson.toJsonTree(data);

        final JsonArray mobs = new JsonArray();
        final List<PokedexEntry> formes = Database.getSortedFormes();
        formes.forEach(e -> {
            mobs.add(e.getTrimmedName());
            final PokedexEntry male = e.getForGender(IPokemob.MALE);
            final PokedexEntry female = e.getForGender(IPokemob.FEMALE);
            if (!formes.contains(male)) mobs.add(male.getTrimmedName());
            if (!formes.contains(female)) mobs.add(female.getTrimmedName());
        });

        if (mobs.size() > 0)
        {
            final String json = PokedexEntryLoader.gson.toJson(mobs);
            final File dir = path.resolve("pokemobs_names.py").toFile();
            try
            {
                final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dir),
                        Charset.forName("UTF-8").newEncoder());
                out.write("pokemobs = ");
                out.write(json);
                out.close();
            }
            catch (final FileNotFoundException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (final IOException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        final JsonArray moves = new JsonArray();
        MovesUtils.getKnownMoves().forEach(e -> {
            moves.add(e.name);
        });
        if (moves.size() > 0)
        {
            final String json = PokedexEntryLoader.gson.toJson(moves);
            final File dir = path.resolve("moves_names.py").toFile();
            try
            {
                final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dir),
                        Charset.forName("UTF-8").newEncoder());
                out.write("moves = ");
                out.write(json);
                out.close();
            }
            catch (final FileNotFoundException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (final IOException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        final JsonObject starters = new JsonObject();
        final JsonObject legends = new JsonObject();

        starters.addProperty("replace", false);
        legends.addProperty("replace", false);

        starters.add("values", new JsonArray());
        legends.add("values", new JsonArray());

        for (final PokedexEntry e : Database.getSortedFormes())
        {
            if (e.isStarter) starters.getAsJsonArray("values").add(e.getTrimmedName());
            if (e.isLegendary()) legends.getAsJsonArray("values").add(e.getTrimmedName());
        }

        if (moves.size() > 0) try
        {
            File dir = path.resolve("starters.json").toFile();
            // String json = PokedexEntryLoader.gson.toJson(starters);
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dir),
                    Charset.forName("UTF-8").newEncoder());
            // out.write(json);
            out.close();

            dir = path.resolve("legends.json").toFile();
            // json = PokedexEntryLoader.gson.toJson(legends);
            out = new OutputStreamWriter(new FileOutputStream(dir), Charset.forName("UTF-8").newEncoder());
            // out.write(json);
            out.close();

        }
        catch (final FileNotFoundException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (final IOException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        for (final Entry<String, String[][]> entries : tags.entrySet())
        {
            final String[][] toMerge = entries.getValue();
            final String filename = entries.getKey();
            final JsonObject newDatabase = new JsonObject();
            final Iterator<JsonElement> iter = obj.getAsJsonObject().getAsJsonArray("pokemon").iterator();
            newDatabase.add("pokemon", new JsonArray());
            try
            {
                iter.forEachRemaining(e -> {
                    final JsonObject o = e.getAsJsonObject();

                    // Cleanup some values if present
                    JsonHelper.cleanEntry(o);

                    final JsonObject o1 = new JsonObject();
                    if (!JsonHelper.mergeIn(o, o1, "name")) return;
                    boolean did = false;
                    for (final String[] var : toMerge) did = JsonHelper.mergeIn(o, o1, var) || did;
                    if (did) newDatabase.getAsJsonArray("pokemon").add(o1);
                });
                final String json = PokedexEntryLoader.gson.toJson(newDatabase);
                final File dir = path.resolve(filename + ".json").toFile();
                final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dir),
                        Charset.forName("UTF-8").newEncoder());
                out.write(json);
                out.close();
            }
            catch (final Exception e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        try
        {
            final String json = PokedexEntryLoader.gson.toJson(obj);
            final File dir = path.resolve("pokemobs_pokedex" + ".json").toFile();
            final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dir),
                    Charset.forName("UTF-8").newEncoder());
            out.write(json);
            out.close();
        }
        catch (final Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
