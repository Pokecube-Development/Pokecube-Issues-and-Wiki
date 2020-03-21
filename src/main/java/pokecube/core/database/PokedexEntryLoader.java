package pokecube.core.database;

import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.ClassUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.core.common.ThutCore;

public class PokedexEntryLoader
{

    public static class Action
    {
        public Map<QName, String> values = Maps.newHashMap();
        public String             tag;
        public String             lootTable;
        public List<Drop>         drops  = Lists.newArrayList();
    }

    public static class BodyNode
    {
        public List<BodyPart> parts = Lists.newArrayList();
    }

    @XmlRootElement(name = "PART")
    public static class BodyPart
    {
        public String name;
        public String offset;
        public String dimensions;
    }

    @XmlRootElement(name = "Drop")
    public static class Drop
    {
        @XmlAnyAttribute
        public Map<QName, String> values = Maps.newHashMap();
        @XmlElement(name = "tag")
        public String             tag;
        @XmlElement(name = "id")
        public String             id;

        public Map<QName, String> getValues()
        {
            if (this.values == null) this.values = Maps.newHashMap();
            if (this.tag != null)
            {
                final QName name = new QName("tag");
                this.values.put(name, this.tag);
            }
            if (this.id != null)
            {
                final QName name = new QName("id");
                this.values.put(name, this.id);
            }
            return this.values;
        }
    }

    public static class DefaultFormeHolder
    {
        public static class TexColours
        {
            public String material = "";
            public float  red      = 1;
            public float  green    = 1;
            public float  blue     = 1;
            public float  alpha    = 1;
        }

        // These three allow specific models/textures for evos
        public String key   = null;
        public String tex   = null;
        public String model = null;
        public String anim  = null;

        public String parent = null;

        public List<TexColours> colours = Lists.newArrayList();
        public String[]         hidden  = {};

        public Map<String, TexColours> _colourMap_ = Maps.newHashMap();
        public Set<String>             _hide_      = Sets.newHashSet();

        public FormeHolder getForme(final PokedexEntry baseEntry)
        {
            if (this.key != null)
            {
                final ResourceLocation key = PokecubeItems.toPokecubeResource(this.key);
                if (Database.formeHolders.containsKey(key)) return Database.formeHolders.get(key);

                parent_check:
                if (this.parent != null)
                {
                    final ResourceLocation pkey = PokecubeItems.toPokecubeResource(this.parent);
                    final FormeHolder parent = Database.formeHolders.get(pkey);
                    if (parent == null || parent.loaded_from == null)
                    {
                        PokecubeCore.LOGGER.error(
                                "Error loading parent {} for {}, it needs to be registered earlier in the file!",
                                this.parent, this.key);
                        break parent_check;
                    }
                    final DefaultFormeHolder p = parent.loaded_from;
                    if (p.tex != null && this.tex == null) this.tex = p.tex;
                    if (p.model != null && this.model == null) this.model = p.model;
                    if (p.anim != null && this.anim == null) this.anim = p.anim;
                    if (p.hidden != null) if (p.hidden.length > 0)
                    {
                        final List<String> ours = this.hidden == null ? Lists.newArrayList()
                                : Lists.newArrayList(this.hidden);
                        for (final String s : p.hidden)
                            ours.add(s);
                        this.hidden = ours.toArray(new String[0]);
                    }
                    this.colours.addAll(p.colours);
                }
                if (this.hidden != null) for (final String element : this.hidden)
                {
                    final String value = ThutCore.trim(element);
                    this._hide_.add(value);
                }
                for (final TexColours c : this.colours)
                {
                    c.material = ThutCore.trim(c.material);
                    this._colourMap_.put(c.material, c);
                }

                final ResourceLocation texl = this.tex != null ? PokecubeItems.toPokecubeResource(baseEntry.texturePath
                        + this.tex) : null;
                final ResourceLocation modell = this.model != null ? PokecubeItems.toPokecubeResource(baseEntry.model
                        .toString().replace(baseEntry.getTrimmedName(), this.model)) : null;
                final ResourceLocation animl = this.anim != null ? PokecubeItems.toPokecubeResource(baseEntry.animation
                        .toString().replace(baseEntry.getTrimmedName(), this.anim)) : null;
                final FormeHolder holder = FormeHolder.get(modell, texl, animl, key);
                holder.loaded_from = this;
                Database.registerFormeHolder(baseEntry, holder);
                return holder;
            }
            return null;
        }
    }

    public static class Evolution
    {
        public Boolean   clear;
        public String    name;
        public Integer   level;
        public Integer   priority;
        public SpawnRule location;
        public String    animation;
        public Drop      item;
        public String    item_preset;
        public String    time;
        public Boolean   trade;
        public Boolean   rain;
        public Boolean   happy;
        public String    sexe;
        public String    move;
        public Float     chance;

        public String form_from = null;

        protected DefaultFormeHolder model = null;

        public FormeHolder getForme(final PokedexEntry baseEntry)
        {
            if (this.model != null) return this.model.getForme(baseEntry);
            return null;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (super.equals(obj)) return true;
            if (obj instanceof Evolution)
            {
                for (final Field f : this.getClass().getFields())
                    try
                    {
                        final Object ours = f.get(this);
                        final Object theirs = f.get(obj);
                        if (ours != null && !ours.equals(theirs)) return false;
                        if (theirs != null && !theirs.equals(ours)) return false;
                        if (ours == null && theirs != null) return false;
                        if (theirs == null && ours != null) return false;
                    }
                    catch (final Exception e)
                    {
                        e.printStackTrace();
                        return false;
                    }
                return true;
            }

            return super.equals(obj);
        }
    }

    public static class Interact
    {
        public Boolean male       = true;
        public Boolean female     = true;
        public Integer cooldown   = 50;
        public Integer variance   = 100;
        public Integer baseHunger = 100;
        public Drop    key;
        public Action  action;
    }

    public static class MegaEvoRule implements MegaRule
    {
        public ItemStack   stack;
        public String      oreDict;
        public String      moveName;
        public String      ability;
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

    public static class Moves
    {
        public static class LvlUp
        {
            public Map<QName, String> values = Maps.newHashMap();
        }

        public static class Misc
        {
            public String moves;

            @Override
            public String toString()
            {
                return this.moves;
            }
        }

        public LvlUp  lvlupMoves;
        public Misc   misc;
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

        protected DefaultFormeHolder model = null;

        public FormeHolder getForme(final PokedexEntry baseEntry)
        {
            if (this.model != null) return this.model.getForme(baseEntry);
            return null;
        }
    }

    public static class StatsNode
    {
        public static class Stats
        {
            public Map<QName, String> values = Maps.newHashMap();
        }

        // Evolution stuff
        public List<Evolution> evolutions = Lists.newArrayList();

        // Species and food
        public String species;
        public String prey;
        public String foodMat;
        public String specialEggRules;
        // Drops and items
        public List<Drop> drops = Lists.newArrayList();
        public List<Drop> held  = Lists.newArrayList();
        public String     lootTable;
        public String     heldTable;
        // Spawn Rules
        public Boolean         overwrite;
        public List<SpawnRule> spawnRules = Lists.newArrayList();
        // STATS
        public Stats   stats;
        public Stats   evs;
        public Stats   sizes;
        public Stats   types;
        public Stats   abilities;
        public Float   mass           = -1f;
        public Integer captureRate    = -1;
        public Integer baseExp        = -1;
        public Integer baseFriendship = 70;
        public String  expMode;

        public Integer genderRatio = -1;
        // MISC
        public Stats logics;
        public Stats formeItems;
        // New Mega rules
        public List<XMLMegaRule> megaRules    = Lists.newArrayList();
        public List<Interact>    interactions = Lists.newArrayList();

        public String movementType;
        public String shadowReplacements;
        public String hatedMaterials;

        public String activeTimes;

        @Override
        public String toString()
        {
            return this.spawnRules + "";
        }
    }

    public static class XMLDatabase
    {
        public boolean               hotload = false;
        public List<XMLPokedexEntry> pokemon = Lists.newArrayList();

        public Map<String, XMLPokedexEntry> __map__ = Maps.newHashMap();

        public void addEntry(final XMLPokedexEntry toAdd)
        {
            if (this.__map__.containsKey(toAdd.name)) this.pokemon.remove(this.__map__.remove(toAdd.name));
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
                        this.__map__.put(entry.name, entry);
                        this.pokemon.add(entry);
                        entry.mergeMissingFrom(e);
                        return;
                    }
                    else e.mergeMissingFrom(entry);
                    return;
                }
            this.pokemon.add(entry);
            this.__map__.put(entry.name, entry);
        }

        public void init()
        {
            for (final XMLPokedexEntry e : this.pokemon)
                this.__map__.put(e.name, e);
        }
    }

    public static class XMLMegaRule
    {
        public String name;
        public String preset;
        public String move;
        public String ability;
        public Drop   item;
        public String item_preset;
    }

    public static class XMLPokedexEntry
    {
        public String    name;
        public Integer   number;
        public String    special;
        public Boolean   base           = false;
        public Boolean   breed          = true;
        public Boolean   dummy          = false;
        public Boolean   starter        = false;
        public Boolean   ridable        = true;
        public Boolean   legend         = false;
        public Boolean   hasShiny       = true;
        public String    gender         = "";
        public String    genderBase     = "";
        public String    modelType      = "";
        public String    sound          = null;
        public String    ridden_offsets = "0.75";
        public StatsNode stats;
        public Moves     moves;
        public BodyNode  body;

        public DefaultFormeHolder model        = null;
        public DefaultFormeHolder male_model   = null;
        public DefaultFormeHolder female_model = null;

        public List<DefaultFormeHolder> models = Lists.newArrayList();

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
            if (this.model == null && other.model != null) this.model = other.model;

            for (final DefaultFormeHolder others : other.models)
            {
                for (final DefaultFormeHolder ours : this.models)
                    if (others.key.equals(ours.key))
                    {
                        this.models.remove(ours);
                        break;
                    }
                this.models.add(others);
            }
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
        }).setPrettyPrinting().disableHtmlEscaping().create();
        PokedexEntryLoader.missingno.stats = new StatsNode();
    }

    public static XMLDatabase            database;
    public static List<ResourceLocation> hotloadable = Lists.newArrayList();

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
                if (field.getName().startsWith("_")) continue;
                field.setAccessible(true);
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
    public static ItemStack getStackFromDrop(final Drop drop)
    {
        return Tools.getStack(drop.getValues());
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
                final XMLPokedexEntry old = PokedexEntryLoader.database.__map__.get(e.name);
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
            final XMLDatabase database = PokedexEntryLoader.initDatabase(Database.resourceManager.getResource(file)
                    .getInputStream(), true);
            return database;
        }
        catch (final FileNotFoundException e)
        {
            PokecubeCore.LOGGER.debug("No Pokemob Database: {}", file);
            return null;
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

    private static void initFormeModels(final PokedexEntry entry, final List<DefaultFormeHolder> list)
    {
        for (final DefaultFormeHolder holder : list)
        {
            holder.getForme(entry);
            PokecubeCore.LOGGER.debug("Loaded Forme: " + holder.key);
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

    public static void updateEntry(final PokedexEntry entry)
    {
        for (final ResourceLocation s : PokedexEntryLoader.hotloadable)
            try
            {
                PokecubeCore.LOGGER.debug("Loading from: {}", s);
                PokedexEntryLoader.initDatabase(s);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error with pokemobs database " + s, e);
            }

        final List<XMLPokedexEntry> entries = Lists.newArrayList(PokedexEntryLoader.database.pokemon);

        for (final XMLPokedexEntry xmlEntry : entries)
        {
            final String name = xmlEntry.name;
            if (!name.equals(entry.getName())) continue;
            PokedexEntryLoader.updateEntry(xmlEntry, false);
            return;
        }
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
        {
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Proccessing Evos for " + entry.getName());
            for (final Evolution evol : xmlStats.evolutions)
            {
                final String name = evol.name;
                final PokedexEntry evolEntry = Database.getEntry(name);
                EvolutionData data = null;
                final boolean clear = evol.clear != null && evol.clear;
                // check for specific clearing info for this entry.
                for (final EvolutionData d : entry.evolutions)
                    if (d.data.equals(evol))
                    {
                        data = d;
                        if (clear)
                        {
                            entry.evolutions.remove(d);
                            PokecubeCore.LOGGER.info("Replacing evolution for " + entry + " -> " + evolEntry);
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
                            if (existing.data.equals(existing)) break check;
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
        SpawnData spawnData = entry.getSpawnData();
        if (spawnData == null || overwrite) spawnData = new SpawnData(entry);
        for (final SpawnRule rule : xmlStats.spawnRules)
        {
            final FormeHolder holder = rule.getForme(entry);
            if (holder != null) Database.registerFormeHolder(entry, holder);
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
                            defaultSpecial = ThutCore.trim(defaultSpecial);
                            for (final DyeColor dye : DyeColor.values())
                                if (ThutCore.trim(dye.name()).equals(defaultSpecial))
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
                                entry.defaultSpecials = Integer.parseInt(defaultSpecial);
                            }
                            catch (final NumberFormatException e)
                            {
                                defaultSpecial = ThutCore.trim(defaultSpecial);
                                for (final DyeColor dye : DyeColor.values())
                                    if (ThutCore.trim(dye.name()).equals(defaultSpecial))
                                    {
                                        entry.defaultSpecials = dye.getId();
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

        if (xmlStats.megaRules != null) for (final XMLMegaRule rule : xmlStats.megaRules)
        {

            String forme = rule.name != null ? rule.name : null;
            if (forme == null) if (rule.preset != null) if (rule.preset.startsWith("Mega"))
            {
                forme = entry.getTrimmedName() + "_" + ThutCore.trim(rule.preset);
                if (rule.item_preset == null) rule.item_preset = entry.getTrimmedName() + "" + ThutCore.trim(
                        rule.preset);
            }
            final String move = rule.move;
            final String ability = rule.ability;
            final String item_preset = rule.item_preset;

            if (forme == null)
            {
                PokecubeCore.LOGGER.info("Error with mega evolution for " + entry + " rule: preset=" + rule.preset
                        + " name=" + rule.name);
                continue;
            }

            final PokedexEntry formeEntry = Database.getEntry(forme);
            if (!forme.isEmpty() && formeEntry != null)
            {
                ItemStack stack = ItemStack.EMPTY;
                if (item_preset != null && !item_preset.isEmpty())
                {
                    if (PokecubeMod.debug) PokecubeCore.LOGGER.info(forme + " " + item_preset);
                    stack = PokecubeItems.getStack(item_preset, false);
                    if (stack.isEmpty()) stack = PokecubeItems.getStack(Database.trim_loose(item_preset), false);
                }
                else if (rule.item != null) stack = Tools.getStack(rule.item.getValues());
                if (rule.item != null) if (PokecubeMod.debug) PokecubeCore.LOGGER.info(stack + " " + rule.item
                        .getValues());
                if ((move == null || move.isEmpty()) && stack.isEmpty() && (ability == null || ability.isEmpty()))
                {
                    PokecubeCore.LOGGER.info("Skipping Mega: " + entry + " -> " + formeEntry
                            + " as it has no conditions, or conditions cannot be met.");
                    PokecubeCore.LOGGER.info(" rule: preset=" + rule.preset + " name=" + rule.name + " item="
                            + rule.item_preset);
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
        final StatsNode stats = xmlEntry.stats;
        final Moves moves = xmlEntry.moves;
        final String name = xmlEntry.name;
        final PokedexEntry entry = Database.getEntry(name);
        entry.modelExt = xmlEntry.modelType;

        if (entry._default_holder == null && xmlEntry.model != null) entry._default_holder = xmlEntry.model;
        if (entry._male_holder == null && xmlEntry.male_model != null) entry._male_holder = xmlEntry.male_model;
        if (entry._female_holder == null && xmlEntry.female_model != null) entry._female_holder = xmlEntry.female_model;

        if (stats != null) try
        {
            if (xmlEntry.sound != null) entry.customSound = xmlEntry.sound;
            entry.modelExt = xmlEntry.modelType;
            PokedexEntryLoader.initStats(entry, stats);
            PokedexEntryLoader.initFormeModels(entry, xmlEntry.models);
            if (!init)
            {
                entry.breeds = xmlEntry.breed;
                entry.isStarter = xmlEntry.starter;
                entry.ridable = xmlEntry.ridable;
                entry.legendary = xmlEntry.legend;
                entry.hasShiny = xmlEntry.hasShiny;

                if (xmlEntry.ridden_offsets != null)
                {
                    // This is each passenger
                    final String[] args = xmlEntry.ridden_offsets.split(":");
                    final List<double[]> offsets = Lists.newArrayList();
                    for (final String s : args)
                    {
                        final String[] vec = s.split(",");
                        if (vec.length == 1) offsets.add(new double[] { 0, Float.parseFloat(vec[0]), 0 });
                        else if (vec.length == 3) offsets.add(new double[] { Float.parseFloat(vec[0]), Float.parseFloat(
                                vec[1]), Float.parseFloat(vec[2]) });
                        else PokecubeCore.LOGGER.warn("Wrong number of numbers for offset, must be 1 or 3: " + entry
                                + " got: " + vec.length);
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
            final Map<String, XMLPokedexEntry> back = database.__map__;
            database.__map__ = null;
            final String json = PokedexEntryLoader.gson.toJson(database);
            database.__map__ = back;
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
