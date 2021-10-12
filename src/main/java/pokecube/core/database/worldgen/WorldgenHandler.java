package pokecube.core.database.worldgen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElementType;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.PokecubeCore;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.gen.WorldgenFeatures;
import pokecube.core.world.gen.jigsaw.CustomJigsawPiece;
import pokecube.core.world.gen.jigsaw.CustomJigsawStructure;
import pokecube.core.world.gen.jigsaw.JigsawConfig;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.core.common.ThutCore;

public class WorldgenHandler
{
    public static final Gson GSON;

    static
    {
        GSON = new GsonBuilder().registerTypeAdapter(QName.class, new TypeAdapter<QName>()
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
        }).create();
    }

    public static class Options
    {
        public static final Codec<Options> CODEC = Codec.STRING.comapFlatMap(Options::decodeOptions,
                Options::serialize);

        private static DataResult<Options> decodeOptions(final String encoded)
        {
            return DataResult.success(Options.deserialize(encoded));
        }

        public int    weight    = 1;
        public String flag      = "";
        public String proc_list = "";
        public int    dy        = 0;

        public boolean rigid     = true;
        public boolean ignoreAir = true;
        public boolean filler    = false;
        public boolean water     = false;

        public boolean override = false;

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
            catch (final JsonSyntaxException e)
            {
                PokecubeCore.LOGGER.error("Error loading options for string {}", structstring);
                e.printStackTrace();
            }
            return result;
        }
    }

    public static class JigSawPool
    {
        public String       name;
        public String       target    = "empty";
        public String       biomeType = "none";
        public String       proc_list = "";
        public List<String> options   = Lists.newArrayList();
        public boolean      rigid     = true;
        public boolean      ignoreAir = true;
        public boolean      water     = false;
        public boolean      filler    = false;
        public List<String> includes  = Lists.newArrayList();
    }

    public static class JigSawConfig
    {
        public static final Codec<JigSawConfig> CODEC = Codec.STRING.comapFlatMap(JigSawConfig::decodeConfig,
                JigSawConfig::serialize);

        private static DataResult<JigSawConfig> decodeConfig(final String encoded)
        {
            return DataResult.success(JigSawConfig.deserialize(encoded));
        }

        public String       name;
        public String       root;
        public int          offset       = 1;
        public int          size         = 4;
        public int          distance     = 8;
        public int          separation   = 4;
        public String       type         = "";
        public String       biomeType    = "none";
        public SpawnRule    spawn;
        public boolean      surface      = true;
        public boolean      base_under   = true;
        public boolean      water        = false;
        public boolean      air          = false;
        public boolean      allow_void   = false;
        public int          minY         = 5;
        public int          height       = 0;
        public int          variance     = 50;
        public int          priority     = 100;
        public int          spacing      = 4;
        public int          seed         = -1;
        public List<String> needed_once  = Lists.newArrayList();
        public List<String> dimBlacklist = Lists.newArrayList();
        public List<String> dimWhitelist = Lists.newArrayList();

        private final List<ResourceKey<Level>> _blacklisted = Lists.newArrayList();
        private final List<ResourceKey<Level>> _whitelisted = Lists.newArrayList();

        public SpawnBiomeMatcher _matcher;

        public String serialize()
        {
            return WorldgenHandler.GSON.toJson(this);
        }

        public StructureFeatureConfiguration toSettings()
        {
            // Make seed based on name
            if (this.seed == -1)
            {
                this.seed = this.name.hashCode();
                // Seed must be positive apparently.
                if (this.seed < 0) this.seed *= -1;
                final Random rand = new Random(this.seed);
                // Ensure the seed is more random
                for (int i = 0; i < 100; i++)
                    this.seed = rand.nextInt();
                if (this.seed < 0) this.seed *= -1;
            }
            return new StructureFeatureConfiguration(this.distance, this.separation, this.seed);
        }

        public static JigSawConfig deserialize(final String structstring)
        {
            return WorldgenHandler.GSON.fromJson(structstring, JigSawConfig.class);
        }

        public boolean isBlackisted(final ResourceKey<Level> dim)
        {
            if (this._blacklisted.size() != this.dimBlacklist.size() || this._whitelisted.size() != this.dimWhitelist
                    .size())
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
    }

    public static class Structures
    {
        public List<JigSawPool>   pools   = Lists.newArrayList();
        public List<JigSawConfig> jigsaws = Lists.newArrayList();
    }

    public static Map<String, CustomJigsawStructure> structs = Maps.newConcurrentMap();

    private static List<StructureFeature<?>> SORTED_PRIOR_LIST = Lists.newArrayList();

    private static Map<StructureFeature<?>, Integer> SPACENEEDS = Maps.newConcurrentMap();

    public static Set<ResourceKey<Level>> SOFTBLACKLIST = Sets.newHashSet();

    private static void initSpaceMap()
    {
        synchronized (WorldgenHandler.SORTED_PRIOR_LIST)
        {
            if (WorldgenHandler.SORTED_PRIOR_LIST.isEmpty())
            {
                WorldgenHandler.SORTED_PRIOR_LIST.addAll(StructureFeature.STEP.keySet());
                WorldgenHandler.SORTED_PRIOR_LIST.sort((s1, s2) ->
                {
                    int p1 = 5;
                    int p2 = 5;
                    if (s1 instanceof CustomJigsawStructure) p1 = ((CustomJigsawStructure) s1).priority;
                    if (s2 instanceof CustomJigsawStructure) p2 = ((CustomJigsawStructure) s2).priority;
                    return Integer.compare(p1, p2);
                });
                WorldgenHandler.SORTED_PRIOR_LIST.forEach(s ->
                {
                    int space = 6;
                    if (s instanceof CustomJigsawStructure) space = ((CustomJigsawStructure) s).spacing;
                    WorldgenHandler.SPACENEEDS.put(s, space);
                });
            }
        }
    }

    public static List<StructureFeature<?>> getSortedList()
    {
        WorldgenHandler.initSpaceMap();
        return WorldgenHandler.SORTED_PRIOR_LIST;
    }

    public static Integer getNeededSpace(final StructureFeature<?> s)
    {
        WorldgenHandler.initSpaceMap();
        return WorldgenHandler.SPACENEEDS.get(s);
    }

    public final String MODID;

    private static class BiomeFeature
    {
        public final GenerationStep.Decoration stage;
        public final ConfiguredFeature<?, ?>   feature;

        public final ResourceLocation name;

        public BiomeFeature(final Decoration stage, final ConfiguredFeature<?, ?> feature, final ResourceLocation name)
        {
            this.stage = stage;
            this.feature = feature;
            this.name = name;
        }
    }

    private static class BiomeStructure
    {
        public ConfiguredStructureFeature<?, ?> feature;

        public BiomeStructure(final ConfiguredStructureFeature<?, ?> configured, final JigSawConfig config)
        {
            this.feature = configured;
            if (config.spawn != null) config._matcher = new SpawnBiomeMatcher(config.spawn);
        }
    }

    private class FMLReger
    {
        @SubscribeEvent
        public void processStructures(final RegistryEvent.Register<StructureFeature<?>> event)
        {
            try
            {
                WorldgenHandler.this.loadStructures();
            }
            catch (final Exception e)
            {
                if (e instanceof FileNotFoundException) PokecubeMod.LOGGER.debug("No worldgen database found for "
                        + WorldgenHandler.this.MODID);
                else PokecubeMod.LOGGER.error(e);
                return;
            }

            PokecubeCore.LOGGER.info("Loaded {} pools and {} jigsaws for {}", WorldgenHandler.this.defaults.pools
                    .size(), WorldgenHandler.this.defaults.jigsaws.size(), WorldgenHandler.this.MODID);
            final WorldgenHandler handler = WorldgenHandler.this;

            // Register the pools.
            for (final JigSawPool pool : handler.defaults.pools)
                handler.patterns.put(pool.name, WorldgenFeatures.register(pool, WorldgenFeatures.GENERICLIST));

            // Register the structrues
            for (final JigSawConfig struct : handler.defaults.jigsaws)
                handler.register(struct, event);
        }
    }

    public static WorldgenHandler INSTANCE;

    private final FMLReger reg = new FMLReger();

    private final Map<BiomeFeature, Predicate<BiomeLoadingEvent>>   features   = Maps.newHashMap();
    private final Map<BiomeStructure, Predicate<BiomeLoadingEvent>> structures = Maps.newHashMap();

    public final Map<String, StructureTemplatePool> patterns = Maps.newHashMap();

    public final Map<JigSawConfig, CustomJigsawStructure> toConfigure = Maps.newHashMap();

    private final Map<CustomJigsawStructure, Set<JigSawConfig>> variants = Maps.newHashMap();

    private final Structures defaults = new Structures();

    public WorldgenHandler(final IEventBus bus)
    {
        this(PokecubeCore.MODID, bus);
    }

    private WorldgenHandler(final String modid, final IEventBus bus)
    {
        this.MODID = modid;
        bus.register(this.reg);
        MinecraftForge.EVENT_BUS.register(this);
        WorldgenHandler.INSTANCE = this;
        if (CustomJigsawPiece.TYPE == null) CustomJigsawPiece.TYPE = StructurePoolElementType.register(
                "pokecube:custom_pool_element", CustomJigsawPiece.makeCodec());
    }

    public static void setupAll()
    {
        WorldgenHandler.INSTANCE.setup();
        WorldgenHandler.INSTANCE.registerConfigured();

        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, WorldgenHandler::removeStructures);
    }

    private static void removeStructures(final WorldEvent.Load event)
    {
        if (event.getWorld().isClientSide()) return;

        if (!(event.getWorld() instanceof ServerLevel)) return;

        final ServerLevel serverWorld = (ServerLevel) event.getWorld();

        final Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(serverWorld
                .getChunkSource().generator.getSettings().structureConfig());
        final List<String> removedStructures = PokecubeCore.getConfig().removedStructures;
        for (final StructureFeature<?> s : Sets.newHashSet(tempMap.keySet()))
            if (removedStructures.contains(s.getFeatureName()) || removedStructures.contains(s.getRegistryName()
                    .toString())) tempMap.remove(s);
        serverWorld.getChunkSource().generator.getSettings().structureConfig = tempMap;
    }

    protected void setup()
    {
        // We only want to run setup uniquely per structure itself, so we use
        // this set to ensure that.
        final Set<CustomJigsawStructure> setup = Sets.newHashSet();

        for (final JigSawConfig structure : this.toConfigure.keySet())
            if (setup.add(this.toConfigure.get(structure))) this.setup(this.toConfigure.get(structure), structure);
    }

    protected void setup(final CustomJigsawStructure structure, final JigSawConfig config)
    {
        /*
         * We need to add our structures into the map in Structure alongside
         * vanilla
         * structures or else it will cause errors. Called by registerStructure.
         * If the registration is setup properly for the structure,
         * getRegistryName() should never return null.
         */
        StructureFeature.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);
        StructureFeature.STEP.put(structure, Decoration.SURFACE_STRUCTURES);

        /*
         * Will add land at the base of the structure like it does for Villages
         * and Outposts.
         * Doesn't work well on structure that have pieces stacked vertically or
         * change in heights.
         */
        if (config.base_under) WorldgenHandler.forceVillageFeature(structure);

        /*
         * Adds the structure's spacing into several places so that the
         * structure's spacing remains
         * correct in any dimension or worldtype instead of not spawning.
         * However, it seems it doesn't always work for code made dimensions as
         * they read from
         * this list beforehand. Use the WorldEvent.Load event in
         * StructureTutorialMain to add
         * the structure spacing from this list into that dimension.
         */
        StructureSettings.DEFAULTS = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration> builder().putAll(
                StructureSettings.DEFAULTS).put(structure, config.toSettings()).build();
    }

    /**
     * Will go into the world's chunkgenerator and manually add our structure
     * spacing.
     * If the spacing is not added, the structure doesn't spawn.
     * Use this for dimension blacklists for your structure.
     * (Don't forget to attempt to remove your structure too from
     * the map if you are blacklisting that dimension! It might have
     * your structure in it already.)
     * Basically use this to make absolutely sure the chunkgenerator
     * can or cannot spawn your structure.
     */
    @SubscribeEvent
    public void addDimensionalSpacing(final WorldEvent.Load event)
    {
        if (event.getWorld().isClientSide()) return;

        if (event.getWorld() instanceof ServerLevel)
        {
            if (ThutCore.proxy.getRegistries() == null) throw new IllegalStateException(
                    "Loading world before registries????");

            final ServerLevel serverWorld = (ServerLevel) event.getWorld();
            final ResourceKey<Level> key = serverWorld.dimension();

            // Prevent spawning our structure in Vanilla's superflat world as
            // people seem to want their superflat worlds free of modded
            // structures.
            // Also that vanilla superflat is really tricky and buggy to work
            // with in my experience.
            if (serverWorld.getChunkSource().getGenerator() instanceof FlatLevelSource && key.equals(Level.OVERWORLD))
                return;

            CustomJigsawPiece.sent_events.clear();

            // We only want to run setup uniquely per structure itself, so we
            // use
            // this set to ensure that.
            for (final Entry<CustomJigsawStructure, Set<JigSawConfig>> entry : this.variants.entrySet())
            {
                final Set<JigSawConfig> opts = entry.getValue();
                final CustomJigsawStructure structure = entry.getKey();
                boolean allowed = false;
                for (final JigSawConfig opt : opts)
                    allowed = allowed || !opt.isBlackisted(key);
                // Actually register the structure to the chunk provider,
                // without this it won't generate!
                final Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(serverWorld
                        .getChunkSource().generator.getSettings().structureConfig());
                if (allowed) tempMap.put(structure, StructureSettings.DEFAULTS.get(structure));
                else tempMap.remove(structure);
                serverWorld.getChunkSource().generator.getSettings().structureConfig = tempMap;
            }

            // If we are the first one, we will check for a spawn location, just
            // to initialize things.
            if (this.MODID == PokecubeCore.MODID && PokecubeCore.getConfig().doSpawnBuilding && !PokecubeSerializer
                    .getInstance().hasPlacedSpawn() && key.equals(Level.OVERWORLD) && !WorldgenHandler.SOFTBLACKLIST
                            .contains(Level.OVERWORLD)) serverWorld.getServer().execute(() ->
                            {
                                final ResourceLocation location = new ResourceLocation("pokecube:village");
                                final IForgeRegistry<StructureFeature<?>> reg = ForgeRegistries.STRUCTURE_FEATURES;
                                final StructureFeature<?> structure = reg.getValue(location);
                                if (reg.containsKey(location)) serverWorld.getLevel().findNearestMapFeature(structure,
                                        BlockPos.ZERO, 50, false);
                            });

        }
    }

    private ResourceKey<Biome> from(final BiomeLoadingEvent event)
    {
        return ResourceKey.create(Registry.BIOME_REGISTRY, event.getName());
    }

    protected void registerConfigured()
    {
        for (final JigSawConfig struct : this.toConfigure.keySet())
        {
            final CustomJigsawStructure structure = this.toConfigure.get(struct);
            final JigsawConfig config = new JigsawConfig(struct);
            ConfiguredStructureFeature<?, ?> configured = structure.configured(config);
            configured = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, struct.name,
                    configured);
            final BiomeStructure value = new BiomeStructure(configured, struct);
            // Add the structures to the list, the predicate based on the spawn
            // rules it made.
            this.structures.put(value, c -> struct._matcher == null ? false : struct._matcher.checkLoadEvent(c));
        }
        for (final BiomeFeature feature : this.features.keySet())
        {
            final Registry<ConfiguredFeature<?, ?>> registry = BuiltinRegistries.CONFIGURED_FEATURE;
            Registry.register(registry, feature.name, feature.feature);
        }
    }

    public void register(final Predicate<ResourceKey<Biome>> selector, final GenerationStep.Decoration stage,
            final ConfiguredFeature<?, ?> feature, final ResourceLocation name)
    {
        final BiomeFeature toAdd = new BiomeFeature(stage, feature, name);
        this.features.put(toAdd, c -> selector.test(this.from(c)));
    }

    public void loadStructures() throws Exception
    {
        final Collection<ResourceLocation> resources = PackFinder.getJsonResources("structures/");

        this.defaults.jigsaws.clear();
        this.defaults.pools.clear();

        PokecubeCore.LOGGER.info("Found Worldgen Databases: {}", resources);

        for (final ResourceLocation file : resources)
            try
            {
                final InputStream res = PackFinder.getStream(file);
                final Reader reader = new InputStreamReader(res);
                final Structures extra = PokedexEntryLoader.gson.fromJson(reader, Structures.class);

                PokecubeCore.LOGGER.info("Found {} jigsaws and {} pools in {}", extra.jigsaws.size(), extra.pools
                        .size(), file);
                this.defaults.jigsaws.addAll(extra.jigsaws);
                this.defaults.pools.addAll(extra.pools);
            }
            catch (JsonSyntaxException | JsonIOException | IOException e)
            {
                PokecubeCore.LOGGER.error("Error with pools for {}", file, e);
            }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void regBiomes(final BiomeLoadingEvent event)
    {
        if (event.getName() == null)
        {
            PokecubeCore.LOGGER.error("Null biome name loading, cannot determine what should go here!");
            return;
        }
        for (final BiomeFeature feat : this.features.keySet())
            if (this.features.get(feat).test(event)) event.getGeneration().addFeature(feat.stage, feat.feature);
        for (final BiomeStructure feat : this.structures.keySet())
            if (this.structures.get(feat).test(event))
            {
                final JigsawConfig conf = (JigsawConfig) feat.feature.config;
                PokecubeCore.LOGGER.debug("Adding Structure {} to biome {}", conf.struct_config.name, event.getName());
                event.getGeneration().getStructures().add(() -> feat.feature);
            }
    }

    public CustomJigsawStructure register(final JigSawConfig struct,
            final RegistryEvent.Register<StructureFeature<?>> event)
    {
        final String structName = struct.type.isEmpty() ? struct.name : struct.type;

        CustomJigsawStructure structure = WorldgenHandler.structs.get(structName);
        // already registered! (Need to do something about this?
        if (structure == null)
        {
            PokecubeCore.LOGGER.info("Registering Structure: {} for mod {}", structName, this.MODID);
            structure = new CustomJigsawStructure(JigsawConfig.CODEC);
            structure.setRegistryName(new ResourceLocation(structName));
            structure.priority = struct.priority;
            structure.spacing = struct.spacing;
            WorldgenHandler.structs.put(structName, structure);
            // Use this instead of event, as it will also populated proper maps.
            event.getRegistry().register(structure);

            for (final String s : PokecubeCore.getConfig().worldgenWorldSettings)
            {
                final ResourceKey<NoiseGeneratorSettings> key = ResourceKey.create(
                        Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation(s));
                BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(key).structureSettings().structureConfig().put(structure,
                        struct.toSettings());
            }
        }
        PokecubeCore.LOGGER.info("Requesting pool of: {}", struct.root);
        if (!this.patterns.containsKey(struct.root))
        {
            PokecubeCore.LOGGER.error("No pool found for {}, are you sure it is registered?", struct.root);
            return structure;
        }
        PokecubeTerrainChecker.manualStructureSubbiomes.put(struct.name, struct.biomeType);
        // Add it to our list for configuration
        this.toConfigure.put(struct, structure);
        Set<JigSawConfig> types = this.variants.get(structure);
        if (types == null) this.variants.put(structure, types = Sets.newHashSet());
        types.add(struct);
        return structure;
    }

    private static void forceVillageFeature(final StructureFeature<?> feature)
    {
        final List<StructureFeature<?>> list = Lists.newArrayList(StructureFeature.NOISE_AFFECTING_FEATURES);
        if (!list.contains(feature)) list.add(feature);
        StructureFeature.NOISE_AFFECTING_FEATURES = list;
    }
}
