package pokecube.core.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.database.moves.MovesDatabases;
import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.moves.json.JsonMoves.AnimationJson;
import pokecube.core.database.moves.json.JsonMoves.MoveJsonEntry;
import pokecube.core.database.moves.json.JsonMoves.MovesJson;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.DefaultFormeHolder;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;
import pokecube.core.database.pokedex.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.pokedex.PokemobsDatabases;
import pokecube.core.database.recipes.IRecipeParser;
import pokecube.core.database.recipes.XMLRecipeHandler;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.resources.PackListener;
import pokecube.core.database.rewards.XMLRewardsHandler;
import pokecube.core.database.rewards.XMLRewardsHandler.XMLReward;
import pokecube.core.database.rewards.XMLRewardsHandler.XMLRewards;
import pokecube.core.database.spawns.PokemobSpawns;
import pokecube.core.database.spawns.SpawnPresets;
import pokecube.core.database.spawns.SpawnRateMask;
import pokecube.core.database.tags.Tags;
import pokecube.core.database.util.DataHelpers;
import pokecube.core.database.worldgen.StructureSpawnPresetLoader;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.implementations.MovesAdder;
import pokecube.core.utils.PokeType;
import thut.core.common.ThutCore;
import thut.core.xml.bind.annotation.XmlAttribute;
import thut.core.xml.bind.annotation.XmlElement;
import thut.core.xml.bind.annotation.XmlRootElement;

public class Database
{
    @XmlRootElement(name = "Drop")
    public static class XMLDropEntry extends Drop
    {
        @XmlAttribute
        boolean overwrite = false;
        @XmlAttribute
        String name;
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
        String name;
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
        static final QName STARTER = new QName("starter");
        @XmlAttribute
        boolean overwrite = false;
        @XmlAttribute
        String name;

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

    public static class ReloadListener implements PreparableReloadListener
    {
        public static final PreparableReloadListener INSTANCE = new ReloadListener();

        @Override
        public final CompletableFuture<Void> reload(final PreparableReloadListener.PreparationBarrier stage,
                final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler,
                final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor)
        {
            return CompletableFuture.supplyAsync(() -> {
                return this.prepare(resourceManager, preparationsProfiler);
            }, backgroundExecutor).thenCompose(stage::wait).thenAcceptAsync((object) -> {
                this.apply(object, resourceManager, reloadProfiler);
            }, gameExecutor);
        }

        /**
         * Performs any reloading that can be done off-thread, such as file IO
         */
        protected Object prepare(final ResourceManager resourceManagerIn, final ProfilerFiller profilerIn)
        {
            return null;
        }

        protected void apply(final Object objectIn, final ResourceManager resourceManagerIn,
                final ProfilerFiller profilerIn)
        {
            Database.listener.add(resourceManagerIn);
            Database.onResourcesReloaded();
        }
    }

    public static List<ItemStack> starterPack = Lists.newArrayList();
    public static Int2ObjectOpenHashMap<PokedexEntry> data = new Int2ObjectOpenHashMap<>();
    public static HashMap<String, PokedexEntry> data2 = new HashMap<>();
    static HashSet<PokedexEntry> allFormes = new HashSet<>();
    private static List<PokedexEntry> sortedFormes = Lists.newArrayList();
    private static List<String> sortedFormNames = Lists.newArrayList();
    public static HashMap<Integer, PokedexEntry> baseFormes = new HashMap<>();
    public static HashMap<Integer, PokedexEntry> dummyMap = new HashMap<>();
    public static HashMap<String, ArrayList<PokedexEntry>> mobReplacements = new HashMap<>();
    public static HashMap<PokedexEntry, List<FormeHolder>> customModels = new HashMap<>();
    public static HashMap<ResourceLocation, FormeHolder> formeHolders = new HashMap<>();
    public static HashMap<ResourceLocation, PokedexEntry> formeToEntry = new HashMap<>();

    public static Int2ObjectOpenHashMap<List<PokedexEntry>> formLists = new Int2ObjectOpenHashMap<>();

    public static List<PokedexEntry> spawnables = new ArrayList<>();

    public static final PokedexEntry missingno = new PokedexEntry(0, "MissingNo");

    public static final Comparator<PokedexEntry> COMPARATOR = (o1, o2) -> {
        int diff = o1.getPokedexNb() - o2.getPokedexNb();
        if (diff == 0) if (o1.base && !o2.base) diff = -1;
        else if (o2.base && !o1.base) diff = 1;
        else diff = o1.getName().compareTo(o2.getName());
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
        Database.missingno.mobType = 15;
        Database.addEntry(Database.missingno);
    }

    static int lastCount = -1;

    public static ReloadableResourceManager resourceManager = new SimpleReloadableResourceManager(PackType.SERVER_DATA);

    public static PokedexEntry[] starters = {};

    private static boolean checkedStarts = false;

    public static void addEntry(final PokedexEntry entry)
    {
        Database.data.put(entry.getPokedexNb(), entry);
    }

    /**
     * Replaces a dummy base form with the first form in the sorted list.
     *
     * @param formes
     * @param vars
     */
    private static void checkDummies(final List<PokedexEntry> formes, final Map.Entry<Integer, PokedexEntry> vars)
    {
        final PokedexEntry entry = vars.getValue();
        if (entry.dummy)
        {
            Database.dummyMap.put(vars.getKey(), entry);
            for (final PokedexEntry entry1 : formes) if (!entry1.dummy)
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
        if (holder == null) return;
        List<FormeHolder> holders = Database.customModels.get(entry);
        Database.formeToEntry.put(holder.key, entry);
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

        /**
         * If the forme has both male and female entries, replace the base forme
         * with the male forme.
         */
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
            for (final PokedexEntry e : Database.sortedFormes) Database.sortedFormNames.add(e.getTrimmedName());
            Database.lastCount = Database.sortedFormes.size();
        }
        return Database.sortedFormes;
    }

    public static List<String> getSortedFormNames()
    {
        Database.getSortedFormes();
        return Database.sortedFormNames;
    }

    public static PokedexEntry[] getStarters()
    {
        if (!Database.checkedStarts)
        {
            Database.checkedStarts = true;
            final List<PokedexEntry> starts = Lists.newArrayList();
            for (final PokedexEntry e : Database.getSortedFormes()) if (e.isStarter) starts.add(e);
            Database.starters = starts.toArray(Database.starters);
        }
        return Database.starters;
    }

    /**
     * This loads in the various databases, merges them then makes pokedex
     * entries as needed
     */
    public static void init()
    {
        PokecubeCore.LOGGER.debug("Database Init()");

        SpawnPresets.init();
        PokemobSpawns.init();

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
            for (final PokedexEntry entry : Database.allFormes) if (entry.dummy) dummies.add(entry);
            dummies.sort(Database.COMPARATOR);
            final StringBuilder builder = new StringBuilder("Dummy Pokedex Entries:");
            for (final PokedexEntry e : dummies) builder.append("\n-   ").append(e.getName());
            PokecubeCore.LOGGER.debug(builder.toString());
        }

        PokecubeCore.LOGGER.info("Loaded " + Database.data.size() + " by number, and " + Database.allFormes.size()
                + " by formes from databases.");
    }

    /**
     * Initializes the various values for the forms from the base form.
     *
     * @param formes to initialize
     * @param base   to copy values from
     */
    private static void initFormes(final List<PokedexEntry> formes, final PokedexEntry base)
    {
        base.copyToGenderFormes();
        PokecubeCore.LOGGER.debug("Processing " + base + " " + formes);
        for (final PokedexEntry e : formes)
        {
            e.forms.clear();
            for (final PokedexEntry e1 : formes) if (e1 != e) e.forms.put(e1.getTrimmedName(), e1);
            if (base != e)
            {
                try
                {
                    e.setBaseForme(base);
                    base.copyToForm(e);
                }
                catch (final Exception e2)
                {
                    PokecubeCore.LOGGER.error("Error copying data: {} <-> {}", e, base);
                    e2.printStackTrace();
                    continue;
                }
                if (e.height <= 0)
                {
                    e.height = base.height;
                    e.width = base.width;
                    e.length = base.length;
                    e.mobType = base.mobType;
                    e.catchRate = base.catchRate;
                    e.mass = base.mass;
                    PokecubeCore.LOGGER.debug("Error with " + e);
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
            if (e.type2 == null) e.type2 = PokeType.unknown;
            if (!base._loaded_interactions.isEmpty() && e._loaded_interactions.isEmpty())
                e._loaded_interactions.addAll(base._loaded_interactions);
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
            for (final PokedexEntry e : Database.allFormes) if (e.getPokedexNb() == entry.getPokedexNb()) set.add(e);
            formes.addAll(set);
            /** If only 1 form, no point in processing this. */
            if (formes.size() > 1)
            {
                formes.sort(Database.COMPARATOR);
                /**
                 * First init the formes, to copy the stuff over from the
                 * current base forme if needed.
                 */
                Database.initFormes(formes, entry);
                /**
                 * Then Check if the entry should be replaced with a gender
                 * version
                 */
                Database.checkGenderFormes(formes, vars);
                /**
                 * Then check if the base form, or any others, are dummy forms,
                 * and replace them.
                 */
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
                if (!registry.containsKey(sound) && !sound.getNamespace().equals("minecraft"))
                {
                    event.setRegistryName(sound);
                    registry.register(event);
                }
            }
            // Register sound on target
            if (entry.soundEffectTarget != null)
            {
                final ResourceLocation sound = new ResourceLocation(entry.soundEffectTarget);
                final SoundEvent event = new SoundEvent(sound);
                if (!registry.containsKey(sound) && !sound.getNamespace().equals("minecraft"))
                {
                    event.setRegistryName(sound);
                    registry.register(event);
                }
            }
            // Register sounds for the animations
            if (entry.animations != null) for (final AnimationJson anim : entry.animations) if (anim.sound != null)
            {
                final ResourceLocation sound = new ResourceLocation(anim.sound);
                final SoundEvent event = new SoundEvent(sound);
                if (!registry.containsKey(sound) && !sound.getNamespace().equals("minecraft"))
                {
                    event.setRegistryName(sound);
                    registry.register(event);
                }
            }
        }

        // Register sound events from config.
        for (final String var : PokecubeCore.getConfig().customSounds)
        {
            final ResourceLocation sound = new ResourceLocation(var);
            final SoundEvent event = new SoundEvent(sound);
            if (!registry.containsKey(sound) && !sound.getNamespace().equals("minecraft"))
            {
                event.setRegistryName(sound);
                registry.register(event);
            }
        }
    }

    public static void loadRecipes()
    {
        for (final IRecipeParser parser : XMLRecipeHandler.recipeParsers.values()) parser.init();
        final Collection<ResourceLocation> resources = PackFinder.getJsonResources("database/recipes");
        for (final ResourceLocation file : resources) try
        {
            final Reader reader = new InputStreamReader(PackFinder.getStream(file));
            final JsonObject database = PokedexEntryLoader.gson.fromJson(reader, JsonObject.class);
            reader.close();

            // Handle lists of recipes in the json
            if (database.has("recipes") && database.get("recipes").isJsonArray())
                for (final JsonElement drop : database.get("recipes").getAsJsonArray())
                    if (drop.isJsonObject()) XMLRecipeHandler.addRecipe(drop.getAsJsonObject());

            // Handle single json as a recipe
            if (database.has("type") || database.has("handler")) XMLRecipeHandler.addRecipe(database);

        }
        catch (final FileNotFoundException e)
        {
            PokecubeCore.LOGGER.info("No Custom Recipes of name {}", file);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with " + file, e);
        }
    }

    public static void loadRewards(final String input)
    {
        if (XMLRewardsHandler.loadedRecipes.add(input)) Database.loadRewards(new StringReader(input));
    }

    private static void loadRewards(final Reader reader)
    {
        final XMLRewards database = PokedexEntryLoader.gson.fromJson(reader, XMLRewards.class);
        for (final XMLReward drop : database.recipes) XMLRewardsHandler.addReward(drop);
    }

    private static void loadRewards()
    {
        final Collection<ResourceLocation> resources = PackFinder.getJsonResources("database/rewards");
        for (final ResourceLocation file : resources) try
        {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(PackFinder.getStream(file)));
            final StringBuffer sb = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) sb.append(str);
            reader.close();
            Database.loadRewards(sb.toString());
        }
        catch (final FileNotFoundException e)
        {
            PokecubeCore.LOGGER.info("No Custom Rewards of name {}", file);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with " + file, e);
        }
    }

    private static void loadStarterPack()
    {
        try
        {
            final Collection<ResourceLocation> resources = PackFinder.getJsonResources("database/starterpack");
            boolean valid = false;
            final List<ItemStack> kit = Lists.newArrayList();
            for (final ResourceLocation file : resources)
            {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(PackFinder.getStream(file)));
                final XMLStarterItems database = PokedexEntryLoader.gson.fromJson(reader, XMLStarterItems.class);
                reader.close();
                valid = true;
                for (final Drop drop : database.drops)
                {
                    final ItemStack stack = PokedexEntryLoader.getStackFromDrop(drop);
                    if (!stack.isEmpty()) kit.add(stack);
                }
            }
            if (valid)
            {
                // Only clear this if things have not failed earlier
                Database.starterPack.clear();
                Database.starterPack.addAll(kit);
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
        for (final PokedexEntry e : Pokedex.getInstance().getRegisteredEntries()) if (e.base) Database.addEntry(e);
        int dummies = 0;
        /** find non-registered entries to remove later. */
        for (final PokedexEntry p : Database.allFormes) if (!Pokedex.getInstance().getRegisteredEntries().contains(p))
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
            for (final PokedexEntry e : messageList) builder.append("\n-   ").append(e.getName());
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
        if (removedNums.size() > 0) PokecubeCore.LOGGER.debug("Removed " + toRemove);

        toRemove.clear();
    }

    public static boolean needs_reload = true;

    public static void onResourcesReloaded()
    {
        // If this was not done, then lisener never reloaded correctly, so we
        // don't want to do anything here.
        if (!Database.listener.loaded) return;

        if (!needs_reload)
        {
            PokecubeCore.LOGGER.info("Skipping Load, too soon since last load.");
            return;
        }

        long time = System.nanoTime();
        StructureSpawnPresetLoader.loadDatabase();
        long dt = System.nanoTime() - time;
        PokecubeCore.LOGGER.info("Resource Stage 1: {}s", dt / 1e9d);

        // In this case, we are not acually a real datapack load, just an
        // initial world check thing.
        if (!StructureSpawnPresetLoader.validLoad) return;
        time = System.nanoTime();

        // Load these first, as they do some checks for full data loading, and
        // they also don't rely on anything else, they just do string based tags
        DataHelpers.onResourcesReloaded();
        dt = System.nanoTime() - time;
        PokecubeCore.LOGGER.info("Resource Stage 2: {}s", dt / 1e9d);
        time = System.nanoTime();

        // In this case, we are not acually a real datapack load, just an
        // initial world check thing.
        if (!Tags.BREEDING.validLoad) return;

        BerryGenManager.parseConfig();

        Database.spawnables.clear();
        PokedexInspector.rewards.clear();
        XMLRewardsHandler.loadedRecipes.clear();

        // Clear the values that will be set below
        for (final PokedexEntry p : Database.allFormes)
        {
            p.related.clear();
            p._childNb = null;
            p.noItemForm = null;
            p.setSpawnData(null);
        }

        // Reload the database incase things are adjusted
        PokedexEntryLoader.onReloaded();
        // Also register bulk defined spawns
        PokemobSpawns.registerSpawns();
        // And the spawn masks
        SpawnRateMask.init();

        Database.loadStarterPack();
        Database.loadRecipes();
        Database.loadRewards();

        /** Initialize relations, prey, children. */
        for (final PokedexEntry p : Database.allFormes) p.initRelations();
        for (final PokedexEntry p : Database.allFormes) p.initPrey();
        // Children last, as relies on relations.
        for (final PokedexEntry p : Database.allFormes) p.getChild();
        // Final setup of things
        for (final PokedexEntry entry : Database.getSortedFormes()) entry.onResourcesReloaded();

        // Some debug messages
        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            final Set<String> ourTags = Tags.BREEDING.lookupTags(entry.getTrimmedName());
            if (Tags.BREEDING.validLoad && entry.breeds && ourTags.isEmpty())
                PokecubeCore.LOGGER.debug("No egg group assigned for {}", entry.getTrimmedName());
        }
        for (final PokedexEntry entry : Database.getSortedFormes())
            if (entry.lootTable == null && !(entry.isMega() || entry.isGMax()))
                PokecubeCore.LOGGER.debug("Missing loot table for {}", entry.getTrimmedName());

        // This gets re-set to true if listener hears a reload
        Database.listener.loaded = false;
        Database.needs_reload = false;
        dt = System.nanoTime() - time;
        PokecubeCore.LOGGER.info("Resource Stage 3: {}s", dt / 1e9d);
    }

    /**
     * Loads in spawns, drops, held items and starter packs, as well as
     * initializing things like children, evolutions, etc
     */
    public static void onLoadComplete()
    {
        Database.listener.loaded = true;
        Database.needs_reload = true;
        Database.onResourcesReloaded();
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
        DefaultFormeHolder._main_init_ = true;
    }

    public static Set<PackResources> customPacks = Sets.newHashSet();

    public static PackListener listener = new PackListener();

    public static void loadCustomPacks(final boolean applyToManager)
    {
        Database.customPacks.clear();
        final PackRepository resourcePacks = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource());
        final PackFinder finder = new PackFinder(
                (name, component, bool, supplier, metadata, source, p_143900_, hidden) ->
                {
                    return new Pack(name, component, bool, supplier, metadata, PackType.SERVER_DATA, source, p_143900_,
                            hidden);
                });
        resourcePacks.addPackFinder(finder);
        for (final PackResources info : finder.allPacks) try
        {
            if (applyToManager)
            {
                PokecubeCore.LOGGER.debug("Loading Pack: " + info.getName());
                ((SimpleReloadableResourceManager) Database.resourceManager).add(info);
            }
            // Only add the zips or folders here, jars get properly added by
            // forge to the real resourcemanager later
            else if (!info.getName().endsWith(".jar")) Database.customPacks.add(info);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.fatal("Error with pack " + info.getName(), e);
        }
        resourcePacks.close();
    }

    /**
     * This is called before generating any items. This ensures that the types
     * are loaded correctly.
     */
    public static void preInit()
    {
        PokecubeCore.LOGGER.debug("Database preInit()");
        // Initialize the resourceloader.
        Database.loadCustomPacks(true);

        // Register the dex inspector
        MinecraftForge.EVENT_BUS.register(PokedexInspector.class);

        // Load in the combat types first.
        CombatTypeLoader.loadTypes();
        // Load in the various databases, starting with moves, then pokemobs.
        MovesAdder.registerMoves();
        PokemobsDatabases.preInitLoad();
        // Finally load in the abilities
        AbilityManager.init();

        PokecubeCore.LOGGER.debug("Loaded all databases");
    }

    public static void preInitMoves()
    {
        MovesDatabases.preInitLoad();
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