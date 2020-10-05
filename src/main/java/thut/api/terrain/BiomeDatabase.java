package thut.api.terrain;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class BiomeDatabase
{
    public static boolean contains(final Biome b, final Type type)
    {
        return BiomeDictionary.hasType(b, type);
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