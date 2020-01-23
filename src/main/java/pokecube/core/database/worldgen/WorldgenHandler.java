package pokecube.core.database.worldgen;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.gen.structure.ConfigStructure;

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
        float            chance = 1;
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
    }

    public static class Structures
    {
        public List<Structure>      structures      = Lists.newArrayList();
        public List<MultiStructure> multiStructures = Lists.newArrayList();
    }

    public static File DEFAULT;

    public static CustomDims dims;

    public static ResourceLocation ROOT     = new ResourceLocation(PokecubeCore.MODID, "structures/");
    public static Structures       defaults = new Structures();

    public static void loadStructures() throws Exception
    {
        final ResourceLocation json = new ResourceLocation(WorldgenHandler.ROOT.toString() + "worldgen.json");
        final InputStream res = Database.resourceManager.getResource(json).getInputStream();
        final Reader reader = new InputStreamReader(res);
        final Structures database = PokedexEntryLoader.gson.fromJson(reader, Structures.class);
        WorldgenHandler.defaults.structures.addAll(database.structures);
        WorldgenHandler.defaults.multiStructures.addAll(database.multiStructures);
    }

    public static void processStructures(final RegistryEvent.Register<Feature<?>> event)
    {
        try
        {
            WorldgenHandler.loadStructures();
        }
        catch (final Exception e)
        {
            PokecubeMod.LOGGER.catching(e);
        }

        for (final Structure struct : WorldgenHandler.defaults.structures)
        {
            final String structname = WorldgenHandler.ROOT.toString() + struct.name.replaceAll("/", "_").toLowerCase(
                    Locale.ROOT);
            final ResourceLocation regname = new ResourceLocation(structname);
            final ConfigStructure toAdd = new ConfigStructure(regname);
            toAdd.structLoc = new ResourceLocation(WorldgenHandler.ROOT.toString() + struct.name);
            toAdd.subbiome = struct.biomeType;

            event.getRegistry().register(toAdd);
            final SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(struct.spawn);

            for (final Biome b : ForgeRegistries.BIOMES.getValues())
            {
                if (!matcher.checkBiome(b)) continue;
                b.addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Biome.createDecoratedFeature(toAdd,
                        IFeatureConfig.NO_FEATURE_CONFIG, Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
                b.addStructure(toAdd, IFeatureConfig.NO_FEATURE_CONFIG);
            }
        }
    }
}
