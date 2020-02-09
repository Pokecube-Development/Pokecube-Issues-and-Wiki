package pokecube.core.database.worldgen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Locale;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.gen.feature.scattered.jigsaw.JigsawConfig;
import pokecube.core.world.gen.feature.scattered.jigsaw.JigsawPieces;
import pokecube.core.world.gen.feature.scattered.jigsaw.JigsawStructure;
import pokecube.core.world.gen.feature.scattered.testa.ConfigStructure;

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
        /** In MultiStructures, this is the chance that the part will be picked.
         * Parts are sorted by priority, then the first to have a successful
         * pick is what is generated for that position. */
        public float     chance    = 1;
        public int       offset    = 0;
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

    public static class JigSawConfig
    {
        public static class JigSawPart
        {
            public String       name;
            public String       target    = "empty";
            public List<String> options;
            public boolean      rigid     = true;
            public boolean      ignoreAir = true;
        }

        public String           name;
        public JigSawPart       root;
        public float            chance     = 1;
        public int              offset     = 0;
        public int              size       = 4;
        public int              distance   = 8;
        public int              separation = 4;
        public String           biomeType  = "ruin";
        public SpawnRule        spawn;
        public boolean          surface    = true;
        public boolean          water      = false;
        public boolean          atSpawn    = false;
        public List<JigSawPart> parts      = Lists.newArrayList();

        public String serialize()
        {
            return WorldgenHandler.GSON.toJson(this);
        }

        public static JigSawConfig deserialize(final String structstring)
        {
            return WorldgenHandler.GSON.fromJson(structstring, JigSawConfig.class);
        }
    }

    public static class Structures
    {
        public List<JsonStructure> structures = Lists.newArrayList();
        public List<JigSawConfig>  jigsaws    = Lists.newArrayList();
    }

    public File             DEFAULT;

    public CustomDims       dims;

    public String           MODID    = PokecubeCore.MODID;
    public ResourceLocation ROOT     = new ResourceLocation(PokecubeCore.MODID, "structures/");
    public Structures       defaults = new Structures();

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
        final Structures database = PokedexEntryLoader.gson.fromJson(reader, Structures.class);
        this.defaults.structures.addAll(database.structures);
        this.defaults.jigsaws.addAll(database.jigsaws);
    }

    public void processStructures(final RegistryEvent.Register<Feature<?>> event)
    {
        try
        {
            this.loadStructures();
        }
        catch (final Exception e)
        {
            PokecubeMod.LOGGER.catching(e);
        }

        for (final JsonStructure struct : this.defaults.structures)
        {
            final String structname = this.ROOT.toString() + struct.name.replaceAll("/", "_").toLowerCase(Locale.ROOT);
            final ResourceLocation regname = new ResourceLocation(structname);
            final ConfigStructure toAdd = new ConfigStructure(regname);
            toAdd.structLoc = new ResourceLocation(this.MODID, struct.name);
            toAdd.struct = struct;

            event.getRegistry().register(toAdd);
            final SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(struct.spawn);

            final GenerationStage.Decoration stage = struct.surface ? GenerationStage.Decoration.SURFACE_STRUCTURES
                    : GenerationStage.Decoration.UNDERGROUND_STRUCTURES;
            for (final Biome b : ForgeRegistries.BIOMES.getValues())
            {
                if (!matcher.checkBiome(b)) continue;
                b.addFeature(stage, toAdd.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
                b.addStructure(toAdd.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
            }
        }
        for (final JigSawConfig struct : this.defaults.jigsaws)
        {
            JigsawPieces.registerJigsaw(struct);
            final JigsawStructure toAdd = new JigsawStructure(struct);
            toAdd.setRegistryName(new ResourceLocation(struct.name));
            event.getRegistry().register(toAdd);
            final SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(struct.spawn);
            final JigsawConfig config = new JigsawConfig(struct);
            final GenerationStage.Decoration stage = struct.surface ? GenerationStage.Decoration.SURFACE_STRUCTURES
                    : GenerationStage.Decoration.UNDERGROUND_STRUCTURES;
            for (final Biome b : ForgeRegistries.BIOMES.getValues())
            {
                if (!matcher.checkBiome(b)) continue;
                b.addFeature(stage, toAdd.withConfiguration(config));
                b.addStructure(toAdd.withConfiguration(config));
            }

        }
        PokecubeMod.LOGGER.debug("Loaded configurable worldgen");
    }
}
