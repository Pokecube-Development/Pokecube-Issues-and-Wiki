package pokecube.core.database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.InteractionLogic;
import pokecube.core.database.PokedexEntry.MegaRule;
import pokecube.core.database.PokedexEntry.MovementType;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.core.database.PokedexEntryLoader.StatsNode.Stats;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.events.pokemob.SpawnEvent.FunctionVariance;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;

public class PokedexEntryLoader
{

    @XmlRootElement(name = "Action")
    public static class Action
    {
        @XmlAnyAttribute
        public Map<QName, String> values = Maps.newHashMap();
        @XmlElement(name = "tag")
        public String             tag;
        @XmlAttribute
        public String             lootTable;
        @XmlElement(name = "Drop")
        public List<Drop>         drops  = Lists.newArrayList();
    }

    @XmlRootElement(name = "BODY")
    public static class BodyNode
    {
        @XmlElement(name = "PART")
        public List<BodyPart> parts = Lists.newArrayList();
    }

    @XmlRootElement(name = "PART")
    public static class BodyPart
    {
        @XmlAttribute(name = "name")
        public String name;
        @XmlAttribute(name = "offset")
        public String offset;
        @XmlAttribute(name = "dimensions")
        public String dimensions;
    }

    @XmlRootElement(name = "Drop")
    public static class Drop
    {
        @XmlAnyAttribute
        public Map<QName, String> values = Maps.newHashMap();
        @XmlElement(name = "tag")
        public String             tag;
    }

    public static class Evolution
    {
        @XmlAttribute(name = "clear")
        public Boolean   clear;
        @XmlAttribute(name = "Name")
        public String    name;
        @XmlAttribute(name = "Level")
        public Integer   level;
        @XmlAttribute(name = "Priority")
        public Integer   priority;
        @XmlElement(name = "Location")
        public SpawnRule location;
        @XmlAttribute(name = "Animation")
        public String    animation;
        @XmlElement(name = "Key")
        public Key       item;
        @XmlElement(name = "PresetItem")
        public String    item_preset;
        @XmlAttribute(name = "Time")
        public String    time;
        @XmlAttribute(name = "Trade")
        public Boolean   trade;
        @XmlAttribute(name = "Rain")
        public Boolean   rain;
        @XmlAttribute(name = "Happy")
        public Boolean   happy;
        @XmlAttribute(name = "Sexe")
        public String    sexe;
        @XmlAttribute(name = "Move")
        public String    move;
        @XmlAttribute(name = "Chance")
        public Float     chance;
    }

    @XmlRootElement(name = "Interact")
    public static class Interact
    {
        @XmlAttribute
        public Boolean male       = true;
        @XmlAttribute
        public Boolean female     = true;
        @XmlAttribute
        public Integer cooldown   = 50;
        @XmlAttribute
        public Integer variance   = 100;
        @XmlAttribute
        public Integer baseHunger = 100;
        @XmlElement(name = "Key")
        public Key     key;
        @XmlElement(name = "Action")
        public Action  action;
    }

    @XmlRootElement(name = "Key")
    public static class Key
    {
        @XmlAnyAttribute
        public Map<QName, String> values = Maps.newHashMap();
        @XmlElement(name = "tag")
        public String             tag;
    }

    public static class MegaEvoRule implements MegaRule
    {
        ItemStack          stack;
        String             oreDict;
        String             moveName;
        String             ability;
        final PokedexEntry baseForme;

        public MegaEvoRule(final PokedexEntry baseForme)
        {
            this.stack = ItemStack.EMPTY;
            this.moveName = "";
            this.ability = "";
            this.baseForme = baseForme;
        }

        @Override
        public boolean shouldMegaEvolve(final IPokemob mobIn, final PokedexEntry entryTo)
        {
            boolean rightStack = true;
            boolean hasMove = true;
            boolean hasAbility = true;
            boolean rule = false;
            if (this.oreDict != null)
            {
                this.stack = PokecubeItems.getStack(this.oreDict);
                this.oreDict = null;
            }
            if (!this.stack.isEmpty())
            {
                rightStack = Tools.isSameStack(this.stack, mobIn.getHeldItem(), true);
                rule = true;
            }
            if (this.moveName != null && !this.moveName.isEmpty())
            {
                hasMove = Tools.hasMove(this.moveName, mobIn);
                rule = true;
            }
            if (this.ability != null && !this.ability.isEmpty())
            {
                hasAbility = AbilityManager.hasAbility(this.ability, mobIn);
                rule = true;
            }
            if (hasAbility && mobIn.getAbility() != null) hasAbility = mobIn.getAbility().canChange(mobIn, entryTo);
            return rule && hasMove && rightStack && hasAbility;
        }
    }

    @XmlRootElement(name = "MOVES")
    public static class Moves
    {
        @XmlRootElement(name = "LVLUP")
        public static class LvlUp
        {
            @XmlAnyAttribute
            public Map<QName, String> values = Maps.newHashMap();
        }

        @XmlRootElement(name = "MISC")
        public static class Misc
        {
            @XmlAttribute(name = "moves")
            public String moves;

            @Override
            public String toString()
            {
                return this.moves;
            }
        }

        @XmlElement(name = "LVLUP")
        public LvlUp lvlupMoves;

        @XmlElement(name = "MISC")
        public Misc misc;

        @XmlElement(name = "EVOMOVES")
        public String evolutionMoves;
    }

    @XmlRootElement(name = "Spawn")
    public static class SpawnRule
    {
        @XmlAnyAttribute
        public Map<QName, String> values = Maps.newHashMap();

        @Override
        public String toString()
        {
            return this.values + "";
        }
    }

    @XmlRootElement(name = "STATS")
    public static class StatsNode
    {
        public static class Stats
        {
            @XmlAnyAttribute
            public Map<QName, String> values = Maps.newHashMap();
        }

        // Evolution stuff
        @XmlElement(name = "Evolution")
        public List<Evolution> evolutions = Lists.newArrayList();

        // Species and food
        @XmlElement(name = "SPECIES")
        public String species;
        @XmlElement(name = "PREY")
        public String prey;
        @XmlElement(name = "FOODMATERIAL")
        public String foodMat;

        @XmlElement(name = "SPECIALEGGSPECIESRULES")
        public String specialEggRules;
        // Drops and items
        @XmlElement(name = "Drop")
        public List<Drop> drops = Lists.newArrayList();
        @XmlElement(name = "Held")
        public List<Drop> held  = Lists.newArrayList();
        @XmlElement(name = "lootTable")
        public String     lootTable;
        @XmlElement(name = "heldTable")
        public String     heldTable;
        // Spawn Rules
        @XmlAttribute
        public Boolean         overwrite;
        @XmlElement(name = "Spawn")
        public List<SpawnRule> spawnRules = Lists.newArrayList();
        // STATS
        @XmlElement(name = "BASESTATS")
        public Stats   stats;
        @XmlElement(name = "EVYIELD")
        public Stats   evs;
        @XmlElement(name = "SIZES")
        public Stats   sizes;
        @XmlElement(name = "TYPE")
        public Stats   types;
        @XmlElement(name = "ABILITY")
        public Stats   abilities;
        @XmlElement(name = "MASSKG")
        public Float   mass           = -1f;
        @XmlElement(name = "CAPTURERATE")
        public Integer captureRate    = -1;
        @XmlElement(name = "EXPYIELD")
        public Integer baseExp        = -1;
        @XmlElement(name = "BASEFRIENDSHIP")
        public Integer baseFriendship = 70;
        @XmlElement(name = "EXPERIENCEMODE")
        public String  expMode;

        @XmlElement(name = "GENDERRATIO")
        public Integer genderRatio = -1;
        // MISC
        @XmlElement(name = "LOGIC")
        public Stats logics;
        @XmlElement(name = "FORMEITEMS")
        public Stats formeItems;

        // Old mega rules
        @XmlElement(name = "MEGARULES")
        public Stats megaRules_old;
        // New Mega rules
        @XmlElement(name = "MegaRules")
        public List<XMLMegaRule> megaRules = Lists.newArrayList();

        @XmlElement(name = "MOVEMENTTYPE")
        public String         movementType;
        @XmlElement(name = "Interact")
        public List<Interact> interactions = Lists.newArrayList();
        @XmlElement(name = "SHADOWREPLACEMENTS")
        public String         shadowReplacements;
        @XmlElement(name = "HATEDMATERIALRULES")
        public String         hatedMaterials;

        @XmlElement(name = "ACTIVETIMES")
        public String activeTimes;

        @Override
        public String toString()
        {
            return this.spawnRules + "";
        }
    }

    @XmlRootElement(name = "Document")
    public static class XMLDatabase
    {
        @XmlElement(name = "Pokemon")
        public List<XMLPokedexEntry> pokemon = Lists.newArrayList();

        public Map<String, XMLPokedexEntry> map = Maps.newHashMap();

        public void addEntry(final XMLPokedexEntry toAdd)
        {
            if (this.map.containsKey(toAdd.name)) this.pokemon.remove(this.map.remove(toAdd.name));
            this.pokemon.add(toAdd);
            Collections.sort(this.pokemon, PokedexEntryLoader.ENTRYSORTER);
        }

        public void addOverrideEntry(final XMLPokedexEntry entry, final boolean overwrite)
        {
            for (final XMLPokedexEntry e : this.pokemon)
                if (e.name.equals(entry.name))
                {
                    if (overwrite)
                    {
                        this.pokemon.remove(e);
                        this.map.put(entry.name, entry);
                        this.pokemon.add(entry);
                        entry.mergeMissingFrom(e);
                        return;
                    }
                    else e.mergeMissingFrom(entry);
                    return;
                }
            this.pokemon.add(entry);
            this.map.put(entry.name, entry);
        }

        public void init()
        {
            for (final XMLPokedexEntry e : this.pokemon)
                this.map.put(e.name, e);
        }
    }

    @XmlRootElement(name = "MegaRule")
    public static class XMLMegaRule
    {
        @XmlAttribute(name = "Name")
        public String name;
        @XmlAttribute(name = "Preset")
        public String preset;
        @XmlAttribute(name = "Move")
        public String move;
        @XmlAttribute(name = "Ability")
        public String ability;
        @XmlElement(name = "Key")
        public Key    item;
        @XmlElement(name = "Key_Template")
        public String item_preset;
    }

    @XmlRootElement(name = "Pokemon")
    public static class XMLPokedexEntry
    {
        @XmlAttribute
        public String    name;
        @XmlAttribute
        public Integer   number;
        @XmlAttribute
        public String    special;
        @XmlAttribute
        public Boolean   base       = false;
        @XmlAttribute
        public Boolean   breed      = true;
        @XmlAttribute
        public Boolean   dummy      = false;
        @XmlAttribute
        public Boolean   starter    = false;
        @XmlAttribute
        public Boolean   ridable    = true;
        @XmlAttribute
        public Boolean   legend     = false;
        @XmlAttribute
        public Boolean   hasShiny   = true;
        @XmlAttribute
        public String    gender     = "";
        @XmlAttribute
        public String    genderBase = "";
        @XmlAttribute
        public String    sound      = null;
        @XmlElement(name = "STATS")
        public StatsNode stats;
        @XmlElement(name = "MOVES")
        public Moves     moves;
        @XmlElement(name = "BODY")
        public BodyNode  body;

        void mergeMissingFrom(final XMLPokedexEntry other)
        {
            if (this.moves == null && other.moves != null) this.moves = other.moves;
            else if (other.moves != null)
            {
                if (this.moves.lvlupMoves == null) this.moves.lvlupMoves = other.moves.lvlupMoves;
                if (this.moves.misc == null) this.moves.misc = other.moves.misc;
                if (this.moves.evolutionMoves != null) this.moves.evolutionMoves = other.moves.evolutionMoves;
            }
            if (this.body == null && other.body != null) this.body = other.body;
            if (this.stats == null && other.stats != null) this.stats = other.stats;
            else if (other.stats != null) // Copy everything which is missing
                for (final Field f : StatsNode.class.getDeclaredFields())
                try
                {
                final Object ours = f.get(this.stats);
                final Object theirs = f.get(other.stats);
                boolean isNumber = !(ours instanceof String || ours instanceof Stats);
                if (isNumber) if (ours instanceof Float) isNumber = (float) ours == -1;
                else if (ours instanceof Integer) isNumber = (int) ours == -1;
                if (ours == null) f.set(this.stats, theirs);
                else if (isNumber) f.set(this.stats, theirs);
                }
                catch (final Exception e)
                {
                e.printStackTrace();
                }
        }

        @Override
        public String toString()
        {
            return this.name + " " + this.number + " " + this.stats + " " + this.moves;
        }
    }

    public static final Gson gson;

    public static final Comparator<XMLPokedexEntry> ENTRYSORTER = (o1, o2) ->
    {
        int diff = o1.number - o2.number;
        if (diff == 0) if (o1.base && !o2.base) diff = -1;
        else if (o2.base && !o1.base) diff = 1;
        return diff;
    };

    public static XMLPokedexEntry missingno = new XMLPokedexEntry();

    static
    {
        gson = new GsonBuilder().registerTypeAdapter(QName.class, new TypeAdapter<QName>()
        {
            @Override
            public QName read(final JsonReader in) throws IOException
            {
                return new QName(in.nextString());
            }

            @Override
            public void write(final JsonWriter out, final QName value) throws IOException
            {
                out.value(value.toString());
            }
        }).setPrettyPrinting().create();
        PokedexEntryLoader.missingno.stats = new StatsNode();
    }

    public static XMLDatabase database;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object getSerializableCopy(final Class<?> type, final Object original) throws InstantiationException,
            IllegalAccessException
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
            copy = type.newInstance();
        }
        catch (final Exception e1)
        {
            copy = original;
        }
        if (copy == original || copy != null && copy.equals(original)) return copy;
        Object value;
        Object defaultvalue;
        for (final Field field : fields)
            try
            {
                if (Modifier.isFinal(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                field.setAccessible(true);
                value = field.get(original);
                defaultvalue = field.get(copy);
                if (value == null) continue;
                if (value.getClass().isPrimitive()) field.set(copy, value);
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
                            final List args = (List) value;
                            final ListIterator iter = args.listIterator();
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
    public static ItemStack getStackFromDrop(final Drop d)
    {
        final Map<QName, String> values = d.values;
        if (d.tag != null)
        {
            final QName name = new QName("tag");
            values.put(name, d.tag);
        }
        return Tools.getStack(d.values);
    }

    /**
     * This is safe to run before tags are loaded.
     *
     * @param spawnData
     * @param rule
     */
    public static void handleAddSpawn(final SpawnData spawnData, final SpawnRule rule)
    {
        final SpawnEntry spawnEntry = new SpawnEntry();
        String val;
        if ((val = rule.values.get(new QName("min"))) != null) spawnEntry.min = Integer.parseInt(val);
        if ((val = rule.values.get(new QName("max"))) != null) spawnEntry.max = Integer.parseInt(val);
        if ((val = rule.values.get(new QName("rate"))) != null) spawnEntry.rate = Float.parseFloat(val);
        if ((val = rule.values.get(new QName("level"))) != null) spawnEntry.level = Integer.parseInt(val);
        if ((val = rule.values.get(new QName("variance"))) != null) spawnEntry.variance = new FunctionVariance(val);
        final SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(rule);
        spawnData.matchers.put(matcher, spawnEntry);
    }

    public static XMLDatabase initDatabase(final InputStream stream, final boolean json) throws Exception
    {
        XMLDatabase newDatabase = null;
        if (PokedexEntryLoader.database == null)
        {
            // This is the first database file, so load it in as the instance.
            PokedexEntryLoader.database = newDatabase = PokedexEntryLoader.loadDatabase(stream, json);
            PokedexEntryLoader.database.init();
        }
        else
        {
            // This is a new database file, so merge it into the existing ones
            newDatabase = PokedexEntryLoader.loadDatabase(stream, json);
            if (newDatabase != null) for (final XMLPokedexEntry e : newDatabase.pokemon)
            {
                final XMLPokedexEntry old = PokedexEntryLoader.database.map.get(e.name);
                if (old != null) PokedexEntryLoader.mergeNonDefaults(PokedexEntryLoader.missingno, e, old);
                else try
                {
                    PokedexEntryLoader.database.addEntry(e);
                }
                catch (final Exception e1)
                {
                    PokecubeCore.LOGGER.error("Error with " + e.name, e1);
                }
            }
            else throw new NullPointerException("Contains no database");
        }

        // Make all of the pokedex entries added by the database.
        for (final XMLPokedexEntry entry : PokedexEntryLoader.database.pokemon)
            if (Database.getEntry(entry.name) == null)
            {
                final PokedexEntry pentry = new PokedexEntry(entry.number, entry.name);
                pentry.dummy = entry.dummy;
                if (entry.base)
                {
                    pentry.base = entry.base;
                    Database.baseFormes.put(entry.number, pentry);
                    Database.addEntry(pentry);
                }
            }
        // Init all of the evolutions, this is so that the relations between the
        // mobs can be known later.
        for (final XMLPokedexEntry entry : PokedexEntryLoader.database.pokemon)
            PokedexEntryLoader.preCheckEvolutions(entry);
        return newDatabase;
    }

    public static XMLDatabase initDatabase(final ResourceLocation file) throws Exception
    {
        if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Initializing Database: " + file);
        try
        {
            return PokedexEntryLoader.initDatabase(Database.resourceManager.getResource(file).getInputStream(), true);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with " + file, e);
            throw new RuntimeException(e);
        }
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
            for (final String s : misc)
                allMoves.add(Database.convertMoveName(s));
        }
        if (xmlMoves.lvlupMoves != null) for (final QName key : xmlMoves.lvlupMoves.values.keySet())
        {
            final String keyName = key.toString();
            final String[] values = xmlMoves.lvlupMoves.values.get(key).split(",");
            ArrayList<String> moves;
            lvlUpMoves.put(Integer.parseInt(keyName.replace("lvl_", "")), moves = new ArrayList<>());
            moves:
            for (String s : values)
            {
                s = Database.convertMoveName(s);
                moves.add(s);
                for (final String s1 : allMoves)
                    if (s1.equalsIgnoreCase(s)) continue moves;
                allMoves.add(Database.convertMoveName(s));
            }
        }

        if (allMoves.isEmpty()) allMoves = null;
        if (lvlUpMoves.isEmpty()) lvlUpMoves = null;
        entry.addMoves(allMoves, lvlUpMoves);

        if (xmlMoves.evolutionMoves != null)
        {
            final String[] moves = xmlMoves.evolutionMoves.split(",");
            for (final String s : moves)
                entry.getEvolutionMoves().add(s);
        }
    }

    /**
     * This can be run at any early loading stage.
     *
     * @param entry
     * @param xmlStats
     */
    private static void initStats(final PokedexEntry entry, final StatsNode xmlStats)
    {
        final int[] stats = new int[6];
        final byte[] evs = new byte[6];
        boolean stat = false, ev = false;
        if (xmlStats.stats != null)
        {
            final Map<QName, String> values = xmlStats.stats.values;
            for (final QName key : values.keySet())
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
        if (xmlStats.evs != null)
        {
            final Map<QName, String> values = xmlStats.evs.values;
            for (final QName key : values.keySet())
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
        if (xmlStats.types != null)
        {
            final Map<QName, String> values = xmlStats.types.values;
            for (final QName key : values.keySet())
            {
                final String keyString = key.toString();
                final String value = values.get(key);
                if (keyString.equals("type1")) entry.type1 = PokeType.getType(value);
                if (keyString.equals("type2")) entry.type2 = PokeType.getType(value);
            }
        }
        if (xmlStats.sizes != null)
        {
            final Map<QName, String> values = xmlStats.sizes.values;
            entry.length = -1;
            entry.width = -1;
            for (final QName key : values.keySet())
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
        if (xmlStats.abilities != null)
        {
            final Map<QName, String> values = xmlStats.abilities.values;
            for (final QName key : values.keySet())
            {
                final String keyString = key.toString();
                final String value = values.get(key);
                if (keyString.equals("hidden"))
                {
                    final String[] vars = value.split(",");
                    for (int i = 0; i < vars.length; i++)
                        if (!entry.abilitiesHidden.contains(vars[i].trim())) entry.abilitiesHidden.add(vars[i].trim());
                }
                if (keyString.equals("normal"))
                {
                    final String[] vars = value.split(",");
                    for (int i = 0; i < vars.length; i++)
                        if (!entry.abilities.contains(vars[i].trim())) entry.abilities.add(vars[i].trim());
                    if (entry.abilities.size() == 1) entry.abilities.add(entry.abilities.get(0));
                }
            }
        }
        if (xmlStats.captureRate != PokedexEntryLoader.missingno.stats.captureRate)
            entry.catchRate = xmlStats.captureRate;
        if (xmlStats.baseExp != PokedexEntryLoader.missingno.stats.baseExp) entry.baseXP = xmlStats.baseExp;
        if (xmlStats.baseFriendship != PokedexEntryLoader.missingno.stats.baseFriendship)
            entry.baseHappiness = xmlStats.baseFriendship;
        if (xmlStats.genderRatio != PokedexEntryLoader.missingno.stats.genderRatio)
            entry.sexeRatio = xmlStats.genderRatio;
        if (xmlStats.mass != PokedexEntryLoader.missingno.stats.mass) entry.mass = xmlStats.mass;
        if (entry.ridable != PokedexEntryLoader.missingno.ridable) entry.ridable = PokedexEntryLoader.missingno.ridable;
        if (xmlStats.movementType != null)
        {
            final String[] strings = xmlStats.movementType.trim().split(":");
            entry.mobType = MovementType.getType(strings[0]);
            if (strings.length > 1) entry.preferedHeight = Double.parseDouble(strings[1]);
        }
        if (xmlStats.species != null) entry.species = xmlStats.species.trim().split(" ");
        if (xmlStats.prey != null) entry.food = xmlStats.prey.trim().split(" ");
    }

    public static XMLDatabase loadDatabase(final InputStream stream, final boolean json) throws Exception
    {
        XMLDatabase database = null;
        final InputStreamReader reader = new InputStreamReader(stream);
        if (json) database = PokedexEntryLoader.gson.fromJson(reader, XMLDatabase.class);
        else
        {
            final JAXBContext jaxbContext = JAXBContext.newInstance(XMLDatabase.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            database = (XMLDatabase) unmarshaller.unmarshal(reader);
        }
        reader.close();
        return database;
    }

    public static void makeEntries(final boolean create)
    {
        final List<XMLPokedexEntry> entries = Lists.newArrayList(PokedexEntryLoader.database.pokemon);

        Collections.sort(entries, PokedexEntryLoader.ENTRYSORTER);

        for (final XMLPokedexEntry xmlEntry : entries)
        {
            String name = xmlEntry.name;
            final int number = xmlEntry.number;
            if (create) if (xmlEntry.gender.isEmpty())
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
            else
            {
                final byte gender = xmlEntry.gender.equalsIgnoreCase("male") ? IPokemob.MALE
                        : xmlEntry.gender.equalsIgnoreCase("female") ? IPokemob.FEMALE : -1;
                if (gender != -1)
                {
                    final PokedexEntry entry = Database.getEntry(xmlEntry.genderBase);
                    name = name.replaceAll("([\\W])", "");
                    if (entry.getTrimmedName().equals(name)) name = null;
                    entry.createGenderForme(gender, name);
                }
                else throw new IllegalArgumentException("Error in gender for " + xmlEntry.name);
            }
            PokedexEntryLoader.updateEntry(xmlEntry, create);
        }
    }

    public static void mergeNonDefaults(final Object defaults, final Object outOf, final Object inTo)
    {
        if (outOf.getClass() != inTo.getClass()) throw new IllegalArgumentException(
                "To and From must be of the same class!");
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
        for (final Field field : fields)
            try
            {
                if (Modifier.isFinal(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
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

                final boolean outIsDefault = valueOut == valueDefault || valueDefault != null && valueDefault.equals(
                        valueOut);
                if (!outIsDefault) if (valueOut instanceof String) field.set(inTo, valueOut);
                else if (valueOut instanceof Object[]) field.set(inTo, ((Object[]) valueOut).clone());
                else if (valueOut instanceof Map) field.set(inTo, valueOut);
                else if (valueOut instanceof Collection) field.set(inTo, valueOut);
                else try
                {
                    valueDefault = valueOut.getClass().newInstance();
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
    private static void parseEvols(final PokedexEntry entry, final StatsNode xmlStats, final boolean error)
    {
        if (xmlStats.specialEggRules != null)
        {
            final String[] matedata = xmlStats.specialEggRules.split(";");
            mates:
            for (final String s1 : matedata)
            {
                final String[] args = s1.split(":");
                PokedexEntry father;
                int num = -1;
                final String name = "";
                try
                {
                    father = Database.getEntry(num = Integer.parseInt(args[0]));
                }
                catch (final NumberFormatException e)
                {
                    father = Database.getEntry(args[0]);
                }
                if (father == null && (num == 0 || name.equalsIgnoreCase("missingno"))) father = Database.missingno;
                if (father == null)
                {
                    PokecubeCore.LOGGER.error("Error with Father for Children for " + entry);
                    break mates;
                }
                final String[] args1 = args[1].split("`");
                final PokedexEntry[] childNbs = new PokedexEntry[args1.length];
                for (int i = 0; i < args1.length; i++)
                {
                    try
                    {
                        childNbs[i] = Database.getEntry(Integer.parseInt(args1[i]));
                    }
                    catch (final NumberFormatException e)
                    {
                        childNbs[i] = Database.getEntry(args1[i]);
                    }
                    if (childNbs[i] == null)
                    {
                        PokecubeCore.LOGGER.error("Error with Children for " + entry + " " + args1[i]);
                        break mates;
                    }
                }
                entry.childNumbers.put(father, childNbs);
            }
        }
        if (xmlStats.evolutions != null && !xmlStats.evolutions.isEmpty())
            for (final Evolution evol : xmlStats.evolutions)
            {
            final String name = evol.name;
            final PokedexEntry evolEntry = Database.getEntry(name);
            EvolutionData data = null;
            for (final EvolutionData d : entry.evolutions)
            if (d.evolution == evolEntry)
            {
            data = d;
            if (evol.clear != null && evol.clear)
            {
            entry.evolutions.remove(d);
            PokecubeCore.LOGGER.info("Replacing evolution for " + entry + " -> " + evolEntry);
            }
            break;
            }
            if (data == null || evol.clear != null && evol.clear)
            {
            data = new EvolutionData(evolEntry);
            data.data = evol;
            data.preEvolution = entry;
            for (final EvolutionData existing : entry.evolutions)
            if (existing.evolution == evolEntry) break;
            entry.addEvolution(data);
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
        SpawnData spawnData = entry.getSpawnData();
        if (spawnData == null || overwrite) spawnData = new SpawnData(entry);
        for (final SpawnRule rule : xmlStats.spawnRules)
        {
            PokedexEntryLoader.handleAddSpawn(spawnData, rule);
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Handling Spawns for " + entry);
        }
        entry.setSpawnData(spawnData);
        if (!Database.spawnables.contains(entry)) Database.spawnables.add(entry);

    }

    /**
     * This can be called whenever.
     *
     * @param special
     * @param entry
     */
    private static void parseSpecial(final String special, final PokedexEntry entry)
    {
        if (special.equals("shadow"))
        {
            entry.isShadowForme = true;
            if (entry.getBaseForme() != null) entry.getBaseForme().shadowForme = entry;
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
        if (xmlStats.lootTable != null && !xmlStats.lootTable.isEmpty()) entry.lootTable = new ResourceLocation(
                xmlStats.lootTable);
        // Held
        if (xmlStats.heldTable != null && !xmlStats.heldTable.isEmpty()) entry.heldTable = new ResourceLocation(
                xmlStats.heldTable);
        // Logics
        if (xmlStats.logics != null)
        {
            entry.shouldFly = entry.isType(PokeType.getType("flying"));
            final Map<QName, String> values = xmlStats.logics.values;
            for (final QName key : values.keySet())
            {
                final String keyString = key.toString();
                final String value = values.get(key);
                if (keyString.equals("shoulder")) entry.canSitShoulder = Boolean.parseBoolean(value);
                if (keyString.equals("fly")) entry.shouldFly = Boolean.parseBoolean(value);
                if (keyString.equals("dive")) entry.shouldDive = Boolean.parseBoolean(value);
                if (keyString.equals("surf")) entry.shouldSurf = Boolean.parseBoolean(value);
                if (keyString.equals("stationary")) entry.isStationary = Boolean.parseBoolean(value);
                if (keyString.equals("dye"))
                {
                    String[] args = value.split("#");
                    entry.dyeable = Boolean.parseBoolean(args[0]);
                    if (args.length > 1)
                    {
                        String defaultSpecial = args[1];
                        try
                        {
                            entry.defaultSpecial = Integer.parseInt(defaultSpecial);
                        }
                        catch (final NumberFormatException e)
                        {
                            for (final DyeColor dye : DyeColor.values())
                                if (dye.name().equals(defaultSpecial) || dye.getName().equals(defaultSpecial) || dye
                                        .getTranslationKey().equals(defaultSpecial))
                                {
                                    entry.defaultSpecial = dye.getId();
                                    break;
                                }
                        }
                        if (args.length > 2)
                        {
                            defaultSpecial = args[2];
                            try
                            {
                                entry.defaultSpecial = Integer.parseInt(defaultSpecial);
                            }
                            catch (final NumberFormatException e)
                            {
                                for (final DyeColor dye : DyeColor.values())
                                    if (dye.name().equals(defaultSpecial) || dye.getName().equals(defaultSpecial) || dye
                                            .getTranslationKey().equals(defaultSpecial))
                                    {
                                        entry.defaultSpecial = dye.getId();
                                        break;
                                    }
                            }
                            if (args.length > 3)
                            {
                                defaultSpecial = args[3];
                                args = defaultSpecial.split(",");
                                for (final String s : args)
                                    for (final DyeColor dye : DyeColor.values())
                                        if (dye.name().equals(s) || dye.getName().equals(s) || dye.getTranslationKey()
                                                .equals(s))
                                        {
                                            entry.validDyes.add(dye);
                                            break;
                                        }

                            }
                        }
                    }
                }
            }
        }
        try
        {
            if (xmlStats.expMode != null) entry.evolutionMode = Tools.getType(xmlStats.expMode);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with expmode" + entry, e);
        }
        if (xmlStats.shadowReplacements != null)
        {
            final String[] replaces = xmlStats.shadowReplacements.split(":");
            for (String s1 : replaces)
            {
                s1 = s1.toLowerCase(java.util.Locale.ENGLISH).trim().replace(" ", "");
                if (s1.isEmpty()) continue;

                if (Database.mobReplacements.containsKey(s1)) Database.mobReplacements.get(s1).add(entry);
                else
                {
                    Database.mobReplacements.put(s1, new ArrayList<PokedexEntry>());
                    Database.mobReplacements.get(s1).add(entry);
                }
            }
        }
        if (xmlStats.foodMat != null)
        {
            final String[] foods = xmlStats.foodMat.split(" ");
            entry.foods = new boolean[] { false, false, false, false, false, true, false };
            for (final String s1 : foods)
                if (s1.equalsIgnoreCase("light"))
                {
                    entry.activeTimes.add(PokedexEntry.day);
                    entry.foods[0] = true;
                }
                else if (s1.equalsIgnoreCase("rock")) entry.foods[1] = true;
                else if (s1.equalsIgnoreCase("electricity")) entry.foods[2] = true;
                else if (s1.equalsIgnoreCase("grass")) entry.foods[3] = true;
                else if (s1.equalsIgnoreCase("water")) entry.foods[6] = true;
                else if (s1.equalsIgnoreCase("none")) entry.foods[4] = true;
        }
        if (entry.isType(PokeType.getType("ghost"))) entry.foods[4] = true;

        if (xmlStats.activeTimes != null)
        {
            final String[] times = xmlStats.activeTimes.split(" ");
            entry.activeTimes.clear();
            for (final String s1 : times)
                if (s1.equalsIgnoreCase("day")) entry.activeTimes.add(PokedexEntry.day);
                else if (s1.equalsIgnoreCase("night")) entry.activeTimes.add(PokedexEntry.night);
                else if (s1.equalsIgnoreCase("dusk")) entry.activeTimes.add(PokedexEntry.dusk);
                else if (s1.equalsIgnoreCase("dawn")) entry.activeTimes.add(PokedexEntry.dawn);
        }
        if (!xmlStats.interactions.isEmpty()) InteractionLogic.initForEntry(entry, xmlStats.interactions);

        if (xmlStats.hatedMaterials != null) entry.hatedMaterial = xmlStats.hatedMaterials.split(":");

        if (xmlStats.formeItems != null)
        {
            final Map<QName, String> values = xmlStats.formeItems.values;
            entry.formeItems.clear();
            for (final QName key : values.keySet())
            {
                final String keyString = key.toString();
                final String value = values.get(key);
                if (keyString.equals("forme"))
                {
                    final String[] args = value.split(",");
                    for (final String s : args)
                    {
                        String forme = "";
                        String item = "";
                        final String[] args2 = s.split(":");
                        for (final String s1 : args2)
                        {
                            final String arg1 = s1.trim().substring(0, 1);
                            final String arg2 = s1.trim().substring(1);
                            if (arg1.equals("N")) forme = arg2;
                            else if (arg1.equals("I")) item = arg2.replace("`", ":");
                        }

                        final PokedexEntry formeEntry = Database.getEntry(forme);
                        if (!forme.isEmpty() && formeEntry != null)
                        {
                            final ItemStack stack = PokecubeItems.getStack(item, false);
                            // TODO see if needs to add to holdables
                            entry.formeItems.put(stack, formeEntry);
                        }
                    }
                }
            }
        }

        if (xmlStats.megaRules != null && !xmlStats.megaRules.isEmpty())
            for (final XMLMegaRule rule : xmlStats.megaRules)
            {

            String forme = rule.name != null ? rule.name : null;
            if (forme == null) if (rule.preset != null) if (rule.preset.startsWith("Mega"))
            {
            forme = entry.getTrimmedName() + rule.preset.toLowerCase(Locale.ENGLISH);
            if (rule.item_preset == null) rule.item_preset = entry.getTrimmedName() + rule.preset.toLowerCase(Locale.ENGLISH);
            }
            final String move = rule.move;
            final String ability = rule.ability;
            final String item_preset = rule.item_preset;

            if (forme == null)
            {
            PokecubeCore.LOGGER.info("Error with mega evolution for " + entry + " rule: preset=" + rule.name + " name=" + rule.name);
            continue;
            }

            final PokedexEntry formeEntry = Database.getEntry(forme);
            if (!forme.isEmpty() && formeEntry != null)
            {
            ItemStack stack = ItemStack.EMPTY;
            if (item_preset != null && !item_preset.isEmpty())
            {
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info(forme + " " + item_preset);
            stack = item_preset.isEmpty() ? ItemStack.EMPTY : PokecubeItems.getStack(item_preset, false);
            }
            else if (rule.item != null) stack = Tools.getStack(rule.item.values);
            if (rule.item != null) if (PokecubeMod.debug) PokecubeCore.LOGGER.info(stack + " " + rule.item.values);
            if ((move == null || move.isEmpty()) && stack.isEmpty() && (ability == null || ability.isEmpty()))
            {
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Skipping Mega: " + entry + " -> " + formeEntry + " as it has no conditions, or conditions cannot be met.");
            continue;
            }
            final MegaEvoRule mrule = new MegaEvoRule(entry);
            if (item_preset != null && !item_preset.isEmpty()) mrule.oreDict = item_preset;
            if (ability != null) mrule.ability = ability;
            if (move != null) mrule.moveName = move;
            if (!stack.isEmpty())
            {
            mrule.stack = stack;
            if (!PokecubeItems.isValidHeldItem(stack)) PokecubeItems.addToHoldables(stack);
            }
            formeEntry.isMega = true;
            entry.megaRules.put(formeEntry, mrule);
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Added Mega: " + entry + " -> " + formeEntry);
            }
            }

        // Add gigantamax things as "megas"
        if (entry.getName().contains("Gigantamax")) entry.isMega = true;

    }

    public static void postInit()
    {
        try
        {
            PokedexEntry.InteractionLogic.initDefaults();
            PokedexEntryLoader.makeEntries(false);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with postinit of loading pokedex entries", e);
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
            PokedexEntryLoader.parseEvols(entry, stats, false);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with " + xmlEntry + " entry? " + entry, e);
        }

    }

    public static void updateEntry(final XMLPokedexEntry xmlEntry, final boolean init)
    {
        final String name = xmlEntry.name;
        final PokedexEntry entry = Database.getEntry(name);
        if (xmlEntry.sound != null) entry.customSound = xmlEntry.sound;
        final StatsNode stats = xmlEntry.stats;
        final Moves moves = xmlEntry.moves;
        if (stats != null) try
        {
            PokedexEntryLoader.initStats(entry, stats);
            if (!init)
            {
                entry.breeds = xmlEntry.breed;
                entry.isStarter = xmlEntry.starter;
                entry.ridable = xmlEntry.ridable;
                entry.legendary = xmlEntry.legend;
                entry.hasShiny = xmlEntry.hasShiny;
                try
                {
                    PokedexEntryLoader.postIniStats(entry, stats);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error with stats for " + entry, e);
                }
                try
                {
                    PokedexEntryLoader.parseSpawns(entry, stats);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error with spawns for " + entry, e);
                }
                try
                {
                    PokedexEntryLoader.parseEvols(entry, stats, true);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error with evols for " + entry, e);
                }
                if (xmlEntry.special != null) try
                {
                    PokedexEntryLoader.parseSpecial(xmlEntry.special, entry);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error with special for " + entry, e);
                }
            }
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with " + xmlEntry + " init? " + init, e);
        }
        if (moves != null) PokedexEntryLoader.initMoves(entry, moves);
    }

    public static void writeCompoundDatabase()
    {
        try
        {
            final List<XMLPokedexEntry> entries = Lists.newArrayList(PokedexEntryLoader.database.pokemon);
            final XMLDatabase database = new XMLDatabase();
            database.pokemon = entries;
            database.pokemon.sort(PokedexEntryLoader.ENTRYSORTER);
            database.pokemon.replaceAll(t ->
            {
                try
                {
                    return (XMLPokedexEntry) PokedexEntryLoader.getSerializableCopy(t.getClass(), t);
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                    return t;
                }
            });
            final Map<String, XMLPokedexEntry> back = database.map;
            database.map = null;
            final String json = PokedexEntryLoader.gson.toJson(database);
            database.map = back;
            final FileWriter writer = new FileWriter(new File(FMLPaths.CONFIGDIR.get().toFile(), "pokemobs.json"));
            writer.append(json);
            writer.close();
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error outputing compound database", e);
        }
    }
}
