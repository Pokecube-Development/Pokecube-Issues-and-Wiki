package thut.api.terrain;

import net.minecraft.world.biome.Biome;

public class BiomeDatabase
{
    // private static int lastTypesSize = -1;
    //
    // private static Map<String, BiomeDictionary.Type> typeMap =
    // Maps.newHashMap();
    //
    // private static BiomeDictionary.Type getBiomeType(String name)
    // {
    // name = name.toUpperCase();
    // if (BiomeDatabase.lastTypesSize != BiomeDictionary.Type.getAll().size())
    // {
    // BiomeDatabase.typeMap.clear();
    // for (final BiomeDictionary.Type type : BiomeDictionary.Type.getAll())
    // BiomeDatabase.typeMap.put(type.getName(), type);
    // }
    // return BiomeDatabase.typeMap.get(name);
    // }

    public static boolean isAType(final String name)
    {
        // TODO fix this.
        return false;// BiomeDatabase.getBiomeType(name) != null;
    }

    public static boolean contains(final Biome b, final String type)
    {
        // TODO fix this.
        return false;
        // final BiomeDictionary.Type bType = BiomeDatabase.getBiomeType(type);
        // if (bType == null) return false;
        // return BiomeDictionary.hasType(b, bType);
    }

    public static String getBiomeName(final Biome biome)
    {
        return biome.getRegistryName().getNamespace();
    }

    public static String getUnlocalizedNameFromType(final int type)
    {
        return BiomeType.getType(type).readableName;
    }

}