package pokecube.core.database.worldgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;

public class WorldgenHandler
{
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

    public static class MultiStructure
    {
        public String          name;
        float                  chance;
        boolean                syncGround = false;
        public SpawnRule       spawn;
        public List<Structure> structures = Lists.newArrayList();
    }

    public static class Structure
    {
        public String    name;
        /**
         * In MultiStructures, this is the chance that the part will be picked.
         * Parts are sorted by priority, then the first to have a successful
         * pick is what is generated for that position.
         */
        float            chance   = 1;
        int              offset;
        public String    biomeType;
        public SpawnRule spawn;
        /**
         * In MultiStructures, this is the relative position of the part. Only
         * one part for each unique positon can be picked, the actual distance
         * the structure spawns, is this scaled by the size of the intermediate
         * parts.
         */
        public String    position;
        public String    rotation;
        public String    mirror;
        /** lower numbers get put higher up the "pick list" */
        public int       priority = 100;
    }

    public static class Structures
    {
        public List<Structure>      structures      = Lists.newArrayList();
        public List<MultiStructure> multiStructures = Lists.newArrayList();
    }

    public static File DEFAULT;

    public static CustomDims dims;

    public static Structures defaults = new Structures();

    static void init()
    {
        WorldgenHandler.defaults.structures.clear();
        final Structure ruin_1 = new Structure();
        ruin_1.name = "ruin_1";
        ruin_1.chance = 0.002f;
        ruin_1.offset = -3;
        ruin_1.biomeType = "ruin";
        final SpawnRule rule = new SpawnRule();
        rule.values.put(SpawnBiomeMatcher.TYPES, "plains");
        ruin_1.spawn = rule;
        WorldgenHandler.defaults.structures.add(ruin_1);
    }

    public static void init(File file)
    {
        WorldgenHandler.init();
        if (!file.exists())
        {
            final Gson gson = new GsonBuilder().registerTypeAdapter(QName.class, new TypeAdapter<QName>()
            {
                @Override
                public QName read(JsonReader in) throws IOException
                {
                    return new QName(in.nextString());
                }

                @Override
                public void write(JsonWriter out, QName value) throws IOException
                {
                    out.value(value.toString());
                }
            }).setPrettyPrinting().create();
            final String json = gson.toJson(WorldgenHandler.defaults, Structures.class);
            try
            {
                final FileWriter writer = new FileWriter(file);
                writer.append(json);
                writer.close();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            WorldgenHandler.defaults.structures.clear();
            WorldgenHandler.defaults.multiStructures.clear();
            FileInputStream stream;
            try
            {
                stream = new FileInputStream(file);
                try
                {
                    WorldgenHandler.loadStructures(stream, true);
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
                stream.close();
            }
            catch (final FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void loadCustomDims(String dimFile)
    {
        // File file = new File(PokecubeTemplates.TEMPLATES, dimFile);
        // if (!file.exists())
        // {
        // PokecubeCore.LOGGER.info("No Custom Dimensions file found: " + file
        // + " If you make one, it will allow specifying custom dimensions and
        // worldgen.");
        // return;
        // }
        //
        // try
        // {
        // FileInputStream stream = new FileInputStream(file);
        // InputStreamReader reader = new InputStreamReader(stream);
        // dims = PokedexEntryLoader.gson.fromJson(reader, CustomDims.class);
        // if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Loaded Dims: " +
        // dims.dims);
        // }
        // catch (Exception e)
        // {
        // PokecubeCore.LOGGER.error("Error loading custom Dims from: " + file,
        // e);
        // }

    }

    public static void loadStructures(InputStream stream, boolean json) throws Exception
    {
        Structures database;
        final InputStreamReader reader = new InputStreamReader(stream);
        if (json) database = PokedexEntryLoader.gson.fromJson(reader, Structures.class);
        else throw new IllegalArgumentException("The database for structures Must be a json.");
        WorldgenHandler.defaults.structures.addAll(database.structures);
        WorldgenHandler.defaults.multiStructures.addAll(database.multiStructures);
    }

    public static void loadStructures(String configFile)
    {
        // boolean json = configFile.endsWith("json");
        // File file = new File(PokecubeTemplates.TEMPLATES, configFile);
        // try
        // {
        // FileInputStream stream = new FileInputStream(file);
        // try
        // {
        // loadStructures(stream, json);
        // }
        // catch (Exception e)
        // {
        // e.printStackTrace();
        // }
        // stream.close();
        // }
        // catch (Exception e)
        // {
        // e.printStackTrace();
        // }

    }

    public static void processStructures()
    {
        // for (Structure struct : defaults.structures)
        // {
        // try
        // {
        // WorldGenTemplates.TemplateGen template = new TemplateGen(struct.name,
        // new SpawnBiomeMatcher(struct.spawn), struct.chance, struct.offset);
        // WorldGenTemplates.templates.add(template);
        // WorldGenTemplates.namedTemplates.put(struct.name, new
        // TemplateGen(struct.name,
        // new SpawnBiomeMatcher(struct.spawn), struct.chance, struct.offset));
        // if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Loaded Structure: "
        // + struct.name + " " + struct.spawn + " "
        // + struct.chance + " " + struct.offset);
        // }
        // catch (Exception e)
        // {
        // PokecubeCore.LOGGER.error(
        // (struct.name + " " + struct.spawn + " " + struct.chance + " " +
        // struct.offset), e);
        // }
        // }
        // for (MultiStructure struct : defaults.multiStructures)
        // {
        // WorldGenMultiTemplate gen = new WorldGenMultiTemplate(new
        // SpawnBiomeMatcher(struct.spawn));
        // gen.chance = struct.chance;
        // gen.syncGround = struct.syncGround;
        // for (Structure struct2 : struct.structures)
        // {
        // try
        // {
        // TemplateSet subGen = new TemplateSet(struct2.name, struct2.offset);
        // WorldGenMultiTemplate.Template template = new
        // WorldGenMultiTemplate.Template();
        // template.template = subGen;
        // String[] args = struct2.position.split(",");
        // BlockPos pos = new BlockPos(Integer.parseInt(args[0]),
        // Integer.parseInt(args[1]),
        // Integer.parseInt(args[2]));
        //
        // if (struct2.rotation != null) template.rotation =
        // Rotation.valueOf(struct2.rotation);
        // if (struct2.mirror != null) template.mirror =
        // Mirror.valueOf(struct2.mirror);
        // template.priority = struct2.priority;
        // template.position = pos;
        // template.chance = struct2.chance;
        // template.biome = struct2.biomeType;
        // gen.subTemplates.add(template);
        // }
        // catch (Exception e)
        // {
        // PokecubeCore.LOGGER.error(
        // (struct2.name + " " + struct2.spawn + " " + struct2.chance + " " +
        // struct2.offset), e);
        // }
        // }
        // if (PokecubeMod.debug) PokecubeCore.LOGGER.info(struct.name + " " +
        // gen.subTemplates + " " + struct.structures);
        // if (!gen.subTemplates.isEmpty())
        // {
        // WorldGenTemplates.templates.add(gen);
        // gen = new WorldGenMultiTemplate(new SpawnBiomeMatcher(struct.spawn));
        // gen.chance = struct.chance;
        // gen.syncGround = struct.syncGround;
        // if (PokecubeMod.debug) PokecubeMod
        // .log("Loaded Multi Structure: " + struct.name + " " + struct.spawn +
        // " " + struct.chance);
        // for (Structure struct2 : struct.structures)
        // {
        // try
        // {
        // TemplateSet subGen = new TemplateSet(struct2.name, struct2.offset);
        // WorldGenMultiTemplate.Template template = new
        // WorldGenMultiTemplate.Template();
        // template.template = subGen;
        // String[] args = struct2.position.split(",");
        // BlockPos pos = new BlockPos(Integer.parseInt(args[0]),
        // Integer.parseInt(args[1]),
        // Integer.parseInt(args[2]));
        //
        // if (struct2.rotation != null) template.rotation =
        // Rotation.valueOf(struct2.rotation);
        // if (struct2.mirror != null) template.mirror =
        // Mirror.valueOf(struct2.mirror);
        // template.priority = struct2.priority;
        // template.position = pos;
        // template.chance = struct2.chance;
        // template.biome = struct2.biomeType;
        // gen.subTemplates.add(template);
        // }
        // catch (Exception e)
        // {
        // PokecubeCore.LOGGER.error(
        // (struct2.name + " " + struct2.spawn + " " + struct2.chance + " " +
        // struct2.offset), e);
        // }
        // }
        // WorldGenTemplates.namedTemplates.put(struct.name, gen);
        // }
        // }
    }

    public static void reloadWorldgen()
    {
        // PokecubeTemplates.clear();
        // WorldGenTemplates.templates.clear();
        // WorldGenTemplates.namedTemplates.clear();
        // WorldGenTemplates.TemplateGenStartBuilding.clear();
        // init(DEFAULT);
        // for (String s : PokecubeCore.getConfig().extraWorldgenDatabases)
        // {
        // WorldgenHandler.loadStructures(s);
        // }
        // WorldgenHandler.processStructures();
        // loadCustomDims("custom_dims.json");
    }
}
