package pokecube.core.database.worldgen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern.PlacementBehaviour;
import net.minecraft.world.gen.feature.jigsaw.JigsawPatternRegistry;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.ProcessorLists;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.gen.jigsaw.CustomJigsawStructure;

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

        // CustomJigsawPiece.TYPE =
        // IJigsawDeserializer.func_236851_a_("pokecube:custom_pool_element",
        // CustomJigsawPiece.CODEC);
        // CustomVillagePiece.PCVP =
        // IStructurePieceType.register(CustomVillagePiece::new,
        // "pokecube:pcvp");

    }

    public static class JsonStructure
    {
        public String    name;
        /**
         * In MultiStructures, this is the chance that the part will be picked.
         * Parts are sorted by priority, then the first to have a successful
         * pick is what is generated for that position.
         */
        public float     chance    = 1;
        public int       offset    = 1;
        public String    biomeType = "none";
        public SpawnRule spawn;
        public boolean   surface   = true;
        public boolean   water     = false;
        public boolean   whitelist = false;
        public int[]     list      = {};

        public String serialize()
        {
            return WorldgenHandler.GSON.toJson(this);
        }

        public static JsonStructure deserialize(final String structstring)
        {
            return WorldgenHandler.GSON.fromJson(structstring, JsonStructure.class);
        }

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
        public float        chance       = 1;
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
        public boolean      atSpawn      = false;
        public List<String> needed_once  = Lists.newArrayList();
        public List<String> dimBlacklist = Lists.newArrayList();

        public SpawnBiomeMatcher _matcher;

        public String serialize()
        {
            return WorldgenHandler.GSON.toJson(this);
        }

        public static JigSawConfig deserialize(final String structstring)
        {
            return WorldgenHandler.GSON.fromJson(structstring, JigSawConfig.class);
        }

        public boolean isBlackisted(final DimensionType dim)
        {
            // for (final String s : this.dimBlacklist)
            // {
            // if (!WorldgenHandler.dimTypes.containsKey(s))
            // {
            // final DimensionType type = DimensionType.byName(new
            // ResourceLocation(s));
            // WorldgenHandler.dimTypes.put(s, type);
            // }
            // final DimensionType type = WorldgenHandler.dimTypes.get(s);
            // if (type == dim) return true;
            // }
            return false;
        }
    }

    public static class Structures
    {
        public List<JigSawPool>   pools   = Lists.newArrayList();
        public List<JigSawConfig> jigsaws = Lists.newArrayList();
    }

    public static Map<String, WorldgenHandler> WORLDGEN = Maps.newConcurrentMap();

    public static Map<String, CustomJigsawStructure> structs = Maps.newConcurrentMap();

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

        public BiomeStructure(final StructureFeature<?, ?> configured)
        {
            this.feature = configured;
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
                if (e instanceof FileNotFoundException) PokecubeMod.LOGGER.warn("No worldgen database found for "
                        + WorldgenHandler.this.MODID);
                else PokecubeMod.LOGGER.catching(e);
                return;
            }

            PokecubeCore.LOGGER.info("Loaded {} pools and {} jigsaws for {}", WorldgenHandler.this.defaults.pools
                    .size(), WorldgenHandler.this.defaults.jigsaws.size(), WorldgenHandler.this.MODID);

            // Register the pools.
            for (final JigSawPool pool : WorldgenHandler.this.defaults.pools)
            {
                final JigsawPattern.PlacementBehaviour placement = pool.rigid ? JigsawPattern.PlacementBehaviour.RIGID
                        : JigsawPattern.PlacementBehaviour.TERRAIN_MATCHING;
                final List<Pair<Function<PlacementBehaviour, ? extends JigsawPiece>, Integer>> pairs = Lists
                        .newArrayList();
                for (final String option : pool.options)
                {
                    int second = 1;
                    final String[] args = option.split(";");
                    if (args.length > 1)
                    {
                        final Options opts = Options.deserialize(args[1]);
                        second = opts.weight;
                    }
                    final Pair<Function<PlacementBehaviour, ? extends JigsawPiece>, Integer> pair = Pair.of(JigsawPiece
                            .func_242851_a(args[0], ProcessorLists.field_244110_j), second);
                    pairs.add(pair);
                }
                JigsawPattern pattern = new JigsawPattern(new ResourceLocation(pool.name), new ResourceLocation(
                        pool.target), pairs, placement);
                pattern = JigsawPatternRegistry.func_244094_a(pattern);
                WorldgenHandler.this.patterns.put(pool.name, pattern);
                PokecubeCore.LOGGER.info("Registered Pattern/Pool: " + pool.name);
            }

            // Register the structrues
            for (final JigSawConfig struct : WorldgenHandler.this.defaults.jigsaws)
                WorldgenHandler.this.register(struct, event);
        }
    }

    private final FMLReger reg = new FMLReger();

    private final Map<Predicate<RegistryKey<Biome>>, BiomeFeature>   features   = Maps.newHashMap();
    private final Map<Predicate<RegistryKey<Biome>>, BiomeStructure> structures = Maps.newHashMap();

    private final Map<String, JigsawPattern> patterns = Maps.newHashMap();

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

    public void register(final Predicate<RegistryKey<Biome>> selector, final GenerationStage.Decoration stage,
            final ConfiguredFeature<?, ?> feature)
    {
        final BiomeFeature toAdd = new BiomeFeature(stage, feature);
        this.features.put(selector, toAdd);
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
        for (final Predicate<RegistryKey<Biome>> checks : this.features.keySet())
            if (checks.test(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, event.getName())))
            {
                final BiomeFeature feat = this.features.get(checks);
                event.getGeneration().getFeatures(feat.stage).add(() -> feat.feature);
            }
        for (final Predicate<RegistryKey<Biome>> checks : this.structures.keySet())
            if (checks.test(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, event.getName())))
            {
                final BiomeStructure feat = this.structures.get(checks);
                event.getGeneration().getStructures().add(() -> feat.feature);
            }
    }

    public void register(final JigSawConfig struct, final RegistryEvent.Register<Structure<?>> event)
    {
        if (!struct.name.equals("pokecube:scattered/ruins")) return;
        final String structName = struct.type.isEmpty() ? struct.name : struct.type;

        CustomJigsawStructure structure = WorldgenHandler.structs.get(structName);
        // already registered! (Need to do something about this?
        if (structure == null)
        {
            PokecubeCore.LOGGER.info("Registering Structure: {} ({}) for mod {}", structName, struct.name, this.MODID);
            structure = new CustomJigsawStructure(VillageConfig.field_236533_a_);
            WorldgenHandler.structs.put(structName, structure);
            structure.setRegistryName(new ResourceLocation(structName));
            // Use this instead of event, as it will also populated proper maps.
            event.getRegistry().register(structure);
            Structure.STRUCTURE_DECORATION_STAGE_MAP.put(structure, Decoration.SURFACE_STRUCTURES);
            Structure.NAME_STRUCTURE_BIMAP.put(structName, structure);
            WorldgenHandler.forceVillageFeature(structure);
        }
        PokecubeCore.LOGGER.info("Requesting pool of: {}", struct.root);
        if (!this.patterns.containsKey(struct.root)) throw new RuntimeException();
        final VillageConfig config = new VillageConfig(() -> this.patterns.get(struct.root), 2);
        StructureFeature<?, ?> configured = structure.withConfiguration(config);
        configured = WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE, struct.name,
                configured);
        final BiomeStructure value = new BiomeStructure(configured);
        this.structures.put((c) -> true, value);
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
