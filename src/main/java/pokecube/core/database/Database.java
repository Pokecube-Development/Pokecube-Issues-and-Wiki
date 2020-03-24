package pokecube.core.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.InteractionLogic;
import pokecube.core.database.PokedexEntry.MovementType;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.PokedexEntryLoader.XMLDatabase;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.moves.json.JsonMoves.AnimationJson;
import pokecube.core.database.moves.json.JsonMoves.MoveJsonEntry;
import pokecube.core.database.moves.json.JsonMoves.MovesJson;
import pokecube.core.database.recipes.XMLRecipeHandler;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipes;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.resources.PackListener;
import pokecube.core.database.rewards.XMLRewardsHandler;
import pokecube.core.database.rewards.XMLRewardsHandler.XMLReward;
import pokecube.core.database.rewards.XMLRewardsHandler.XMLRewards;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.implementations.MovesAdder;
import pokecube.core.utils.PokeType;
import thut.core.common.ThutCore;

public class Database
{
    /** <br>
     * Index 0 = pokemobs<br>
     * Index 1 = moves<br>
    */
    public static enum EnumDatabase
    {
        POKEMON, MOVES, BERRIES
    }

    @XmlRootElement(name = "Drop")
    public static class XMLDropEntry extends Drop
    {
        @XmlAttribute
        boolean overwrite = false;
        @XmlAttribute
        String  name;
    }

    @XmlRootElement(name = "Drops")
    public static class XMLDrops
    {
        @XmlElement(name = "Drop")
        private final List<XMLDropEntry> pokemon = Lists.newArrayList();
    }

    @XmlRootElement(name = "Held")
    public static class XMLHeldEntry extends Drop
    {
        @XmlAttribute
        boolean overwrite = false;
        @XmlAttribute
        String  name;
    }

    @XmlRootElement(name = "Helds")
    public static class XMLHelds
    {
        @XmlElement(name = "Held")
        private final List<XMLHeldEntry> pokemon = Lists.newArrayList();
    }

    @XmlRootElement(name = "Spawn")
    public static class XMLSpawnEntry extends SpawnRule
    {
        static final QName STARTER   = new QName("starter");
        @XmlAttribute
        boolean            overwrite = false;
        @XmlAttribute
        String             name;

        public Boolean isStarter()
        {
            if (!this.values.containsKey(XMLSpawnEntry.STARTER)) return null;
            return Boolean.parseBoolean(this.values.get(XMLSpawnEntry.STARTER));
        }
    }

    @XmlRootElement(name = "Spawns")
    public static class XMLSpawns
    {
        @XmlElement(name = "Spawn")
        private final List<XMLSpawnEntry> pokemon = Lists.newArrayList();
    }

    @XmlRootElement(name = "Items")
    public static class XMLStarterItems
    {
        @XmlElement(name = "Item")
        private final List<Drop> drops = Lists.newArrayList();
    }

    public static List<ItemStack>                          starterPack     = Lists.newArrayList();
    public static Int2ObjectOpenHashMap<PokedexEntry>      data            = new Int2ObjectOpenHashMap<>();
    public static HashMap<String, PokedexEntry>            data2           = new HashMap<>();
    static HashSet<PokedexEntry>                           allFormes       = new HashSet<>();
    private static List<PokedexEntry>                      sortedFormes    = Lists.newArrayList();
    private static List<String>                            sortedFormNames = Lists.newArrayList();
    public static HashMap<Integer, PokedexEntry>           baseFormes      = new HashMap<>();
    public static HashMap<Integer, PokedexEntry>           dummyMap        = new HashMap<>();
    public static HashMap<String, ArrayList<PokedexEntry>> mobReplacements = new HashMap<>();
    public static HashMap<PokedexEntry, List<FormeHolder>> customModels    = new HashMap<>();
    public static HashMap<ResourceLocation, FormeHolder>   formeHolders    = new HashMap<>();

    public static Int2ObjectOpenHashMap<List<PokedexEntry>> formLists        = new Int2ObjectOpenHashMap<>();

    public static List<PokedexEntry>                        spawnables       = new ArrayList<>();
    /** These are used for config added databasea <br>
     * Index 0 = pokemon<br>
     * Index 1 = moves<br>
    */
    public static List<ArrayList<ResourceLocation>>         configDatabases  = Lists.newArrayList(
            new ArrayList<ResourceLocation>(), new ArrayList<ResourceLocation>(), new ArrayList<ResourceLocation>());
    public static Set<ResourceLocation>                     defaultDatabases = Sets.newHashSet();
    public static Set<ResourceLocation>                     spawnDatabases   = Sets.newHashSet();
    public static Set<ResourceLocation>                     dropDatabases    = Sets.newHashSet();

    public static Set<ResourceLocation>                     heldDatabases    = Sets.newHashSet();

    public static ResourceLocation                          STARTERPACK      = new ResourceLocation(
            "pokecube:database/pack.xml");

    public static final PokedexEntry                        missingno        = new PokedexEntry(0, "MissingNo");

    public static final Comparator<PokedexEntry>            COMPARATOR       = (o1, o2) ->
                                                                             {
                                                                                 int diff = o1.getPokedexNb()
                                                                                         - o2.getPokedexNb();
                                                                                 if (diff == 0)
                                                                                     if (o1.base && !o2.base) diff = -1;
                                                                                 else if (o2.base && !o1.base) diff = 1;
                                                                                 else diff = o1.getName()
                                                                                         .compareTo(o2.getName());
                                                                                 return diff;
                                                                             };
    // Init some stuff for the missignno entry.
    static
    {
        Database.missingno.type1 = PokeType.unknown;
        Database.missingno.type2 = PokeType.unknown;
        Database.missingno.base = true;
        Database.missingno.evs = new byte[6];
        Database.missingno.stats = new int[6];
        Database.missingno.height = 1;
        Database.missingno.width = Database.missingno.length = 0.41f;
        Database.missingno.stats[0] = 33;
        Database.missingno.stats[1] = 136;
        Database.missingno.stats[2] = 0;
        Database.missingno.stats[3] = 6;
        Database.missingno.stats[4] = 6;
        Database.missingno.stats[5] = 29;
        Database.missingno.addMoves(Lists.newArrayList(), Maps.newHashMap());
        Database.missingno.addMove("skyattack");
        Database.missingno.mobType = MovementType.FLYING;
        Database.addEntry(Database.missingno);
    }

    static int                               lastCount       = -1;

    public static IReloadableResourceManager resourceManager = new SimpleReloadableResourceManager(
            ResourcePackType.SERVER_DATA, Thread.currentThread());

    public static PokedexEntry[]             starters        = {};

    private static boolean                   checkedStarts   = false;

    public static void addDatabase(final String file, final EnumDatabase database)
    {
        final ResourceLocation loc = PokecubeItems.toPokecubeResource(file);
        final int index = database.ordinal();
        final ArrayList<ResourceLocation> list = Database.configDatabases.get(index);
        for (final ResourceLocation s : list)
            if (s.equals(loc)) return;
        PokecubeCore.LOGGER.debug("Adding Database: {}", loc);
        list.add(loc);
    }

    public static void addEntry(final PokedexEntry entry)
    {
        Database.data.put(entry.getPokedexNb(), entry);
    }

    public static void addHeldData(final String file)
    {
        final ResourceLocation loc = PokecubeItems.toPokecubeResource(file);
        Database.heldDatabases.add(loc);
    }

    public static void addSpawnData(final String file)
    {
        final ResourceLocation loc = PokecubeItems.toPokecubeResource(file);
        Database.spawnDatabases.add(loc);
    }

    /** Replaces a dummy base form with the first form in the sorted list.
     *
     * @param formes
     * @param vars */
    private static void checkDummies(final List<PokedexEntry> formes, final Map.Entry<Integer, PokedexEntry> vars)
    {
        final PokedexEntry entry = vars.getValue();
        if (entry.dummy)
        {
            Database.dummyMap.put(vars.getKey(), entry);
            for (final PokedexEntry entry1 : formes)
                if (!entry1.dummy)
                {
                    entry1.base = true;
                    entry.base = false;
                    Database.data.put(entry1.getPokedexNb(), entry1);
                    Database.data2.put(entry.getTrimmedName(), entry1);
                    Database.data2.put(entry.getName(), entry1);
                    // Set all the subformes base to this new one.
                    for (final PokedexEntry e : formes)
                    {
                        // Set the forme.
                        e.setBaseForme(entry1);
                        // Initialize some things.
                        e.getBaseForme();
                    }
                    vars.setValue(entry1);
                    break;
                }
        }
    }

    public static void registerFormeHolder(final PokedexEntry entry, final FormeHolder holder)
    {
        List<FormeHolder> holders = Database.customModels.get(entry);
        if (holders == null) Database.customModels.put(entry, holders = Lists.newArrayList());
        if (!holders.contains(holder))
        {
            holders.add(holder);
            Collections.sort(holders, (o1, o2) -> o1.key.compareTo(o2.key));
        }
    }

    private static void checkGenderFormes(final List<PokedexEntry> formes, final Map.Entry<Integer, PokedexEntry> vars)
    {
        PokedexEntry entry = vars.getValue();
        final PokedexEntry female = entry.getForGender(IPokemob.FEMALE);
        final PokedexEntry male = entry.getForGender(IPokemob.MALE);

        /** If the forme has both male and female entries, replace the base
         * forme with the male forme. */
        if (male != female && male != entry && female != entry)
        {
            male.base = true;
            male.male = male;
            female.male = male;
            male.female = female;
            entry.dummy = true;
            entry.base = false;
            Database.data.put(male.getPokedexNb(), male);
            Database.data2.put(entry.getTrimmedName(), male);
            Database.data2.put(entry.getName(), male);
            vars.setValue(male);
            // Set all the subformes base to this new one.
            for (final PokedexEntry e : formes)
            {
                // Set the forme.
                e.setBaseForme(male);
                // Initialize some things.
                e.getBaseForme();
            }
            entry = male;
        }
    }

    public static String convertMoveName(final String moveNameFromBulbapedia)
    {
        final String ret = Database.trim(moveNameFromBulbapedia);
        return ret;
    }

    public static boolean entryExists(final int nb)
    {
        return Database.getEntry(nb) != null;
    }

    public static boolean entryExists(final String name)
    {
        return Database.getEntry(name) != null;
    }

    public static PokedexEntry getEntry(final int nb)
    {
        return Database.data.get(nb);
    }

    public static PokedexEntry getEntry(final IPokemob mob)
    {
        return mob.getPokedexEntry();
    }

    public static PokedexEntry getEntry(String name)
    {
        final PokedexEntry ret = null;
        if (name == null) return null;
        name = ThutCore.trim(name);
        if (name.trim().isEmpty()) return null;
        final PokedexEntry test = Database.data2.get(name);
        if (test != null) return test;
        final List<PokedexEntry> toProcess = Lists.newArrayList(Database.allFormes);
        toProcess.sort(Database.COMPARATOR);
        final String name2 = Database.trim_loose(name);
        for (final PokedexEntry e : toProcess)
        {
            final String s = Database.trim_loose(e.getTrimmedName());
            if (s.equals(name2))
            {
                Database.data2.put(name, e);
                Database.data2.put(s, e);
                return e;
            }
        }
        if (ThutCore.trim(name).contains("mega_"))
            return Database.getEntry((ThutCore.trim(name).replace("mega_", "") + "_mega").trim());
        return ret;
    }

    public static ArrayList<String> getFile(final ResourceLocation file)
    {
        final ArrayList<String> rows = new ArrayList<>();
        BufferedReader br = null;
        String line = "";
        try
        {
            final InputStream res = Database.resourceManager.getResource(file).getInputStream();
            br = new BufferedReader(new InputStreamReader(res));
            while ((line = br.readLine()) != null)
                rows.add(line);

        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with " + file, e);
        }
        finally
        {
            if (br != null) try
            {
                br.close();
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error with " + file, e);
            }
        }

        return rows;
    }

    public static List<PokedexEntry> getFormes(final int number)
    {
        List<PokedexEntry> formes = Database.formLists.get(number);
        if (formes == null)
        {
            formes = Lists.newArrayList();
            Database.formLists.put(number, formes);
        }
        return formes;
    }

    public static List<PokedexEntry> getFormes(final PokedexEntry variant)
    {
        return Database.getFormes(variant.getPokedexNb());
    }

    public static List<String> getLearnableMoves(final int nb)
    {
        return Database.entryExists(nb) ? Database.getEntry(nb).getMoves() : null;
    }

    public static List<String> getLevelUpMoves(final PokedexEntry entry, final int level, final int oldLevel)
    {
        return entry != null ? entry.getMovesForLevel(level, oldLevel) : null;
    }

    public static List<PokedexEntry> getSortedFormes()
    {
        if (Database.lastCount != Database.allFormes.size())
        {
            Database.sortedFormes.clear();
            Database.sortedFormes.addAll(Database.allFormes);
            Database.sortedFormes.sort(Database.COMPARATOR);
            for (final PokedexEntry e : Database.sortedFormes)
                Database.sortedFormNames.add(e.getTrimmedName());
            Database.lastCount = Database.sortedFormes.size();
        }
        return Database.sortedFormes;
    }

    public static List<String> getSortedFormNames()
    {
        Database.getSortedFormes();
        return Database.sortedFormNames;
    }

    public static SpawnData getSpawnData(final int nb)
    {
        if (Database.data.containsKey(nb)) return Database.data.get(nb).getSpawnData();
        return null;
    }

    public static PokedexEntry[] getStarters()
    {
        if (!Database.checkedStarts)
        {
            Database.checkedStarts = true;
            final List<PokedexEntry> starts = Lists.newArrayList();
            for (final PokedexEntry e : Database.getSortedFormes())
                if (e.isStarter) starts.add(e);
            Database.starters = starts.toArray(Database.starters);
        }
        return Database.starters;
    }

    public static boolean hasSpawnData(final int nb)
    {
        return Database.getEntry(nb) != null && Database.getEntry(nb).getSpawnData() != null;
    }

    /** This loads in the various databases, merges them then makes pokedex
     * entries as needed */
    public static void init()
    {
        PokecubeCore.LOGGER.debug("Database Init()");

        // Fire load event to let addons do stuff after databases have been
        // loaded.
        MinecraftForge.EVENT_BUS.post(new InitDatabase.Load());

        // Make the pokedex entries with what was in database.
        try
        {
            PokedexEntryLoader.makeEntries(true);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with databases ", e);
        }
        // Outputs a file for reference.
        if (PokecubeMod.debug) PokedexEntryLoader.writeCompoundDatabase();
        // Init the lists of what all forms are loaded.
        Database.initFormLists();

        if (PokecubeMod.debug)
        {
            // Debug some dummy lists.
            final List<PokedexEntry> dummies = Lists.newArrayList();
            for (final PokedexEntry entry : Database.allFormes)
                if (entry.dummy) dummies.add(entry);
            dummies.sort(Database.COMPARATOR);
            final StringBuilder builder = new StringBuilder("Dummy Pokedex Entries:");
            for (final PokedexEntry e : dummies)
                builder.append("\n-   ").append(e.getName());
            PokecubeCore.LOGGER.debug(builder.toString());
        }

        PokecubeCore.LOGGER.info("Loaded " + Database.data.size() + " by number, and " + Database.allFormes.size()
                + " by formes from databases.");
    }

    /** Initializes the various values for the forms from the base form.
     *
     * @param formes
     *            to initialize
     * @param base
     *            to copy values from */
    private static void initFormes(final List<PokedexEntry> formes, final PokedexEntry base)
    {
        base.copyToGenderFormes();
        PokecubeCore.LOGGER.debug("Processing " + base + " " + formes);
        for (final PokedexEntry e : formes)
        {
            e.forms.clear();
            for (final PokedexEntry e1 : formes)
                if (e1 != e) e.forms.put(e1.getTrimmedName(), e1);
            if (base != e)
            {
                e.setBaseForme(base);
                base.copyToForm(e);
                if (e.height <= 0)
                {
                    e.height = base.height;
                    e.width = base.width;
                    e.length = base.length;
                    e.childNumbers = base.childNumbers;
                    e.species = base.species;
                    e.mobType = base.mobType;
                    e.catchRate = base.catchRate;
                    e.mass = base.mass;
                    PokecubeCore.LOGGER.debug("Error with " + e);
                }
                if (e.species == null)
                {
                    e.childNumbers = base.childNumbers;
                    e.species = base.species;
                    PokecubeCore.LOGGER.debug(e + " Has no Species");
                }
                if (e.type1 == null)
                {
                    e.type1 = base.type1;
                    e.type2 = base.type2;
                    PokecubeCore.LOGGER.debug("Copied Types from " + base + " to " + e);
                }
                boolean noAbilities;
                if (noAbilities = e.abilities.isEmpty()) e.abilities.addAll(base.abilities);
                if (noAbilities && e.abilitiesHidden.isEmpty()) e.abilitiesHidden.addAll(base.abilitiesHidden);
            }
            if (e.mobType == null)
            {
                e.mobType = MovementType.NORMAL;
                PokecubeCore.LOGGER.debug(e + " Has no Mob Type");
            }
            if (e.type2 == null) e.type2 = PokeType.unknown;
            if (e.interactionLogic.actions.isEmpty())
            {
                InteractionLogic.initForEntry(e);
                if (e.interactionLogic.actions.isEmpty() && !base.interactionLogic.actions.isEmpty())
                    e.interactionLogic.actions = base.interactionLogic.actions;
            }
        }
    }

    private static void initFormLists()
    {
        PokecubeCore.LOGGER.debug("Processing Form Lists");
        for (final Map.Entry<Integer, PokedexEntry> vars : Database.baseFormes.entrySet())
        {
            PokedexEntry entry = vars.getValue();
            final List<PokedexEntry> formes = Lists.newArrayList();
            final Set<PokedexEntry> set = Sets.newHashSet();
            set.addAll(entry.forms.values());
            set.add(entry);
            /** Collect all the different forms we can for this mob. */
            for (final PokedexEntry e : Database.allFormes)
                if (e.getPokedexNb() == entry.getPokedexNb()) set.add(e);
            formes.addAll(set);
            /** If only 1 form, no point in processing this. */
            if (formes.size() > 1)
            {
                formes.sort(Database.COMPARATOR);
                /** First init the formes, to copy the stuff over from the
                 * current base forme if needed. */
                Database.initFormes(formes, entry);
                /** Then Check if the entry should be replaced with a gender
                 * version */
                Database.checkGenderFormes(formes, vars);
                /** Then check if the base form, or any others, are dummy forms,
                 * and replace them. */
                Database.checkDummies(formes, vars);
            }
            entry = vars.getValue();
            Database.formLists.put(entry.getPokedexNb(), formes);
        }
        // Post process, also count dummies.
        int dummies = 0;
        for (final PokedexEntry e : Database.allFormes)
        {
            if (e.getType1() == null)
            {
                e.type1 = PokeType.unknown;
                if (e != Database.missingno)
                    PokecubeCore.LOGGER.error("Error with typing for " + e + " " + e.getType2());
            }
            if (e.getType2() == null) e.type2 = PokeType.unknown;
            if (e.dummy) dummies++;
        }
        PokecubeCore.LOGGER.debug("Processed Form Lists, found " + dummies + " Dummy Forms.");
    }

    public static void initMobSounds(final IForgeRegistry<SoundEvent> registry)
    {
        // Register sounds for the pokemobs
        final List<PokedexEntry> toProcess = Lists.newArrayList(Pokedex.getInstance().getRegisteredEntries());
        toProcess.sort(Database.COMPARATOR);
        for (final PokedexEntry e : toProcess)
        {
            if (e.getModId() == null || e.event != null) continue;
            if (e.sound == null) if (e.customSound != null) e.setSound("mobs." + Database.trim(e.customSound));
            else if (e.base) e.setSound("mobs." + e.getTrimmedName());
            else e.setSound("mobs." + e.getBaseForme().getTrimmedName());
            PokecubeCore.LOGGER.debug(e + " has Sound: " + e.sound);
            e.event = new SoundEvent(e.sound);
            e.event.setRegistryName(e.sound);
            // Loader.instance().setActiveModContainer(mc);
            if (registry.containsKey(e.sound)) continue;
            registry.register(e.event);
        }
    }

    public static void initSounds(final IForgeRegistry<SoundEvent> registry)
    {
        // Register sounds for the moves

        // null as it should have been populated already
        final MovesJson moves = JsonMoves.getMoves(null);
        for (final MoveJsonEntry entry : moves.moves)
        {
            // Register sound on source
            if (entry.soundEffectSource != null)
            {
                final ResourceLocation sound = new ResourceLocation(entry.soundEffectSource);
                final SoundEvent event = new SoundEvent(sound);
                event.setRegistryName(sound);
                if (!registry.containsKey(sound) && !sound.getNamespace().equals("minecraft")) registry.register(event);
            }
            // Register sound on target
            if (entry.soundEffectTarget != null)
            {
                final ResourceLocation sound = new ResourceLocation(entry.soundEffectTarget);
                final SoundEvent event = new SoundEvent(sound);
                event.setRegistryName(sound);
                if (!registry.containsKey(sound) && !sound.getNamespace().equals("minecraft")) registry.register(event);
            }
            // Register sounds for the animations
            if (entry.animations != null) for (final AnimationJson anim : entry.animations)
                if (anim.sound != null)
                {
                    final ResourceLocation sound = new ResourceLocation(anim.sound);
                    final SoundEvent event = new SoundEvent(sound);
                    event.setRegistryName(sound);
                    if (!registry.containsKey(sound) && !sound.getNamespace().equals("minecraft"))
                        registry.register(event);
                }
        }

        // Register sound events from config.
        for (final String var : PokecubeCore.getConfig().customSounds)
        {
            final ResourceLocation sound = new ResourceLocation(var);
            final SoundEvent event = new SoundEvent(sound);
            event.setRegistryName(sound);
            if (!registry.containsKey(sound) && !sound.getNamespace().equals("minecraft")) registry.register(event);
        }
    }

    private static boolean recipeDone = false;

    public static void loadRecipes()
    {
        // We only want to do this once.
        if (Database.recipeDone) return;
        Database.recipeDone = true;

        for (final ResourceLocation name : XMLRecipeHandler.recipeFiles)
            try
            {
                final IReloadableResourceManager manager = Database.resourceManager;
                final Reader reader = new InputStreamReader(manager.getResource(name).getInputStream());
                final XMLRecipes database = PokedexEntryLoader.gson.fromJson(reader, XMLRecipes.class);
                reader.close();
                for (final XMLRecipe drop : database.recipes)
                    XMLRecipeHandler.addRecipe(drop);
            }
            catch (final FileNotFoundException e)
            {
                PokecubeCore.LOGGER.debug("No Custom Recipes of name {}", name);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error with " + name, e);
            }
    }

    public static void loadRewards(final String input)
    {
        if (XMLRewardsHandler.loadedRecipes.add(input)) Database.loadRewards(new StringReader(input));
    }

    public static void loadRewards(final Reader reader)
    {
        final XMLRewards database = PokedexEntryLoader.gson.fromJson(reader, XMLRewards.class);
        for (final XMLReward drop : database.recipes)
            XMLRewardsHandler.addReward(drop);
    }

    public static void loadRewards()
    {
        for (final ResourceLocation name : XMLRewardsHandler.recipeFiles)
            try
            {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(Database.resourceManager
                        .getResource(name).getInputStream()));
                final StringBuffer sb = new StringBuffer();
                String str;
                while ((str = reader.readLine()) != null)
                    sb.append(str);
                reader.close();
                Database.loadRewards(sb.toString());
            }
            catch (final FileNotFoundException e)
            {
                PokecubeCore.LOGGER.debug("No Custom Rewards of name {}", name);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error with " + name, e);
            }
    }

    private static void loadSpawns()
    {
        for (final ResourceLocation s : Database.spawnDatabases)
            if (s != null) Database.loadSpawns(s);
    }

    /** This method should only be called for override files, such as the one
     * added by Pokecube Compat
     *
     * @param file */
    private static void loadSpawns(final ResourceLocation file)
    {
        try
        {
            final JAXBContext jaxbContext = JAXBContext.newInstance(XMLSpawns.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final Reader reader = new InputStreamReader(Database.resourceManager.getResource(file).getInputStream());
            final XMLSpawns database = (XMLSpawns) unmarshaller.unmarshal(reader);
            reader.close();
            for (final XMLSpawnEntry xmlEntry : database.pokemon)
            {
                final PokedexEntry entry = Database.getEntry(xmlEntry.name);
                if (entry == null)
                {
                    new NullPointerException(xmlEntry.name + " not found").printStackTrace();
                    continue;
                }
                if (entry.isGenderForme) continue;
                if (xmlEntry.isStarter() != null) entry.isStarter = xmlEntry.isStarter();
                SpawnData data = entry.getSpawnData();
                if (xmlEntry.overwrite || data == null)
                {
                    data = new SpawnData(entry);
                    entry.setSpawnData(data);
                    PokecubeCore.LOGGER.debug("Overwriting spawns for " + entry);
                }
                else if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Editing spawns for " + entry);
                PokedexEntryLoader.handleAddSpawn(data, xmlEntry);
                Database.spawnables.remove(entry);
                if (!data.matchers.isEmpty()) Database.spawnables.add(entry);
            }
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with " + file, e);
        }
    }

    private static void loadStarterPack()
    {
        Database.starterPack.clear();
        try
        {
            final JAXBContext jaxbContext = JAXBContext.newInstance(XMLStarterItems.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final Reader reader = new InputStreamReader(
                    Database.resourceManager.getResource(Database.STARTERPACK).getInputStream());
            final XMLStarterItems database = (XMLStarterItems) unmarshaller.unmarshal(reader);
            reader.close();
            for (final Drop drop : database.drops)
            {
                final ItemStack stack = PokedexEntryLoader.getStackFromDrop(drop);
                if (!stack.isEmpty()) Database.starterPack.add(stack);
            }
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error Loading Starter Pack", e);
        }
    }

    /** does some final cleanup work for removing un-registered entries */
    public static void postInit()
    {
        PokecubeCore.LOGGER.debug("Post Init of Database.");
        final List<PokedexEntry> toRemove = new ArrayList<>();
        final Set<Integer> removedNums = Sets.newHashSet();
        final List<PokedexEntry> removed = Lists.newArrayList();
        for (final PokedexEntry e : Pokedex.getInstance().getRegisteredEntries())
            if (e.base) Database.addEntry(e);
        int dummies = 0;
        /** find non-registered entries to remove later. */
        for (final PokedexEntry p : Database.allFormes)
            if (!Pokedex.getInstance().getRegisteredEntries().contains(p))
            {
                if (p.dummy) dummies++;
                else removedNums.add(p.getPokedexNb());
                toRemove.add(p);
                removed.add(p);
            }
        Collections.sort(toRemove, Database.COMPARATOR);
        final List<PokedexEntry> messageList = Lists.newArrayList(toRemove);
        messageList.removeIf(t -> t.dummy);
        if (PokecubeMod.debug)
        {
            messageList.sort(Database.COMPARATOR);
            final StringBuilder builder = new StringBuilder("UnRegistered Pokedex Entries:");
            for (final PokedexEntry e : messageList)
                builder.append("\n-   ").append(e.getName());
            PokecubeCore.LOGGER.debug(builder.toString());
        }
        /** Remove the non-registered entries found earlier */
        for (final PokedexEntry p : toRemove)
        {
            if (p == Database.getEntry(p.pokedexNb) && !p.dummy)
            {
                if (p.dummy) PokecubeCore.LOGGER
                        .debug("Error with " + p + ", It is still listed as base forme, as well as being dummy.");
                Database.data.remove(p.pokedexNb);
                Database.baseFormes.remove(p.pokedexNb);
                Database.formLists.remove(p.pokedexNb);
            }
            else if (Database.formLists.containsKey(p.pokedexNb)) Database.formLists.get(p.pokedexNb).remove(p);
            Database.spawnables.remove(p);
        }

        /** Cleanup evolutions which are not actually in game. */
        for (final PokedexEntry e : Database.allFormes)
        {
            final List<EvolutionData> invalidEvos = Lists.newArrayList();
            for (final EvolutionData d : e.evolutions)
                if (!Pokedex.getInstance().getRegisteredEntries().contains(d.evolution)) invalidEvos.add(d);
            e.evolutions.removeAll(invalidEvos);
        }

        Database.allFormes.removeAll(toRemove);
        PokecubeCore.LOGGER.debug("Removed " + removedNums.size() + " Missing Pokemon and "
                + (toRemove.size() - dummies) + " missing Formes");

        toRemove.clear();
    }

    /** Loads in spawns, drops, held items and starter packs, as well as
     * initializing things like children, evolutions, etc */
    public static void postResourcesLoaded()
    {
        PokedexEntryLoader.postInit();
        Database.loadSpawns();
        Database.loadStarterPack();
        Database.loadRecipes();
        PokedexInspector.init();

        // Process custom forme models, etc
        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            if (entry._default_holder != null)
            {
                entry.default_holder = entry._default_holder.getForme(entry);
                Database.registerFormeHolder(entry, entry.default_holder);
            }
            if (entry._male_holder != null)
            {
                entry.male_holder = entry._male_holder.getForme(entry);
                Database.registerFormeHolder(entry, entry.male_holder);
            }
            if (entry._female_holder != null)
            {
                entry.female_holder = entry._female_holder.getForme(entry);
                Database.registerFormeHolder(entry, entry.female_holder);
            }

            // Spawns should have been dealt with earlier, so do evolutions
            if (!entry.evolutions.isEmpty()) for (final EvolutionData data : entry.evolutions)
            {
                final FormeHolder holder = data.data.getForme(data.evolution);
                if (holder != null) Database.registerFormeHolder(data.evolution, holder);
            }
        }

        /** Initialize relations, prey, children. */
        for (final PokedexEntry p : Database.allFormes)
            p.initRelations();
        for (final PokedexEntry p : Database.allFormes)
            p.initPrey();
        // Children last, as relies on relations.
        for (final PokedexEntry p : Database.allFormes)
            p.getChild();
    }

    public static Set<IResourcePack> customPacks = Sets.newHashSet();
    private static PackListener      listener    = new PackListener();

    public static void swapManager(final MinecraftServer server)
    {
        Database.resourceManager = server.getResourceManager();
        Database.listener.add(Database.resourceManager);
        server.getResourceManager().addReloadListener(Database.listener);
    }

    /** This is called before generating any items. This ensures that the types
     * are loaded correctly. */
    public static void preInit()
    {
        PokecubeCore.LOGGER.debug("Database preInit()");
        // Initialize the resourceloader.
        @SuppressWarnings("deprecation")
        final ResourcePackList<ResourcePackInfo> resourcePacks = new ResourcePackList<>(ResourcePackInfo::new);
        @SuppressWarnings("deprecation")
        final PackFinder finder = new PackFinder(ResourcePackInfo::new);
        resourcePacks.addPackFinder(finder);
        for (final IResourcePack info : finder.allPacks)
        {
            PokecubeCore.LOGGER.debug("Loading Pack: " + info.getName());
            ((SimpleReloadableResourceManager) Database.resourceManager).addResourcePack(info);
            Database.customPacks.add(info);
        }
        resourcePacks.close();

        // Register the dex inspector
        MinecraftForge.EVENT_BUS.register(PokedexInspector.class);

        // Load in the combat types first.
        CombatTypeLoader.loadTypes();
        // Load in the various databases, starting with moves, then pokemobs.
        MovesAdder.registerMoves();
        for (final ResourceLocation s : Database.configDatabases.get(EnumDatabase.POKEMON.ordinal()))
            try
            {
                PokecubeCore.LOGGER.debug("Loading from: {}", s);
                final XMLDatabase database = PokedexEntryLoader.initDatabase(s);
                // Hotloadable ones will be able to be re-loaded at runtime
                // later, for things like setting ridden offsets, etc
                if (database != null && database.hotload) PokedexEntryLoader.hotloadable.add(s);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error with pokemobs database " + s, e);
            }
        // Finally load in the abilities
        AbilityManager.init();

        PokecubeCore.LOGGER.debug("Loaded all databases");
    }

    public static void preInitMoves()
    {
        for (final ResourceLocation s : Database.configDatabases.get(EnumDatabase.MOVES.ordinal()))
            try
            {
                JsonMoves.merge(new ResourceLocation(s.getNamespace(), s.getPath().replace(".json", "_anims.json")), s);
            }
            catch (final Exception e1)
            {
                PokecubeCore.LOGGER.error("Error with moves database " + s, e1);
            }
    }

    public static String trim_loose(String name)
    {
        // ROOT locale to prevent issues with turkish letters.
        name = name.toLowerCase(Locale.ROOT).trim();
        // Replace all non-alphanumeric
        name = name.replaceAll("([^a-z0-9])", "");
        return name;
    }

    public static String trim(final String name)
    {
        return ThutCore.trim(name);
    }
}