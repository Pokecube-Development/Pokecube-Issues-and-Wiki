package pokecube.core.database;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.data.effects.PokemobEffects;
import pokecube.api.data.pokedex.DefaultFormeHolder;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.events.init.InitDatabase;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.database.pokedex.JsonPokedexEntry;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.resources.PackListener;
import pokecube.core.database.rewards.XMLRewardsHandler;
import pokecube.core.database.rewards.XMLRewardsHandler.XMLReward;
import pokecube.core.database.rewards.XMLRewardsHandler.XMLRewards;
import pokecube.core.database.spawns.PokemobSpawns;
import pokecube.core.database.spawns.SpawnPresets;
import pokecube.core.database.spawns.SpawnRateMask;
import pokecube.core.database.spawns.SpawnRegion;
import pokecube.core.database.tags.Tags;
import pokecube.core.database.types.CombatTypeLoader;
import pokecube.core.database.worldgen.StructureSpawnPresetLoader;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.moves.implementations.MovesAdder;
import thut.api.data.DataHelpers;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;
import thut.core.xml.bind.annotation.XmlElement;
import thut.core.xml.bind.annotation.XmlRootElement;
import thut.lib.ResourceHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Database
{
    private static final Pattern ALLOWED = Pattern.compile("([^a-z0-9])");

    public static String trim_loose(String name)
    {
        // ROOT locale to prevent issues with turkish letters.
        name = name.toLowerCase(Locale.ROOT).trim();
        // Replace all non-alphanumeric
        name = ALLOWED.matcher(name).replaceAll("");
        return name;
    }

    @XmlRootElement(name = "Items")
    public static class XMLStarterItems
    {
        @XmlElement(name = "Item")
        private final List<JsonElement> drops = Lists.newArrayList();
    }

    public static class ReloadListener implements PreparableReloadListener
    {
        public static final PreparableReloadListener INSTANCE = new ReloadListener();

        @Override
        public final CompletableFuture<Void> reload(final PreparableReloadListener.PreparationBarrier stage,
                final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler,
                final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor)
        {
            return CompletableFuture.supplyAsync(() ->
                    this.prepare(resourceManager, preparationsProfiler), backgroundExecutor)
                    .thenCompose(stage::wait).thenAcceptAsync((object) -> {
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
            Database.listener.loaded = needs_reload = true;
            Database.listener.add(resourceManagerIn);
            Database.onResourcesReloaded();
        }
    }

    public static List<ItemStack> starterPack = Lists.newArrayList();
    public static Int2ObjectOpenHashMap<PokedexEntry> data = new Int2ObjectOpenHashMap<>();
    public static HashMap<String, PokedexEntry> data2 = new HashMap<>();
    public static HashSet<PokedexEntry> allFormes = new HashSet<>();
    private static final List<PokedexEntry> sortedFormes = Lists.newArrayList();
    private static final List<String> sortedFormNames = Lists.newArrayList();
    public static HashMap<Integer, PokedexEntry> baseFormes = new HashMap<>();
    public static HashMap<Integer, PokedexEntry> dummyMap = new HashMap<>();
    public static HashMap<PokedexEntry, List<FormeHolder>> customModels = new HashMap<>();
    public static HashMap<ResourceLocation, PokedexEntry> formeToEntry = new HashMap<>();
    public static Map<String, FormeHolder> formeHoldersByKey = new HashMap<>();

    public static Int2ObjectOpenHashMap<List<PokedexEntry>> formLists = new Int2ObjectOpenHashMap<>();

    public static List<PokedexEntry> spawnables = new ArrayList<>();
    public static Map<PokeType, List<PokedexEntry>> spawnablesByType = new HashMap<>();

    public static final PokedexEntry missingno = new PokedexEntry(0, "MissingNo", false);

    public static final Comparator<PokedexEntry> COMPARATOR = (o1, o2) -> {
        int diff = o1.getPokedexNb() - o2.getPokedexNb();
        // Same number, so decide based on forms
        if (diff == 0)
        {
            // Base always first.
            if (o1.base && !o2.base) return -1;
            else if (o2.base && !o1.base) return 1;

            // Gendered forms have priority
            if (o1.isGenderForme && !o2.isGenderForme) return -1;
            else if (o2.isGenderForme && !o1.isGenderForme) return 1;

            // Generated forms last
            if (o1.generated && !o2.generated) return 1;
            else if (o2.generated && !o1.generated) return -1;

            diff = o1.getName().compareTo(o2.getName());
        }
        return diff;
    };

    // Init some stuff for the missignno entry.
    static
    {
        Database.missingno.type1 = PokeType.unknown;
        Database.missingno.type2 = PokeType.unknown;
        Database.missingno.base = true;
        Database.missingno.height = 1;
        Database.missingno.width = Database.missingno.length = 0.41f;
        Database.missingno.mobType = 15;
        Database.addEntry(Database.missingno);
    }

    static int lastCount = -1;

    public static ReloadableResourceManager resourceManager = new ReloadableResourceManager(PackType.SERVER_DATA);

    public static PokedexEntry[] starters = {};

    private static boolean checkedStarts = false;

    public static void addEntry(final PokedexEntry entry)
    {
        Database.data.put(entry.getPokedexNb(), entry);
    }

    /**
     * Replaces a dummy base form with the first form in the sorted list.
     */
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
                        if (e.generated) continue;
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
        Database.formeToEntry.put(holder.key, entry);
        Database.formeHoldersByKey.put(holder.loaded_from.key, holder);
        Database.customModels.computeIfAbsent(entry, e->new ArrayList<>()).add(holder);
    }

    public static String convertMoveName(final String moveNameFromBulbapedia)
    {
        return Database.trim(moveNameFromBulbapedia);
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


    public static PokedexEntry checkEntryExists(String name){
        final PokedexEntry ret = null;
        if (name == null) return null;
        name = ThutCore.trim(name);
        if (name.trim().isEmpty()) return null;
        final PokedexEntry test = Database.data2.get(name);
        if (test != null) return test;
        return ret;
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
            Database.starters = starts.toArray(new PokedexEntry[0]);
        }
        return Database.starters;
    }

    /**
     * This loads in the various databases, merges them then makes pokedex entries as needed
     */
    public static void init()
    {
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Database Init()");

        SpawnPresets.init();
        PokemobSpawns.init();

        // Fire load event to let addons do stuff after databases have been
        // loaded.
        ThutCore.FORGE_BUS.post(new InitDatabase.Load());

        // Init the lists of what all forms are loaded.
        Database.initFormLists();

        if (PokecubeCore.getConfig().debug_data)
        {
            // Debug some dummy lists.
            final List<PokedexEntry> dummies = Lists.newArrayList();
            for (final PokedexEntry entry : Database.allFormes) if (entry.dummy) dummies.add(entry);
            dummies.sort(Database.COMPARATOR);
            final StringBuilder builder = new StringBuilder("Dummy Pokedex Entries:");
            for (final PokedexEntry e : dummies) builder.append("\n-   ").append(e.getName());
            PokecubeAPI.logInfo(builder.toString());
        }

        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo(
                "Loaded " + Database.data.size() + " by number, and " + Database.allFormes.size()
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
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Processing " + base + " " + formes);
        for (final PokedexEntry e : formes)
        {
            e.forms.clear();
            for (final PokedexEntry e1 : formes) if (e1 != e) e.forms.put(e1.getTrimmedName(), e1);
            if (base != e)
            {
                try
                {
                    if (e.getBaseForme() == null)
                    {
                        base.copyToForm(e);
                    }
                }
                catch (final Exception e2)
                {
                    PokecubeAPI.LOGGER.error("Error copying data: {} <-> {}", e, base, e2);
                    continue;
                }
                if (e.getWidth() <= 0)
                {
                    e.height = 1;
                    e.width = 1;
                    e.length = 1;
                    e.mobType = 0;
                    e.catchRate = 0;
                    e.mass = 1;
                    PokecubeAPI.logDebug("Error with height for " + e);
                }
                if (e.type1 == null)
                {
                    e.type1 = base.type1;
                    e.type2 = base.type2;
                    if (PokecubeCore.getConfig().debug_data)
                        PokecubeAPI.logInfo("Copied Types from " + base + " to " + e);
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
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Processing Form Lists");
        for (final Map.Entry<Integer, PokedexEntry> vars : Database.baseFormes.entrySet())
        {
            PokedexEntry entry = vars.getValue();
            final List<PokedexEntry> formes = Lists.newArrayList();
            final Set<PokedexEntry> set = Sets.newHashSet();
            set.addAll(entry.forms.values());
            set.add(entry);
            /* Collect all the different forms we can for this mob. */
            for (final PokedexEntry e : Database.allFormes) if (e.getPokedexNb() == entry.getPokedexNb()) set.add(e);
            formes.addAll(set);
            /* If only 1 form, no point in processing this. */
            if (formes.size() > 1)
            {
                formes.sort(Database.COMPARATOR);
                /*
                 * First init the formes, to copy the stuff over from the
                 * current base forme if needed.
                 */
                Database.initFormes(formes, entry);
                /*
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
                    PokecubeAPI.LOGGER.error("Error with typing for " + e + " " + e.getType2());
            }
            if (e.getType2() == null) e.type2 = PokeType.unknown;
            if (e.dummy) dummies++;
        }
        if (PokecubeCore.getConfig().debug_data)
            PokecubeAPI.logInfo("Processed Form Lists, found " + dummies + " Dummy Forms.");
    }

    public static void loadRewards(final String input)
    {
        if (XMLRewardsHandler.loadedRecipes.add(input)) Database.loadRewards(new StringReader(input));
    }

    private static void loadRewards(final Reader reader)
    {
        final XMLRewards database = JsonUtil.gson.fromJson(reader, XMLRewards.class);
        for (final XMLReward drop : database.recipes) XMLRewardsHandler.addReward(drop);
    }

    private static void loadRewards()
    {
        final Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources("database/rewards");
        resources.forEach((file, resource) -> {
            try
            {
                final BufferedReader reader = ResourceHelper.getReader(resource);
                if (reader == null) throw new FileNotFoundException(file.toString());
                final StringBuilder sb = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) sb.append(str);
                reader.close();
                Database.loadRewards(sb.toString());
            }
            catch (final FileNotFoundException e)
            {
                PokecubeAPI.logInfo("No Custom Rewards of name {}", file);
            }
            catch (final Exception e)
            {
                PokecubeAPI.LOGGER.error("Error with rewards file {}", file, e);
            }
        });
    }

    private static void loadStarterPack()
    {

        final Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources("database/starterpack");
        AtomicBoolean valid = new AtomicBoolean(false);
        final List<ItemStack> kit = Lists.newArrayList();
        resources.forEach((file, resource) -> {
            try
            {
                {
                    final BufferedReader reader = ResourceHelper.getReader(resource);
                    if (reader == null) throw new FileNotFoundException(file.toString());
                    final XMLStarterItems database = JsonUtil.gson.fromJson(reader, XMLStarterItems.class);
                    reader.close();
                    valid.set(true);
                    for (final JsonElement drop : database.drops)
                    {
                        final ItemStack stack = PokedexEntryLoader.getStackFromDrop(drop);
                        if (!stack.isEmpty()) kit.add(stack);
                    }
                }
            }
            catch (final Exception e)
            {
                PokecubeAPI.LOGGER.error("Error Loading Starter Pack", e);
            }
        });

        if (valid.get())
        {
            // Only clear this if things have not failed earlier
            Database.starterPack.clear();
            Database.starterPack.addAll(kit);
        }

    }

    /** does some final cleanup work for removing un-registered entries */
    public static void postInit()
    {
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Post Init of Database.");
        final List<PokedexEntry> toRemove = new ArrayList<>();
        final Set<Integer> removedNums = Sets.newHashSet();
        for (final PokedexEntry e : Pokedex.getInstance().getRegisteredEntries()) if (e.base) Database.addEntry(e);
        int dummies = 0;
        /* find non-registered entries to remove later. */
        for (final PokedexEntry p : Database.allFormes)
            if (!Pokedex.getInstance().getRegisteredEntries().contains(p))
            {
                if (p.dummy) dummies++;
                else removedNums.add(p.getPokedexNb());
                toRemove.add(p);
            }
        toRemove.sort(Database.COMPARATOR);
        final List<PokedexEntry> messageList = Lists.newArrayList(toRemove);
        messageList.removeIf(t -> t.dummy);
        if (PokecubeCore.getConfig().debug_data)
        {
            messageList.sort(Database.COMPARATOR);
            final StringBuilder builder = new StringBuilder("UnRegistered Pokedex Entries:");
            for (final PokedexEntry e : messageList) builder.append("\n-   ").append(e.getName());
            PokecubeAPI.logDebug(builder.toString());
        }
        /* Remove the non-registered entries found earlier */
        for (final PokedexEntry p : toRemove)
        {
            if (p == Database.getEntry(p.pokedexNb) && !p.dummy)
            {
                if (p.dummy) PokecubeAPI.logInfo(
                        "Error with " + p + ", It is still listed as base forme, as well as being dummy.");
                Database.data.remove(p.pokedexNb);
                Database.baseFormes.remove(p.pokedexNb);
                Database.formLists.remove(p.pokedexNb);
            }
            else if (Database.formLists.containsKey(p.pokedexNb)) Database.formLists.get(p.pokedexNb).remove(p);
        }

        /* Cleanup evolutions which are not actually in game. */
        for (final PokedexEntry e : Database.allFormes)
        {
            final List<EvolutionData> invalidEvos = Lists.newArrayList();
            for (final EvolutionData d : e.evolutions)
                if (!Pokedex.getInstance().getRegisteredEntries().contains(d.evolution)) invalidEvos.add(d);
            e.evolutions.removeAll(invalidEvos);
        }

        toRemove.forEach(Database.allFormes::remove);
        if (PokecubeCore.getConfig().debug_data)
        {
            PokecubeAPI.logInfo("Removed " + removedNums.size() + " Missing Pokemon and " + (toRemove.size() - dummies)
                    + " missing Formes");
            if (!removedNums.isEmpty()) PokecubeAPI.logInfo("Removed " + toRemove);
        }

        toRemove.clear();
    }

    public static boolean needs_reload = true;

    public static void onResourcesReloaded()
    {
        long time = System.nanoTime();
        StructureSpawnPresetLoader.loadDatabase();
        long dt = System.nanoTime() - time;
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Resource Stage 1: {}s", dt / 1e9d);

        time = System.nanoTime();

        // Load these first, as they do some checks for full data loading, and
        // they also don't rely on anything else, they just do string based tags
        DataHelpers.onResourcesReloaded();

        // Also load in the pokemob material effects.
        PokemobEffects.loadMaterials();

        dt = System.nanoTime() - time;
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Resource Stage 2: {}s", dt / 1e9d);

        time = System.nanoTime();

        BerryGenManager.parseConfig();

        Database.spawnables.clear();
        PokedexInspector.rewards.clear();
        XMLRewardsHandler.loadedRecipes.clear();

        // Clear the values that will be set below
        for (final PokedexEntry p : Database.getSortedFormes())
        {
            p.related.clear();
            p._childNb = null;
            p.noItemForm = null;
            p.setSpawnData(null);
        }

        // Reload the database incase things are adjusted
        PokedexEntryLoader.onReloaded();
        // And the spawn masks
        SpawnRateMask.init();

        dt = System.nanoTime() - time;
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Resource Stage 3: {}s", dt / 1e9d);
        time = System.nanoTime();

        Database.loadStarterPack();
        Database.loadRewards();

        dt = System.nanoTime() - time;
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Resource Stage 4: {}s", dt / 1e9d);
        time = System.nanoTime();

        /* Initialize relations, prey, children. */
        for (final PokedexEntry p : Database.getSortedFormes())
            p.initRelations(PokecubeCore.proxy.getRegistries());
        // Finally prey and children, as they depend on relations
        for (final PokedexEntry p : Database.getSortedFormes())
        {
            p.getChild();
            p.initPrey();
        }

        // Final setup of things
        for (final PokedexEntry entry : Database.getSortedFormes()) entry.onResourcesReloaded();

        // Regenerate the starter array
        Database.checkedStarts = false;
        Database.getStarters();

        // Some debug messages
        if (PokecubeCore.getConfig().debug_data)
        {
            for (final PokedexEntry entry : Database.getSortedFormes())
            {
                final Set<String> ourTags = Tags.BREEDING.lookupTags(entry.getTrimmedName());
                if (Tags.BREEDING.validLoad && entry.breeds && ourTags.isEmpty() && !entry.generated)
                    PokecubeAPI.logInfo("No egg group assigned for {}", entry.getTrimmedName());
            }
            for (final PokedexEntry entry : Database.getSortedFormes())
                if (entry.lootTable == null && !entry.generated)
                    PokecubeAPI.logInfo("Missing loot table for {}", entry.getTrimmedName());
        }

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

        Database.spawnablesByType.clear();
        Database.spawnables.forEach(entry -> {
            var type = entry.type1;
            if (type != PokeType.unknown)
            {
                var list = spawnablesByType.computeIfAbsent(type, key -> new ArrayList<>());
                list.add(entry);
            }
            type = entry.type2;
            if (type != PokeType.unknown)
            {
                var list = spawnablesByType.computeIfAbsent(type, key -> new ArrayList<>());
                list.add(entry);
            }
        });
        SpawnRegion.initAllSpawns();

        // This gets re-set to true if listener hears a reload
        Database.listener.loaded = false;
        Database.needs_reload = false;
        dt = System.nanoTime() - time;
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Resource Stage 5: {}s", dt / 1e9d);
    }

    /**
     * Loads in spawns, drops, held items and starter packs, as well as initializing things like children, evolutions,
     * etc
     */
    public static void onLoadComplete()
    {
        Database.listener.loaded = true;
        Database.needs_reload = true;
        DefaultFormeHolder._main_init_ = true;
    }

    public static Set<PackResources> customPacks = Sets.newHashSet();

    public static PackListener listener = new PackListener();

    public static void loadCustomPacks(final boolean applyToManager)
    {
        Database.customPacks.clear();
        //        TODO: Fix
        List<Supplier<PackResources>> packs = applyToManager
                ? PackFinder.DEFAULT_FINDER.allPacks
                : PackFinder.DEFAULT_FINDER.folderPacks;
        for (final Supplier<PackResources> infoS : packs)
            try
            {
                var info = infoS.get();
                if (info == null)
                {
                    continue;
                }
                // This initialises the info, for the caching system.
                info.getNamespaces(PackType.SERVER_DATA);
                if (applyToManager)
                {
                    if (PokecubeCore.getConfig().debug_data)
                    {
                        PokecubeAPI.logInfo("Loading Pack: " + info.getClass().getName());
                        PokecubeAPI.logInfo("Namespaces: " + info.getNamespaces(PackType.SERVER_DATA));
                    }
                    PackListener.addPack(info, Database.resourceManager);
                }
                // Only add the zips or folders here, jars get properly added by
                // forge to the real resourcemanager later
                else if (!info.getClass().getName().endsWith(".jar")) Database.customPacks.add(info);
            }
            catch (final Exception e)
            {
                PokecubeAPI.LOGGER.fatal("Error with pack {}", infoS, e);
            }
    }

    /**
     * This is called before generating any items. This ensures that the types are loaded correctly.
     */
    public static void preInit()
    {
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Database preInit()");

        // Initialize the resourceloader.
        Database.loadCustomPacks(true);

        // Register the dex inspector
        ThutCore.FORGE_BUS.register(PokedexInspector.class);

        // Load in the combat types first.
        CombatTypeLoader.loadTypes();
        // Load in the various databases, starting with moves, then pokemobs.
        MovesAdder.registerMoves();
        JsonPokedexEntry.loadPokedex();
        // Finally load in the abilities
        AbilityManager.init();

        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Loaded all databases");
    }

    public static String trim(final String name)
    {
        return ThutCore.trim(name);
    }
}