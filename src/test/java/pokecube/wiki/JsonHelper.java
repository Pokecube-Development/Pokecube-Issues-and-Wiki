package pokecube.wiki;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.interfaces.IPokemob;

public class JsonHelper
{
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
            else
            {
                o_1 = o.getAsJsonObject(property);
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
            if (!remove && (boolean.class.isInstance(default_) || Boolean.class.isInstance(default_))) remove = value
                    .getAsBoolean() == (boolean) default_;
            if (!remove && (float.class.isInstance(default_) || Float.class.isInstance(default_))) remove = value
                    .getAsFloat() == (float) default_;
        }
        catch (final Exception e)
        {
        }
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
                    iter.forEachRemaining(e ->
                    {
                        if (e.isJsonObject()) JsonHelper.cleanEmptyLists(e.getAsJsonObject());
                    });
                }
            }
            else if (value.isJsonObject()) JsonHelper.cleanEmptyLists(value.getAsJsonObject());
        }
        stale.forEach(s -> object.remove(s));
    }

    public static void load(final ResourceLocation location)
    {

        final Map<String, String[][]> tags = Maps.newHashMap();

        tags.put("pokemobs_spawns", new String[][] { { "stats", "spawnRules" } });
        tags.put("pokemobs_formes", new String[][] { { "models" } });
        tags.put("pokemobs_drops", new String[][] { { "stats", "lootTable" }, { "stats", "heldTable" } });

        tags.put("pokemobs_interacts", new String[][] {
            // @formatter:off
            { "stats", "evolutions" },
            { "stats", "formeItems" } ,
            { "stats", "foodMat" } ,
            { "stats", "megaRules" } ,
            { "stats", "hatedMaterials" } ,
            { "stats", "activeTimes" } ,
            { "stats", "prey" } ,
            { "stats", "interactions" } ,
            { "stats", "specialEggRules" }
            // @formatter:on
        });
        tags.put("pokemobs_offsets", new String[][] { { "ridden_offsets" } });

        final JsonElement obj = PokedexEntryLoader.gson.toJsonTree(PokedexEntryLoader.database);

        final JsonArray mobs = new JsonArray();
        final List<PokedexEntry> formes = Database.getSortedFormes();
        formes.forEach(e -> {
            mobs.add(e.getTrimmedName());
            final PokedexEntry male = e.getForGender(IPokemob.MALE);
            final PokedexEntry female = e.getForGender(IPokemob.FEMALE);
            if(!formes.contains(male)) mobs.add(male.getTrimmedName());
            if(!formes.contains(female)) mobs.add(female.getTrimmedName());
        });

        if(mobs.size()>0)
        {
            final String json = PokedexEntryLoader.gson.toJson(mobs);
            final Path path = FMLPaths.CONFIGDIR.get().resolve("pokecube");
            path.toFile().mkdirs();
            final File dir = path.resolve("pokemobs_names.py").toFile();
            try
            {
                final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dir), Charset.forName(
                        "UTF-8").newEncoder());
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


        for (final Entry<String, String[][]> entries : tags.entrySet())
        {
            final String[][] toMerge = entries.getValue();
            final String filename = entries.getKey();
            final JsonObject newDatabase = new JsonObject();
            final Iterator<JsonElement> iter = obj.getAsJsonObject().getAsJsonArray("pokemon").iterator();
            newDatabase.add("pokemon", new JsonArray());
            try
            {
                iter.forEachRemaining(e ->
                {
                    final JsonObject o = e.getAsJsonObject();

                    // Cleanup some values if present
                    JsonHelper.cleanMember(o, "override", false);
                    JsonHelper.cleanMember(o, "dummy", false);
                    JsonHelper.cleanMember(o, "starter", false);
                    JsonHelper.cleanMember(o, "legend", false);
                    JsonHelper.cleanMember(o, "breed", true);
                    JsonHelper.cleanMember(o, "hasShiny", true);
                    JsonHelper.cleanMember(o, "stock", true);
                    JsonHelper.cleanMember(o, "ridable", true);
                    JsonHelper.cleanMember(o, "gender", "");
                    JsonHelper.cleanMember(o, "genderBase", "");
                    JsonHelper.cleanMember(o, "modelType", "");
                    JsonHelper.cleanMember(o, "ridden_offsets", "0.75");
                    JsonHelper.cleanEmptyLists(o);

                    final JsonObject o1 = new JsonObject();
                    if (!JsonHelper.mergeIn(o, o1, "name")) return;
                    boolean did = false;
                    for (final String[] var : toMerge)
                        did = JsonHelper.mergeIn(o, o1, var) || did;
                    if (did) newDatabase.getAsJsonArray("pokemon").add(o1);
                });
                final String json = PokedexEntryLoader.gson.toJson(newDatabase);
                final Path path = FMLPaths.CONFIGDIR.get().resolve("pokecube");
                path.toFile().mkdirs();
                final File dir = path.resolve(filename + ".json").toFile();
                final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dir), Charset.forName(
                        "UTF-8").newEncoder());
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
            final Path path = FMLPaths.CONFIGDIR.get().resolve("pokecube");
            path.toFile().mkdirs();
            final File dir = path.resolve("pokemobs_pokedex" + ".json").toFile();
            final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dir), Charset.forName("UTF-8")
                    .newEncoder());
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
