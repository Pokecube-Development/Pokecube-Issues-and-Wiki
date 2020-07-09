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
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.gen.WorldStructDatafixer;
import pokecube.core.world.gen.jigsaw.JigsawConfig;
import pokecube.core.world.gen.jigsaw.JigsawPieces;
import pokecube.core.world.gen.jigsaw.JigsawStructure;

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
        PokecubeCore.LOGGER.info("Registering DataFixer for structures");
        WorldStructDatafixer.insertFixer();
    }

    public static class CustomDim
    {
        public int    dimid;
        public String world_name;
        public String dim_type;
        public String world_type;
        public String generator_options;
        public Long   seed;

        @Override
        public String toString()
        {
            return this.dimid + " " + this.world_name + " " + this.dim_type + " " + this.world_type + " "
                    + this.generator_options + " " + this.seed;
        }
    }

    public static class CustomDims
    {
        public List<CustomDim> dims = Lists.newArrayList();
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
            for (final String s : this.dimBlacklist)
            {
                if (!WorldgenHandler.dimTypes.containsKey(s))
                {
                    final DimensionType type = DimensionType.byName(new ResourceLocation(s));
                    WorldgenHandler.dimTypes.put(s, type);
                }
                final DimensionType type = WorldgenHandler.dimTypes.get(s);
                if (type == dim) return true;
            }
            return false;
        }
    }

    public static class Structures
    {
        public List<JigSawPool>   pools   = Lists.newArrayList();
        public List<JigSawConfig> jigsaws = Lists.newArrayList();
    }

    public static Map<String, JigsawStructure> structs = Maps.newConcurrentMap();

    private static Map<String, DimensionType> dimTypes = Maps.newConcurrentMap();

    private static Map<JigsawStructure, Integer> priorities = Maps.newConcurrentMap();

    public static boolean isBlocked(final ChunkGenerator<?> gen, final Random rand, final Structure<?> feat_in,
            final int chunkX, final int chunkZ)
    {
        final Integer id = WorldgenHandler.priorities.getOrDefault(feat_in, 100);
        final ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        for (final Entry<JigsawStructure, Integer> entry : WorldgenHandler.priorities.entrySet())
        {
            final JigsawStructure feat = entry.getKey();
            if (feat == feat_in) continue;
            final Integer id2 = entry.getValue();
            if (id2 >= id) continue;
            if (feat.getStartPositionForPosition(gen, rand, chunkX, chunkZ, 0, 0).equals(pos)) return true;
        }
        return false;
    }

    public CustomDims dims;

    public String           MODID = PokecubeCore.MODID;
    public ResourceLocation ROOT  = new ResourceLocation(PokecubeCore.MODID, "structures/");
    public Structures       defaults;

    public WorldgenHandler()
    {
    }

    public WorldgenHandler(final String modid)
    {
        this.MODID = modid;
        this.ROOT = new ResourceLocation(this.MODID, "structures/");
    }

    public void loadStructures() throws Exception
    {
        final ResourceLocation json = new ResourceLocation(this.ROOT.toString() + "worldgen.json");
        final InputStream res = Database.resourceManager.getResource(json).getInputStream();
        final Reader reader = new InputStreamReader(res);
        this.defaults = PokedexEntryLoader.gson.fromJson(reader, Structures.class);
    }

    public void processStructures(final RegistryEvent.Register<Feature<?>> event)
    {
        try
        {
            this.loadStructures();
        }
        catch (final Exception e)
        {
            if (e instanceof FileNotFoundException) PokecubeMod.LOGGER.warn("No worldgen database found for "
                    + this.MODID);
            else PokecubeMod.LOGGER.catching(e);
            return;
        }
        // Initialize the pools
        for (final JigSawPool pool : this.defaults.pools)
            JigsawPieces.initPool(pool);

        // Register the jigsaws
        for (final JigSawConfig struct : this.defaults.jigsaws)
            WorldgenHandler.register(struct, event);
        PokecubeMod.LOGGER.debug("Loaded configurable worldgen");
    }

    public static void register(final JigSawConfig struct, final RegistryEvent.Register<Feature<?>> event)
    {
        JigsawPieces.registerJigsaw(struct);
        final String key = struct.type.isEmpty() ? struct.name : struct.type;
        final JigsawStructure toAdd = WorldgenHandler.structs.getOrDefault(key, new JigsawStructure(key)).addStruct(
                struct);
        if (!WorldgenHandler.structs.containsKey(key))
        {
            WorldgenHandler.structs.put(key, toAdd);
            toAdd.setRegistryName(new ResourceLocation(struct.name));
            event.getRegistry().register(toAdd);
            WorldgenHandler.priorities.put(toAdd, struct.priority);
        }
        // No natural spawn, we skip this one for spawning.
        if (struct.spawn == null) return;

        struct._matcher = new SpawnBiomeMatcher(struct.spawn);
        final JigsawConfig config = new JigsawConfig(struct);
        final GenerationStage.Decoration stage = struct.surface ? GenerationStage.Decoration.SURFACE_STRUCTURES
                : GenerationStage.Decoration.UNDERGROUND_STRUCTURES;
        if (struct.base_under && !struct.water && !struct.air) WorldgenHandler.forceVillageFeature(toAdd);
        for (final Biome b : ForgeRegistries.BIOMES.getValues())
        {
            b.addFeature(stage, toAdd.withConfiguration(config));
            b.addStructure(toAdd.withConfiguration(config));
        }
    }

    private static Field illagers = null;

    private static void forceVillageFeature(final Structure<?> feature)
    {
        if (WorldgenHandler.illagers == null) WorldgenHandler.illagers = ObfuscationReflectionHelper.findField(
                Feature.class, "field_214488_aQ");
        final List<Structure<?>> list = Lists.newArrayList(Feature.ILLAGER_STRUCTURES);
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
