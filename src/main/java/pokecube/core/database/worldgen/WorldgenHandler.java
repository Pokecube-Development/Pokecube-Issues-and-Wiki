package pokecube.core.database.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.database.pokedex.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.spawns.SpawnBiomeMatcher;
import pokecube.core.world.gen.jigsaw.CustomJigsawStructure;
import thut.api.util.JsonUtil;

public class WorldgenHandler
{
    public static final Gson GSON = JsonUtil.gson;

    public static class Options
    {
        public static final Codec<Options> CODEC = Codec.STRING.comapFlatMap(Options::decodeOptions,
                Options::serialize);

        private static DataResult<Options> decodeOptions(final String encoded)
        {
            return DataResult.success(Options.deserialize(encoded));
        }

        public int weight = 1;
        public String flag = "";
        public String proc_list = "";
        public int dy = 0;

        public boolean rigid = true;
        public boolean ignoreAir = false;
        public boolean filler = false;
        public boolean water = false;

        public boolean override = false;
        public boolean needs_children = false;

        public boolean base_override = false;

        public Map<String, JsonElement> extra = Maps.newHashMap();

        public String serialize()
        {
            return WorldgenHandler.GSON.toJson(this);
        }

        public static Options deserialize(final String structstring)
        {
            Options result = new Options();
            try
            {
                result = WorldgenHandler.GSON.fromJson(structstring, Options.class);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error loading options for string {}", structstring, e);
            }
            return result;
        }
    }

    public static class JigSawPool
    {
        public String name;
        public String target = "empty";
        public String biomeType = "none";
        public String proc_list = "";
        public List<String> options = Lists.newArrayList();
        public boolean rigid = true;
        public boolean ignoreAir = false;
        public boolean water = false;
        public boolean filler = false;
        public boolean base_override = false;
        public List<String> includes = Lists.newArrayList();
    }

    public static class JigSawConfig
    {
        public static final Codec<JigSawConfig> CODEC = Codec.STRING.comapFlatMap(JigSawConfig::decodeConfig,
                JigSawConfig::serialize);

        private static DataResult<JigSawConfig> decodeConfig(final String encoded)
        {
            return DataResult.success(JigSawConfig.deserialize(encoded));
        }

        public String name;
        public String root;
        public int offset = 1;

        // This is max depth of the structure, ie how many times it can add new
        // jigsaws onto a previous part.
        public int size = 4;

        // These are for the rarity of the structure
        public int distance = 8;
        public int separation = 4;

        // This defines if we need all biomes in the checked area to match. if
        // this is -1, it will allow spawn if any biome matches, otherwise it
        // will require all biomes to match within this number of blocks.
        public int needed_space = -1;

        public String proc_list = "";

        public String type = "";
        public String biomeType = "none";
        public SpawnRule spawn;
        public boolean surface = true;
        public boolean base_under = true;
        public boolean base_override = false;
        public boolean water = false;
        public boolean air = false;
        public boolean allow_void = false;
        public int minY = 5;
        public int height = 0;
        public int variance = 50;
        public int priority = 100;
        public int spacing = 4;
        public int seed = -1;
        public List<String> needed_once = Lists.newArrayList();
        public List<String> dimBlacklist = Lists.newArrayList();
        public List<String> dimWhitelist = Lists.newArrayList();

        private final List<ResourceKey<Level>> _blacklisted = Lists.newArrayList();
        private final List<ResourceKey<Level>> _whitelisted = Lists.newArrayList();

        public String serialize()
        {
            return WorldgenHandler.GSON.toJson(this);
        }

        public void initSettings()
        {
            // Make seed based on name
            if (this.seed == -1)
            {
                this.seed = this.name.hashCode();
                // Seed must be positive apparently.
                if (this.seed < 0) this.seed *= -1;
                final Random rand = new Random(this.seed);
                // Ensure the seed is more random
                for (int i = 0; i < 100; i++) this.seed = rand.nextInt();
                if (this.seed < 0) this.seed *= -1;
            }
        }

        public static JigSawConfig deserialize(final String structstring)
        {
            return WorldgenHandler.GSON.fromJson(structstring, JigSawConfig.class);
        }

        public boolean isBlackisted(final ResourceKey<Level> dim)
        {
            if (this._blacklisted.size() != this.dimBlacklist.size()
                    || this._whitelisted.size() != this.dimWhitelist.size())
            {
                this._blacklisted.clear();
                this._whitelisted.clear();
                for (final String s : this.dimBlacklist)
                    this._blacklisted.add(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(s)));
                for (final String s : this.dimWhitelist)
                    this._whitelisted.add(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(s)));
            }
            if (this._whitelisted.contains(dim)) return false;
            return this._blacklisted.contains(dim) || WorldgenHandler.SOFTBLACKLIST.contains(dim);
        }

        public SpawnBiomeMatcher getMatcher()
        {
            if (this.spawn == null) return null;
            return SpawnBiomeMatcher.get(spawn);
        }
    }

    public static class Structures
    {
        public List<JigSawPool> pools = Lists.newArrayList();
        public List<JigSawConfig> jigsaws = Lists.newArrayList();
    }

    public static final DeferredRegister<StructureFeature<?>> STRUCTURE_FEATURES = DeferredRegister
            .create(ForgeRegistries.STRUCTURE_FEATURES, PokecubeCore.MODID);

    public static Map<String, CustomJigsawStructure> structs = Maps.newConcurrentMap();

    private static List<StructureFeature<?>> SORTED_PRIOR_LIST = Lists.newArrayList();

    private static Map<ResourceLocation, Integer> SPACENEEDS = Maps.newConcurrentMap();

    private static Map<ResourceLocation, StructureFeature<?>> FEATURELOOKUP = Maps.newConcurrentMap();

    public static List<JigSawPool> BASE_OVERRIDES = Lists.newArrayList();
    public static Set<StructureFeature<?>> HAS_BASE_OVERRIDES = Sets.newHashSet();

    public static Set<StructureFeature<?>> HAS_BASES = Sets.newHashSet();

    public static Set<ResourceKey<Level>> SOFTBLACKLIST = Sets.newHashSet();

    private static void initSpaceMap()
    {
        synchronized (WorldgenHandler.SORTED_PRIOR_LIST)
        {
            if (WorldgenHandler.SORTED_PRIOR_LIST.isEmpty())
            {
                WorldgenHandler.SORTED_PRIOR_LIST.addAll(StructureFeature.STEP.keySet());
                WorldgenHandler.SORTED_PRIOR_LIST.sort((s1, s2) -> {
                    int p1 = 5;
                    int p2 = 5;
                    if (s1 instanceof CustomJigsawStructure) p1 = ((CustomJigsawStructure) s1).priority;
                    if (s2 instanceof CustomJigsawStructure) p2 = ((CustomJigsawStructure) s2).priority;
                    return Integer.compare(p1, p2);
                });
                WorldgenHandler.SORTED_PRIOR_LIST.forEach(s -> {
                    int space = 6;
                    if (s instanceof CustomJigsawStructure) space = ((CustomJigsawStructure) s).spacing;
                    WorldgenHandler.SPACENEEDS.put(s.getRegistryName(), space);
                    WorldgenHandler.FEATURELOOKUP.put(s.getRegistryName(), s);
                });
            }
        }
    }

    public static List<StructureFeature<?>> getSortedList()
    {
        WorldgenHandler.initSpaceMap();
        return WorldgenHandler.SORTED_PRIOR_LIST;
    }

    public static Integer getNeededSpace(final ResourceLocation s)
    {
        WorldgenHandler.initSpaceMap();
        return WorldgenHandler.SPACENEEDS.get(s);
    }

    public static StructureFeature<?> getFeature(final ResourceLocation s)
    {
        synchronized (FEATURELOOKUP)
        {
            WorldgenHandler.initSpaceMap();
        }
        return WorldgenHandler.FEATURELOOKUP.get(s);
    }

    public final String MODID;

    public static WorldgenHandler INSTANCE;

    public final Map<String, Holder<StructureTemplatePool>> patterns = Maps.newHashMap();

    public final Map<JigSawConfig, CustomJigsawStructure> toConfigure = Maps.newHashMap();

    private final Map<CustomJigsawStructure, Set<JigSawConfig>> variants = Maps.newHashMap();

    private final Map<Holder<ConfiguredStructureFeature<?, ?>>, Set<ResourceKey<Biome>>> structure_biomes = Maps
            .newHashMap();

    private final Structures defaults = new Structures();

    public WorldgenHandler(final IEventBus bus)
    {
        this(PokecubeCore.MODID, bus);
    }

    private WorldgenHandler(final String modid, final IEventBus bus)
    {
        this.MODID = modid;
        MinecraftForge.EVENT_BUS.register(this);
        WorldgenHandler.INSTANCE = this;
    }

    public static void setupAll()
    {
        WorldgenHandler.INSTANCE.setup();
    }


    protected void setup()
    {
    }

    protected void setup(final CustomJigsawStructure structure, final JigSawConfig config)
    {
        /*
         * We need to add our structures into the map in Structure alongside
         * vanilla structures or else it will cause errors. Called by
         * registerStructure. If the registration is setup properly for the
         * structure, getRegistryName() should never return null.
         */
        StructureFeature.STEP.put(structure, Decoration.SURFACE_STRUCTURES);

        /*
         * Will add land at the base of the structure like it does for Villages
         * and Outposts. Doesn't work well on structure that have pieces stacked
         * vertically or change in heights.
         */
        if (config.base_under) WorldgenHandler.forceVillageFeature(structure);
    }

    /**
     * Will go into the world's chunkgenerator and manually add our structure
     * spacing. If the spacing is not added, the structure doesn't spawn. Use
     * this for dimension blacklists for your structure. (Don't forget to
     * attempt to remove your structure too from the map if you are blacklisting
     * that dimension! It might have your structure in it already.) Basically
     * use this to make absolutely sure the chunkgenerator can or cannot spawn
     * your structure.
     */
    @SubscribeEvent
    public void addDimensionalSpacing(final WorldEvent.Load event)
    {
        if (event.getWorld().isClientSide()) return;

        if (event.getWorld() instanceof ServerLevel)
        {
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGH)
    public void regBiomes(final BiomeLoadingEvent event)
    {
    }


    private static void forceVillageFeature(final StructureFeature<?> feature)
    {
        WorldgenHandler.HAS_BASES.add(feature);
    }

    public static ArrayList<ConfiguredStructureFeature<?, ?>> getSortedConfiguredList()
    {
        // TODO Auto-generated method stub
        return Lists.newArrayList();
    }
}
