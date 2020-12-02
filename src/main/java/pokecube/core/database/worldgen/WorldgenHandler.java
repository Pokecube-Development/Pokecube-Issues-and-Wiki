package pokecube.core.database.worldgen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.gen.WorldgenFeatures;
import pokecube.core.world.gen.jigsaw.CustomJigsawPiece;
import pokecube.core.world.gen.jigsaw.CustomJigsawStructure;
import pokecube.core.world.gen.jigsaw.JigsawConfig;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

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

        CustomJigsawPiece.TYPE = IJigsawDeserializer.func_236851_a_("pokecube:custom_pool_element",
                CustomJigsawPiece.CODEC);

    }

    public static class Options
    {
        public static final Codec<Options> CODEC = Codec.STRING.comapFlatMap(Options::decodeOptions,
                Options::serialize);

        private static DataResult<Options> decodeOptions(final String encoded)
        {
            return DataResult.success(Options.deserialize(encoded));
        }

        public int    weight = 1;
        public String flag   = "";
        public int    dy     = 0;

        public boolean rigid     = true;
        public boolean ignoreAir = true;
        public boolean filler    = false;

        public boolean override = false;

        public Map<String, JsonElement> extra = Maps.newHashMap();

        public String serialize()
        {
            return WorldgenHandler.GSON.toJson(this);
        }

        public static Options deserialize(final String structstring)
        {
            return WorldgenHandler.GSON.fromJson(structstring, Options.class);
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
        public int          height       = 0;
        public int          variance     = 50;
        public int          priority     = 100;
        public int          seed         = -1;
        public List<String> needed_once  = Lists.newArrayList();
        public List<String> dimBlacklist = Lists.newArrayList();
        public List<String> dimWhitelist = Lists.newArrayList();

        private final List<RegistryKey<World>> _blacklisted = Lists.newArrayList();
        private final List<RegistryKey<World>> _whitelisted = Lists.newArrayList();

        public SpawnBiomeMatcher _matcher;

        public String serialize()
        {
            return WorldgenHandler.GSON.toJson(this);
        }

        public StructureSeparationSettings toSettings()
        {
            // Make seed based on name
            if (this.seed == -1)
            {
                this.seed = this.name.hashCode();
                // Seed must be positive apparently.
                if (this.seed < 0) this.seed *= -1;
                final Random rand = new Random(this.seed);
                // Ensure the seed is "large"
                while (this.seed < 1e6)
                    this.seed = rand.nextInt();
            }
            return new StructureSeparationSettings(this.distance, this.separation, this.seed);
        }

        public static JigSawConfig deserialize(final String structstring)
        {
            return WorldgenHandler.GSON.fromJson(structstring, JigSawConfig.class);
        }

        public boolean isBlackisted(final RegistryKey<World> dim)
        {
            if (this._blacklisted.size() != this.dimBlacklist.size() || this._whitelisted.size() != this.dimWhitelist
                    .size())
            {
                this._blacklisted.clear();
                this._whitelisted.clear();
                for (final String s : this.dimBlacklist)
                    this._blacklisted.add(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(s)));
                for (final String s : this.dimWhitelist)
                    this._whitelisted.add(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(s)));
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

    private static Map<String, WorldgenHandler> WORLDGEN = Maps.newConcurrentMap();

    public static Map<String, CustomJigsawStructure> structs = Maps.newConcurrentMap();

    public static Set<RegistryKey<World>> SOFTBLACKLIST = Sets.newHashSet();

    public static WorldgenHandler get(final String modid)
    {
        return WorldgenHandler.WORLDGEN.get(modid);
    }

    public final String           MODID;
    public final ResourceLocation ROOT;

    private static class BiomeFeature
    {
        public GenerationStage.Decoration stage;

        public ConfiguredFeature<?, ?> feature;

        public BiomeFeature(final Decoration stage, final ConfiguredFeature<?, ?> feature)
        {
            this.stage = stage;
            this.feature = feature;
        }
    }

    private static class BiomeStructure
    {
        public StructureFeature<?, ?> feature;

        public BiomeStructure(final StructureFeature<?, ?> configured, final JigSawConfig config)
        {
            this.feature = configured;
            if (config.spawn != null) config._matcher = new SpawnBiomeMatcher(config.spawn);
        }
    }

    private class FMLReger
    {
        @SubscribeEvent
        public void processStructures(final RegistryEvent.Register<Structure<?>> event)
        {
            try
            {
                WorldgenHandler.this.loadStructures();
            }
            catch (final Exception e)
            {
                if (e instanceof FileNotFoundException) PokecubeMod.LOGGER.debug("No worldgen database found for "
                        + WorldgenHandler.this.MODID);
                else PokecubeMod.LOGGER.catching(e);
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

    private final FMLReger reg = new FMLReger();

    private final Map<BiomeFeature, Predicate<RegistryKey<Biome>>>   features   = Maps.newHashMap();
    private final Map<BiomeStructure, Predicate<RegistryKey<Biome>>> structures = Maps.newHashMap();

    public final Map<String, JigsawPattern> patterns = Maps.newHashMap();

    public final Map<JigSawConfig, CustomJigsawStructure> toConfigure = Maps.newHashMap();

    public Structures defaults;

    public WorldgenHandler(final IEventBus bus)
    {
        this(PokecubeCore.MODID, bus);
    }

    public WorldgenHandler(final String modid, final IEventBus bus)
    {
        this.MODID = modid;
        this.ROOT = new ResourceLocation(this.MODID, "structures/");
        bus.register(this.reg);
        MinecraftForge.EVENT_BUS.register(this);
        WorldgenHandler.WORLDGEN.put(this.MODID, this);
    }

    public static void setupAll()
    {
        for (final WorldgenHandler gen : WorldgenHandler.WORLDGEN.values())
        {
            gen.setup();
            gen.registerConfigured();
        }
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
        Structure.NAME_STRUCTURE_BIMAP.put(structure.getRegistryName().toString(), structure);
        Structure.STRUCTURE_DECORATION_STAGE_MAP.put(structure, Decoration.SURFACE_STRUCTURES);

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
        DimensionStructuresSettings.field_236191_b_ = ImmutableMap.<Structure<?>, StructureSeparationSettings> builder()
                .putAll(DimensionStructuresSettings.field_236191_b_).put(structure, config.toSettings()).build();
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
        if (event.getWorld() instanceof ServerWorld)
        {
            final ServerWorld serverWorld = (ServerWorld) event.getWorld();
            final RegistryKey<World> key = serverWorld.getDimensionKey();

            // Prevent spawning our structure in Vanilla's superflat world as
            // people seem to want their superflat worlds free of modded
            // structures.
            // Also that vanilla superflat is really tricky and buggy to work
            // with in my experience.
            if (serverWorld.getChunkProvider().getChunkGenerator() instanceof FlatChunkGenerator && key.equals(
                    World.OVERWORLD)) return;

            CustomJigsawPiece.sent_events.clear();

            // We only want to run setup uniquely per structure itself, so we
            // use
            // this set to ensure that.
            final Set<CustomJigsawStructure> setup = Sets.newHashSet();
            for (final JigSawConfig struct : this.toConfigure.keySet())
            {
                final CustomJigsawStructure structure = this.toConfigure.get(struct);
                // Ensures we only do this once!
                if (!setup.add(structure)) continue;

                // Check the blacklist for here.
                if (struct.isBlackisted(key)) continue;

                // Actually register the structure to the chunk provider,
                // without this it won't generate!
                final Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(serverWorld
                        .getChunkProvider().generator.func_235957_b_().func_236195_a_());
                tempMap.put(structure, DimensionStructuresSettings.field_236191_b_.get(structure));
                serverWorld.getChunkProvider().generator.func_235957_b_().field_236193_d_ = tempMap;
            }

            // If we are the first one, we will check for a spawn location, just
            // to initialize things.
            if (this.MODID == PokecubeCore.MODID && !PokecubeSerializer.getInstance().hasPlacedSpawn() && key.equals(
                    World.OVERWORLD)) serverWorld.getServer().execute(() ->
                    {
                        final ResourceLocation location = new ResourceLocation("pokecube:village");
                        final IForgeRegistry<Structure<?>> reg = ForgeRegistries.STRUCTURE_FEATURES;
                        final Structure<?> structure = reg.getValue(location);
                        if (reg.containsKey(location)) serverWorld.getWorld().func_241117_a_(structure, BlockPos.ZERO,
                                50, false);
                    });

        }
    }

    protected void registerConfigured()
    {
        for (final JigSawConfig struct : this.toConfigure.keySet())
        {
            final CustomJigsawStructure structure = this.toConfigure.get(struct);
            final JigsawConfig config = new JigsawConfig(struct);
            StructureFeature<?, ?> configured = structure.withConfiguration(config);
            configured = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE, struct.name,
                    configured);
            final BiomeStructure value = new BiomeStructure(configured, struct);
            // Add the structures to the list, the predicate based on the spawn
            // rules it made.
            this.structures.put(value, c -> struct._matcher == null ? false : struct._matcher.checkBiome(c));
        }
    }

    public void register(final Predicate<RegistryKey<Biome>> selector, final GenerationStage.Decoration stage,
            final ConfiguredFeature<?, ?> feature)
    {
        final BiomeFeature toAdd = new BiomeFeature(stage, feature);
        this.features.put(toAdd, selector);
    }

    public void loadStructures() throws Exception
    {
        final ResourceLocation json = new ResourceLocation(this.ROOT.toString() + "worldgen.json");
        final InputStream res = Database.resourceManager.getResource(json).getInputStream();
        final Reader reader = new InputStreamReader(res);
        this.defaults = PokedexEntryLoader.gson.fromJson(reader, Structures.class);
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
            if (this.features.get(feat).test(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, event.getName()))) event
                    .getGeneration().getFeatures(feat.stage).add(() -> feat.feature);
        for (final BiomeStructure feat : this.structures.keySet())
            if (this.structures.get(feat).test(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, event.getName())))
            {
                final JigsawConfig conf = (JigsawConfig) feat.feature.field_236269_c_;
                PokecubeCore.LOGGER.info("Adding Structure {} to biome {}", conf.struct_config.name, event.getName());
                event.getGeneration().getStructures().add(() -> feat.feature);
            }
    }

    public CustomJigsawStructure register(final JigSawConfig struct, final RegistryEvent.Register<Structure<?>> event)
    {
        final String structName = struct.type.isEmpty() ? struct.name : struct.type;

        CustomJigsawStructure structure = WorldgenHandler.structs.get(structName);
        // already registered! (Need to do something about this?
        if (structure == null)
        {
            PokecubeCore.LOGGER.info("Registering Structure: {} for mod {}", structName, this.MODID);
            structure = new CustomJigsawStructure(JigsawConfig.CODEC);
            structure.setRegistryName(new ResourceLocation(structName));
            WorldgenHandler.structs.put(structName, structure);
            // Use this instead of event, as it will also populated proper maps.
            event.getRegistry().register(structure);
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
        return structure;
    }

    private static Field illagers = null;

    private static void forceVillageFeature(final Structure<?> feature)
    {
        if (WorldgenHandler.illagers == null) WorldgenHandler.illagers = ObfuscationReflectionHelper.findField(
                Structure.class, "field_236384_t_");
        final List<Structure<?>> list = Lists.newArrayList(Structure.field_236384_t_);
        list.add(feature);
        try
        {
            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(WorldgenHandler.illagers, WorldgenHandler.illagers.getModifiers() & ~Modifier.FINAL);
            WorldgenHandler.illagers.set(null, list);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
}
