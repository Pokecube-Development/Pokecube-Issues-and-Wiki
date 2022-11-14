package pokecube.core.database.pokedex;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ClassUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.api.data.PokedexEntry.MovementType;
import pokecube.api.data.PokedexEntry.SpawnData;
import pokecube.api.data.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.api.data.pokedex.DefaultFormeHolder;
import pokecube.api.data.pokedex.InteractsAndEvolutions.BaseMegaRule;
import pokecube.api.data.pokedex.InteractsAndEvolutions.DyeInfo;
import pokecube.api.data.pokedex.InteractsAndEvolutions.Evolution;
import pokecube.api.data.pokedex.InteractsAndEvolutions.FormeItem;
import pokecube.api.data.pokedex.InteractsAndEvolutions.Interact;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.events.pokemobs.SpawnEvent.FunctionVariance;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.spawns.PokemobSpawns;
import thut.api.entity.multipart.GenericPartEntity.BodyNode;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;
import thut.core.xml.bind.annotation.XmlAnyAttribute;
import thut.core.xml.bind.annotation.XmlElement;
import thut.core.xml.bind.annotation.XmlRootElement;

public class PokedexEntryLoader
{
    @Retention(RUNTIME)
    @Target(FIELD)
    public @interface ManualCopy
    {

    }

    public static interface IMergeable<T>
    {
        T mergeFrom(@Nullable T other);

        default void mergeBasic(T other)
        {
            Field fields[] = new Field[] {};
            try
            {
                // returns the array of Field objects representing the public
                // fields
                fields = this.getClass().getDeclaredFields();
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }

            var def = new JsonPokedexEntry();

            for (final Field field : fields) try
            {
                if (Modifier.isFinal(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                var theirs = field.get(other);

                if (theirs != null)
                {
                    if (ClassUtils.isPrimitiveOrWrapper(theirs.getClass()) && theirs == field.get(def)) continue;
                    field.set(this, theirs);
                }
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @XmlRootElement(name = "Drop")
    public static class Drop
    {
        @XmlAnyAttribute
        public Map<String, String> values = Maps.newHashMap();
        @XmlElement(name = "tag")
        public String tag;
        @XmlElement(name = "id")
        public String id;

        public Map<String, String> getValues()
        {
            if (this.values == null) this.values = Maps.newHashMap();
            final String tagName = new String("tag");
            final String idName = new String("id");
            if (this.tag != null && !this.values.containsKey(tagName)) this.values.put(tagName, this.tag);
            if (this.id != null && !this.values.containsKey(idName)) this.values.put(idName, this.id);
            return this.values;
        }
    }

    public static class Moves
    {
        public static class Misc
        {
            public String moves;

            @Override
            public String toString()
            {
                return this.moves;
            }
        }

        public JsonObject lvlupMoves;

        public Misc misc;
    }

    public static class StatsNode implements IMergeable<StatsNode>, Consumer<PokedexEntry>
    {
        public static class Stats
        {
            public Map<String, String> values = Maps.newHashMap();
        }

        // Evolution stuff
        public List<Evolution> evolutions = Lists.newArrayList();

        // Prey, Food and Egg stuff
        public String prey;

        // Drops and items
        public String lootTable;
        public String heldTable;

        // Spawn Rules
        @ManualCopy
        public Boolean overwrite;
        // STATS
        public Stats stats;
        public Stats evs;
        public Stats sizes;
        public Stats types;
        public Stats abilities;
        public Float mass;
        public Integer captureRate = -1;
        public Integer baseExp = -1;
        public Integer baseFriendship = 70;
        public String expMode;

        public Integer genderRatio = -1;

        // MISC

        @ManualCopy
        public List<SpawnRule> spawnRules = Lists.newArrayList();
        // New Mega rules
        public List<BaseMegaRule> megaRules = Lists.newArrayList();
        public List<Interact> interactions = Lists.newArrayList();

        public String movementType;
        public String shadowReplacements;
        public String hatedMaterials;

        public StatsNode()
        {}

        public StatsNode(PokedexEntry from)
        {
            stats = new Stats();
            stats.values.put("hp", from.getStatHP() + "");
            stats.values.put("atk", from.getStatATT() + "");
            stats.values.put("def", from.getStatDEF() + "");
            stats.values.put("spatk", from.getStatATTSPE() + "");
            stats.values.put("spdef", from.getStatDEFSPE() + "");
            stats.values.put("spd", from.getStatVIT() + "");

            this.mass = (float) from.mass;
            this.captureRate = from.catchRate;
            this.baseExp = from.baseXP;
            this.genderRatio = from.sexeRatio;

            evs = new Stats();
            if (from.evs[0] != 0) evs.values.put("hp", from.evs[0] + "");
            if (from.evs[1] != 0) evs.values.put("atk", from.evs[1] + "");
            if (from.evs[2] != 0) evs.values.put("def", from.evs[2] + "");
            if (from.evs[3] != 0) evs.values.put("spatk", from.evs[3] + "");
            if (from.evs[4] != 0) evs.values.put("spdef", from.evs[4] + "");
            if (from.evs[5] != 0) evs.values.put("spd", from.evs[5] + "");

            this.types = new Stats();
            types.values.put("type1", from.getType1().name());
            if (from.getType1() != from.getType2()) types.values.put("type2", from.getType2().name());

            sizes = new Stats();
            sizes.values.put("height", from.height + "");
            sizes.values.put("width", from.width + "");
        }

        @Override
        /**
         * Here we apply any settings which can be done during early setup.
         * These are things which do not depend on registered value later, or
         * vanilla datapack stuff.
         */
        public void accept(PokedexEntry entry)
        {
            final int[] stats = new int[6];
            final byte[] evs = new byte[6];
            boolean stat = false, ev = false;
            if (this.stats != null)
            {
                final Map<String, String> values = this.stats.values;
                for (final String key : values.keySet())
                {
                    final String keyString = key.toString();
                    final String value = values.get(key);
                    if (keyString.equals("hp")) stats[0] = Integer.parseInt(value);
                    if (keyString.equals("atk")) stats[1] = Integer.parseInt(value);
                    if (keyString.equals("def")) stats[2] = Integer.parseInt(value);
                    if (keyString.equals("spatk")) stats[3] = Integer.parseInt(value);
                    if (keyString.equals("spdef")) stats[4] = Integer.parseInt(value);
                    if (keyString.equals("spd")) stats[5] = Integer.parseInt(value);
                }
                stat = true;
            }
            if (this.evs != null)
            {
                final Map<String, String> values = this.evs.values;
                for (final String key : values.keySet())
                {
                    final String keyString = key.toString();
                    final String value = values.get(key);
                    if (keyString.equals("hp")) evs[0] = (byte) Integer.parseInt(value);
                    if (keyString.equals("atk")) evs[1] = (byte) Integer.parseInt(value);
                    if (keyString.equals("def")) evs[2] = (byte) Integer.parseInt(value);
                    if (keyString.equals("spatk")) evs[3] = (byte) Integer.parseInt(value);
                    if (keyString.equals("spdef")) evs[4] = (byte) Integer.parseInt(value);
                    if (keyString.equals("spd")) evs[5] = (byte) Integer.parseInt(value);
                }
                ev = true;
            }
            if (stat) entry.stats = stats;
            if (ev) entry.evs = evs;
            if (this.types != null)
            {
                final Map<String, String> values = this.types.values;
                for (final String key : values.keySet())
                {
                    final String keyString = key.toString();
                    final String value = values.get(key);
                    if (keyString.equals("type1")) entry.type1 = PokeType.getType(value);
                    if (keyString.equals("type2")) entry.type2 = PokeType.getType(value);
                }
            }
            if (this.sizes != null)
            {
                final Map<String, String> values = this.sizes.values;
                for (final String key : values.keySet())
                {
                    final String keyString = key.toString();
                    final String value = values.get(key);
                    if (keyString.equals("height")) entry.height = Float.parseFloat(value);
                    if (keyString.equals("length")) entry.length = Float.parseFloat(value);
                    if (keyString.equals("width")) entry.width = Float.parseFloat(value);
                }
                if (entry.width == -1) entry.width = entry.height;
                if (entry.length == -1) entry.length = entry.width;
            }
            if (this.abilities != null)
            {
                final Map<String, String> values = this.abilities.values;
                for (final String key : values.keySet())
                {
                    final String keyString = key.toString();
                    final String value = values.get(key);
                    if (keyString.equals("hidden"))
                    {
                        final String[] vars = value.split(",");
                        for (final String var : vars)
                            if (!entry.abilitiesHidden.contains(var.trim())) entry.abilitiesHidden.add(var.trim());
                    }
                    if (keyString.equals("normal"))
                    {
                        final String[] vars = value.split(",");
                        for (final String var : vars)
                            if (!entry.abilities.contains(var.trim())) entry.abilities.add(var.trim());
                        if (entry.abilities.size() == 1) entry.abilities.add(entry.abilities.get(0));
                    }
                }
            }
            if (this.captureRate != PokedexEntryLoader.missingno.stats.captureRate) entry.catchRate = this.captureRate;
            if (this.baseExp != PokedexEntryLoader.missingno.stats.baseExp) entry.baseXP = this.baseExp;
            if (this.baseFriendship != null) entry.baseHappiness = this.baseFriendship;
            if (this.genderRatio != PokedexEntryLoader.missingno.stats.genderRatio) entry.sexeRatio = this.genderRatio;
            if (this.mass != PokedexEntryLoader.missingno.stats.mass && this.mass != null) entry.mass = this.mass;
            if (entry.ridable != PokedexEntryLoader.missingno.ridable)
                entry.ridable = PokedexEntryLoader.missingno.ridable;
            if (this.movementType != null)
            {
                final String[] strings = this.movementType.trim().split(":");
                final String typeArg = strings[0];
                final String[] types = typeArg.split(",");
                for (final String type : types)
                {
                    final MovementType t = MovementType.getType(type);
                    if (t != null) entry.mobType |= t.mask;
                }
                if (strings.length > 1) entry.preferedHeight = Double.parseDouble(strings[1]);
            }
            if (this.prey != null) entry.food = this.prey.trim().split(" ");
        }

        @Override
        public String toString()
        {
            return this.spawnRules + "";
        }

        @Override
        public StatsNode mergeFrom(@Nullable final StatsNode other)
        {
            if (other != null)
            {
                final boolean replaceSpawns = other.overwrite != null && other.overwrite;
                // Replace the list entirely
                if (replaceSpawns) this.spawnRules = other.spawnRules;
                // Otherwise merge the lists
                else for (final SpawnRule rule : other.spawnRules)
                    if (!this.spawnRules.contains(rule)) this.spawnRules.add(rule);
            }
            return this;
        }
    }

    public static class XMLPokedexEntry
    {
        public String name;
        public Integer number;

        public Boolean base = false;
        public Boolean dummy = false;
        public Boolean stock = true;

        public Boolean ridable = true;
        public Boolean hasShiny = true;

        public Boolean override = false;

        public Boolean mega = false;
        public Boolean gmax = false;

        public String modelType = "";

        public String baseForm = "";

        public String sound = null;

        public String model_path = null;
        public String tex_path = null;
        public String anim_path = null;

        public String modid = "pokecube_mobs";

        public String ridden_offsets = "0.75";

        public Map<String, BodyNode> poseShapes = Maps.newHashMap();

        public DefaultFormeHolder model = null;
        public DefaultFormeHolder male_model = null;
        public DefaultFormeHolder female_model = null;

        public List<FormeItem> formeItems = Lists.newArrayList();

        public StatsNode stats;
        public Moves moves;

        public DyeInfo dye = null;

        public List<DefaultFormeHolder> models = Lists.newArrayList();

        @Override
        public String toString()
        {
            return this.name + " " + this.number + " " + this.stats + " " + this.moves;
        }

        /**
         * Blank constructor for json use
         */
        public XMLPokedexEntry()
        {}

        /**
         * constructor for making from a pokedex entry
         */
        public XMLPokedexEntry(PokedexEntry from)
        {
            this.name = from.getTrimmedName();
            this.number = from.getPokedexNb();
            this.stock = from.stock;
            this.dummy = from.dummy;
            this.base = from.base;

            this.stats = new StatsNode(from);

            if (from.getBaseForme() != null) this.baseForm = from.getBaseName();
        }
    }

    public static final Gson gson = JsonUtil.gson;

    public static final Comparator<XMLPokedexEntry> ENTRYSORTER = (o1, o2) -> {
        int diff = o1.number - o2.number;
        if (diff == 0) if (o1.base && !o2.base) diff = -1;
        else if (o2.base && !o1.base) diff = 1;
        return diff;
    };

    public static XMLPokedexEntry missingno = new XMLPokedexEntry();

    static
    {
        PokedexEntryLoader.missingno.stats = new StatsNode();
    }

    public static List<ResourceLocation> hotloadable = Lists.newArrayList();

    @SuppressWarnings(
    { "unchecked" })
    public static Object getSerializableCopy(final Class<?> type, final Object original)
            throws InstantiationException, IllegalAccessException
    {
        Field fields[] = new Field[] {};
        try
        {
            // returns the array of Field objects representing the public fields
            fields = type.getDeclaredFields();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        Object copy = null;
        try
        {
            copy = type.getConstructor().newInstance();
        }
        catch (final Exception e1)
        {
            copy = original;
        }
        if (copy == original || copy != null && copy.equals(original))
        {
            return copy;
        }
        Object value;
        Object defaultvalue;
        for (final Field field : fields) try
        {
            if (Modifier.isFinal(field.getModifiers())) continue;
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;
            if (field.getName().startsWith("_")) continue;
            try
            {
                field.setAccessible(true);
            }
            catch (Throwable e)
            {
                // Module stuff can make this fail in some cases.
                continue;
            }
            value = field.get(original);
            defaultvalue = field.get(copy);
            if (value == null) continue;
            if (ClassUtils.isPrimitiveOrWrapper(value.getClass())) field.set(copy, value);
            else if (defaultvalue != null && defaultvalue.equals(value)) field.set(copy, null);
            else if (value instanceof String)
            {
                if (((String) value).isEmpty()) field.set(copy, null);
                else field.set(copy, value);
            }
            else if (value instanceof Object[])
            {
                if (((Object[]) value).length == 0) field.set(copy, null);
                else field.set(copy, value);
            }
            else if (value instanceof Map)
            {
                if (((Map<?, ?>) value).isEmpty()) field.set(copy, null);
                else field.set(copy, value);
            }
            else if (value instanceof Collection)
            {
                if (((Collection<?>) value).isEmpty()) field.set(copy, null);
                else
                {
                    if (value instanceof List)
                    {
                        final List<Object> args = (List<Object>) value;
                        final ListIterator<Object> iter = args.listIterator();
                        while (iter.hasNext())
                        {
                            final Object var = iter.next();
                            iter.set(PokedexEntryLoader.getSerializableCopy(var.getClass(), var));
                        }
                    }
                    field.set(copy, value);
                }
            }
            else field.set(copy, PokedexEntryLoader.getSerializableCopy(value.getClass(), value));
        }
        catch (final IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return copy;
    }

    /**
     * This must be called after tags are loaded server side.
     *
     * @param d
     * @return
     */
    public static ItemStack getStackFromDrop(final Drop drop)
    {
        return Tools.getStack(drop.getValues());
    }

    public static final String MIN = new String("min");
    public static final String MAX = new String("max");
    public static final String RATE = new String("rate");
    public static final String LEVEL = new String("level");
    public static final String VARIANCE = new String("variance");
    public static final String MINY = new String("minY");
    public static final String MAXY = new String("maxY");

    /**
     * This is safe to run before tags are loaded.
     * 
     * @param entry
     *
     * @param spawnData
     * @param rule
     */
    public static SpawnBiomeMatcher handleAddSpawn(PokedexEntry entry, SpawnBiomeMatcher matcher)
    {
        final SpawnEntry spawnEntry = new SpawnEntry();
        String val;
        SpawnRule rule = matcher.spawnRule.copy();
        if ((val = rule.values.remove(MIN)) != null) spawnEntry.min = Integer.parseInt(val);
        if ((val = rule.values.remove(MAX)) != null) spawnEntry.max = Integer.parseInt(val);
        if ((val = rule.values.remove(MINY)) != null) spawnEntry.minY = Integer.parseInt(val);
        if ((val = rule.values.remove(MAXY)) != null) spawnEntry.maxY = Integer.parseInt(val);
        if ((val = rule.values.remove(RATE)) != null) spawnEntry.rate = Float.parseFloat(val);
        if ((val = rule.values.remove(LEVEL)) != null) spawnEntry.level = Integer.parseInt(val);
        if ((val = rule.values.remove(VARIANCE)) != null) spawnEntry.variance = new FunctionVariance(val);
        if (entry.getSpawnData() == null) entry.setSpawnData(new SpawnData(entry));
        matcher = SpawnBiomeMatcher.get(rule);
        entry.getSpawnData().matchers.put(matcher, spawnEntry);
        if (!Database.spawnables.contains(entry)) Database.spawnables.add(entry);
        // If it can spawn in water, then it can swim in water.
        if (matcher.water) entry.mobType |= MovementType.WATER.mask;
        return matcher;
    }

    /**
     * This can be called before tags are added.
     *
     * @param entry
     * @param xmlMoves
     */
    private static void initMoves(final PokedexEntry entry, final Moves xmlMoves)
    {
        Map<Integer, ArrayList<String>> lvlUpMoves = new HashMap<>();
        ArrayList<String> allMoves = new ArrayList<>();
        if (xmlMoves.misc != null && xmlMoves.misc.moves != null)
        {
            final String[] misc = xmlMoves.misc.moves.split(",");
            for (final String s : misc) allMoves.add(Database.convertMoveName(s));
        }
        if (xmlMoves.lvlupMoves != null)
        {
            JsonObject o = xmlMoves.lvlupMoves;
            // This supports the old format as well.
            if (o.has("values")) o = o.getAsJsonObject("values");
            for (final Entry<String, JsonElement> key : o.entrySet())
            {
                final String keyName = key.getKey();
                if (key.getValue().isJsonObject())
                {
                    PokecubeAPI.LOGGER.error("Error with value: {} {} for {}", keyName, key.getValue(), entry);
                    continue;
                }
                final String[] values = key.getValue().getAsString().split(",");
                ArrayList<String> moves;
                lvlUpMoves.put(Integer.parseInt(keyName), moves = new ArrayList<>());
                moves:
                for (String s : values)
                {
                    s = Database.convertMoveName(s);
                    moves.add(s);
                    for (final String s1 : allMoves) if (s1.equalsIgnoreCase(s)) continue moves;
                    allMoves.add(Database.convertMoveName(s));
                }
            }
        }

        if (allMoves.isEmpty()) allMoves = null;
        if (lvlUpMoves.isEmpty()) lvlUpMoves = null;
        entry.addMoves(allMoves, lvlUpMoves);
    }

    public static void initFormeModels(final PokedexEntry entry, final List<DefaultFormeHolder> list)
    {
        list.forEach(holder -> initFormeModel(entry, holder));
    }

    public static void initFormeModel(final PokedexEntry entry, DefaultFormeHolder holder)
    {
        FormeHolder forme = holder.getForme(entry);
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Loaded form for {}: ({} {} {}) -> ({} {} {} {})",
                entry, holder.model, holder.anim, holder.tex, forme.key, forme.model, forme.animation, forme.texture);
    }

    public static void updateEntry(final PokedexEntry entry)
    {
        PokemobsDatabases.load();

        final List<XMLPokedexEntry> entries = Lists.newArrayList(PokemobsDatabases.compound.pokemon);

        for (final XMLPokedexEntry xmlEntry : entries)
        {
            final String name = xmlEntry.name;
            if (!name.equals(entry.getName())) continue;
            PokedexEntryLoader.updateEntry(xmlEntry, false);
            return;
        }
    }

    public static void onReloaded()
    {
        PokemobsDatabases.load();
        for (final XMLPokedexEntry xmlEntry : PokemobsDatabases.compound.pokemon)
        {
            final String name = xmlEntry.name;
            final PokedexEntry entry = Database.getEntry(name);
            // Reset the spawn data, we will reload the bulk manual spawns right
            // after this as well
            entry.setSpawnData(new SpawnData(entry));
            PokedexEntryLoader.updateEntry(xmlEntry, false);
        }
        // now register bulk defined spawns
        PokemobSpawns.registerSpawns();
    }

    public static void makeEntries(final boolean create)
    {
        final List<XMLPokedexEntry> entries = Lists.newArrayList(PokemobsDatabases.compound.pokemon);

        entries.removeIf(value -> {
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
                        "Error with entry for {}, it is missing a Number for sorting! removing on make", value.name);
                return true;
            }
            return false;
        });

        Collections.sort(entries, PokedexEntryLoader.ENTRYSORTER);

        for (final XMLPokedexEntry xmlEntry : entries)
        {
            String name = xmlEntry.name;
            final int number = xmlEntry.number;
            if (create)
            {
                if (Database.getEntry(name) == null)
                {
                    final PokedexEntry entry = new PokedexEntry(number, name);
                    if (xmlEntry.base)
                    {
                        entry.base = xmlEntry.base;
                        Database.baseFormes.put(number, entry);
                        Database.addEntry(entry);
                    }
                }
            }
            PokedexEntryLoader.updateEntry(xmlEntry, create);
        }
    }

    public static void mergeNonDefaults(final Object defaults, final Object outOf, final Object inTo)
    {
        if (outOf.getClass() != inTo.getClass())
            throw new IllegalArgumentException("To and From must be of the same class!");
        Field fields[] = new Field[] {};
        try
        {
            fields = outOf.getClass().getDeclaredFields();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        Object valueOut;
        Object valueIn;
        Object valueDefault;
        for (final Field field : fields) try
        {
            if (Modifier.isFinal(field.getModifiers())) continue;
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;
            final ManualCopy annot = field.getAnnotation(ManualCopy.class);
            if (annot != null) continue;
            field.setAccessible(true);
            valueOut = field.get(outOf);
            valueIn = field.get(inTo);
            valueDefault = field.get(defaults);
            if (valueOut == null) continue;
            if (valueIn == null && valueOut != null)
            {
                field.set(inTo, valueOut);
                continue;
            }

            final boolean outIsDefault = valueOut == valueDefault
                    || valueDefault != null && valueDefault.equals(valueOut);
            if (!outIsDefault) if (valueOut instanceof String) field.set(inTo, valueOut);
            else if (valueOut instanceof Object[]) field.set(inTo, ((Object[]) valueOut).clone());
            else if (valueOut instanceof Map) field.set(inTo, valueOut);
            else if (valueOut instanceof Collection) field.set(inTo, valueOut);
            else try
            {
                valueDefault = valueOut.getClass().getConstructor().newInstance();
                PokedexEntryLoader.mergeNonDefaults(valueDefault, valueOut, valueIn);
                field.set(inTo, valueIn);
            }
            catch (final Exception e)
            {
                field.set(inTo, valueOut);
            }
        }
        catch (final IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This can be run at any point after the main entries are all known.
     *
     * @param entry
     * @param xmlStats
     * @param error
     */
    static void parseEvols(final PokedexEntry entry, final List<Evolution> evolutions, final boolean error)
    {
        if (evolutions != null && !evolutions.isEmpty())
        {
            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Proccessing Evos for " + entry.getName());
            for (final Evolution evol : evolutions)
            {
                final String name = evol.name;
                final PokedexEntry evolEntry = Database.getEntry(name);
                if (evolEntry == null)
                {
                    PokecubeAPI.LOGGER.error("Entry {} not found for evolution of {}, skipping", name, entry.name);
                    continue;
                }
                EvolutionData data = null;
                final boolean clear = evol.clear != null && evol.clear;
                // check for specific clearing info for this entry.
                for (final EvolutionData d : entry.evolutions) if (d.data.equals(evol))
                {
                    data = d;
                    if (clear)
                    {
                        entry.evolutions.remove(d);
                        PokecubeAPI.logInfo("Replacing evolution for " + entry + " -> " + evolEntry);
                    }
                    break;
                }
                if (data == null || clear)
                {
                    data = new EvolutionData(evolEntry);
                    data.data = evol;
                    data.preEvolution = entry;
                    // Skip any exactly duplicated entires.
                    check:
                    {
                        if (!clear) for (final EvolutionData existing : entry.evolutions)
                            if (existing.data.equals(data.data)) break check;
                        entry.addEvolution(data);
                    }
                }
            }
        }
    }

    /**
     * This can be called before tags load.
     *
     * @param entry
     * @param xmlStats
     */
    private static void parseSpawns(final PokedexEntry entry, final StatsNode xmlStats)
    {
        if (xmlStats.spawnRules.isEmpty()) return;
        final boolean overwrite = xmlStats.overwrite == null ? false : xmlStats.overwrite;
        if (overwrite) entry.setSpawnData(new SpawnData(entry));
        for (final SpawnRule rule : xmlStats.spawnRules)
        {
            final FormeHolder holder = rule.getForme(entry);
            if (holder != null) Database.registerFormeHolder(entry, holder);
            final SpawnBiomeMatcher matcher = SpawnBiomeMatcher.get(rule);
            PokedexEntryLoader.handleAddSpawn(entry, matcher);
            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Handling Spawns for {}", entry);
        }
    }

    /**
     * This must be called AFTER tags are loaded.
     *
     * @param entry
     * @param xmlStats
     */
    private static void postIniStats(final PokedexEntry entry, final StatsNode xmlStats)
    {
        // Items
        // Drops Loot table
        if (xmlStats.lootTable != null && !xmlStats.lootTable.isEmpty())
            entry.lootTable = new ResourceLocation(xmlStats.lootTable);
        // Held
        if (xmlStats.heldTable != null && !xmlStats.heldTable.isEmpty())
            entry.heldTable = new ResourceLocation(xmlStats.heldTable);
        try
        {
            if (xmlStats.expMode != null) entry.evolutionMode = Tools.getType(xmlStats.expMode);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error with expmode" + entry, e);
        }
        if (xmlStats.shadowReplacements != null)
        {
            final String[] replaces = xmlStats.shadowReplacements.split(":");
            for (String s1 : replaces)
            {
                s1 = ThutCore.trim(s1);
                if (s1.isEmpty()) continue;

                if (Database.mobReplacements.containsKey(s1)) Database.mobReplacements.get(s1).add(entry);
                else
                {
                    Database.mobReplacements.put(s1, new ArrayList<PokedexEntry>());
                    Database.mobReplacements.get(s1).add(entry);
                }
            }
        }
        if (!xmlStats.interactions.isEmpty()) entry.addInteractions(xmlStats.interactions);
        if (xmlStats.megaRules != null) entry._loaded_megarules.addAll(xmlStats.megaRules);
    }

    public static void postInit()
    {
        try
        {
            PokedexEntry.InteractionLogic.initDefaults();
            JsonPokedexEntry.postInit();
            PokedexEntryLoader.makeEntries(false);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error with postinit of loading pokedex entries", e);
        }
    }

    /**
     * Safe to call whenever.
     *
     * @param xmlEntry
     */
    public static void preCheckEvolutions(final XMLPokedexEntry xmlEntry)
    {
        final PokedexEntry entry = Database.getEntry(xmlEntry.name);
        final StatsNode stats = xmlEntry.stats;
        if (stats != null) try
        {
            PokedexEntryLoader.parseEvols(entry, stats.evolutions, false);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error with " + xmlEntry + " entry? " + entry, e);
        }
    }

    public static void updateEntry(final XMLPokedexEntry xmlEntry, final boolean init)
    {
        final StatsNode stats = xmlEntry.stats;
        final Moves moves = xmlEntry.moves;
        final String name = xmlEntry.name;
        final PokedexEntry entry = Database.getEntry(name);
        entry.modelExt = xmlEntry.modelType;

        if (xmlEntry.model_path != null && xmlEntry.tex_path != null)
        {
            final String tex = xmlEntry.tex_path;
            final String model = xmlEntry.model_path;
            String anim = xmlEntry.anim_path;
            if (anim == null) anim = model;
            entry.setModId(xmlEntry.modid);
            entry.texturePath = tex;
            entry.model = new ResourceLocation(model + entry.getTrimmedName() + entry.modelExt);
            entry.texture = new ResourceLocation(tex + entry.getTrimmedName() + ".png");
            entry.animation = new ResourceLocation(anim + entry.getTrimmedName() + ".xml");
        }

        if (xmlEntry.poseShapes != null && !xmlEntry.poseShapes.isEmpty())
        {
            entry.poseShapes = null;
            xmlEntry.poseShapes.forEach((s, n) -> n.onLoad());
            entry.poseShapes = xmlEntry.poseShapes;
        }

        entry.setMega(xmlEntry.mega != null && xmlEntry.mega);
        entry.setGMax(xmlEntry.gmax != null && xmlEntry.gmax);

        if (!init && xmlEntry.baseForm != null && !xmlEntry.baseForm.isEmpty())
        {
            final PokedexEntry base = Database.getEntry(xmlEntry.baseForm);
            if (base == null) PokecubeAPI.LOGGER.error("Error with base form {} for {}", xmlEntry.baseForm, entry);
            else entry.setBaseForme(base);
        }

        if (entry._default_holder == null && xmlEntry.model != null) entry._default_holder = xmlEntry.model;
        if (entry._male_holder == null && xmlEntry.male_model != null) entry._male_holder = xmlEntry.male_model;
        if (entry._female_holder == null && xmlEntry.female_model != null) entry._female_holder = xmlEntry.female_model;

        if (stats != null) try
        {
            if (xmlEntry.sound != null) entry.customSound = xmlEntry.sound;
            entry.modelExt = xmlEntry.modelType;
            stats.accept(entry);
            if (!init)
            {
                PokedexEntryLoader.initFormeModels(entry, xmlEntry.models);
                entry.ridable = xmlEntry.ridable;
                entry.hasShiny = xmlEntry.hasShiny;

                if (xmlEntry.ridden_offsets != null)
                {
                    // This is each passenger
                    final String[] args = xmlEntry.ridden_offsets.split(":");
                    final List<double[]> offsets = Lists.newArrayList();
                    for (final String s : args)
                    {
                        final String[] vec = s.split(",");
                        if (vec.length == 1) offsets.add(new double[]
                        { 0, Float.parseFloat(vec[0]), 0 });
                        else if (vec.length == 3) offsets.add(new double[]
                        { Float.parseFloat(vec[0]), Float.parseFloat(vec[1]), Float.parseFloat(vec[2]) });
                        else PokecubeAPI.LOGGER.warn(
                                "Wrong number of numbers for offset, must be 1 or 3: " + entry + " got: " + vec.length);
                    }
                    if (!offsets.isEmpty())
                    {
                        entry.passengerOffsets = new double[offsets.size()][];
                        for (int i = 0; i < entry.passengerOffsets.length; i++)
                            entry.passengerOffsets[i] = offsets.get(i);
                    }
                }

                try
                {
                    PokedexEntryLoader.postIniStats(entry, stats);
                    if (xmlEntry.formeItems != null) entry._forme_items = xmlEntry.formeItems;

                    // Now handle dyable stuff
                    if (xmlEntry.dye != null) xmlEntry.dye.accept(entry);
                }
                catch (final Exception e)
                {
                    PokecubeAPI.LOGGER.error("Error with stats for " + entry, e);
                }
                try
                {
                    PokedexEntryLoader.parseSpawns(entry, stats);
                }
                catch (final Exception e)
                {
                    PokecubeAPI.LOGGER.error("Error with spawns for " + entry, e);
                }
                try
                {
                    PokedexEntryLoader.parseEvols(entry, stats.evolutions, true);
                }
                catch (final Exception e)
                {
                    PokecubeAPI.LOGGER.error("Error with evols for " + entry, e);
                }
            }
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error with " + xmlEntry + " init? " + init, e);
        }
        if (moves != null) PokedexEntryLoader.initMoves(entry, moves);
    }

    public static String getCompoundDatabaseJson(PokemobsJson source)
    {
        final List<XMLPokedexEntry> entries = Lists.newArrayList(source.pokemon);
        final PokemobsJson database = new PokemobsJson();
        database.pokemon = entries;
        database.pokemon.removeIf(value -> {
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
                        "Error with entry for {}, it is missing a Number for sorting! removing on write", value.name);
                return true;
            }
            return false;
        });
        database.pokemon.sort(PokedexEntryLoader.ENTRYSORTER);
        database.pokemon.replaceAll(t -> {
            try
            {
                return (XMLPokedexEntry) PokedexEntryLoader.getSerializableCopy(t.getClass(), t);
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                return t;
            }
        });
        final Map<String, XMLPokedexEntry> back = database.__map__;
        database.__map__ = null;
        final String json = JsonUtil.gson.toJson(database);
        database.__map__ = back;
        return json;
    }

    public static void writeCompoundDatabase(PokemobsJson source)
    {
        try
        {
            String json = getCompoundDatabaseJson(source);
            final FileOutputStream writer = new FileOutputStream(
                    new File(FMLPaths.CONFIGDIR.get().toFile(), "pokemobs.json"));
            writer.write(json.getBytes());
            writer.close();
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error outputing compound database", e);
        }
    }
}
