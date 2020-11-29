package thut.api.terrain;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeDatabase
{
    private static Map<String, BiomeDictionary.Type> TYPES = Maps.newHashMap();

    private static Set<String> notTypes = Sets.newHashSet();

    public static RegistryKey<Biome> getKey(final Biome b)
    {
        return RegistryKey.getOrCreateKey(Registry.BIOME_KEY, b.getRegistryName());
    }

    public static boolean isAType(final String name)
    {
        if (BiomeDatabase.notTypes.contains(name)) return false;
        if (BiomeDatabase.TYPES.containsKey(name)) return true;
        for (final BiomeDictionary.Type t : BiomeDictionary.Type.getAll())
            if (name.equalsIgnoreCase(t.getName()))
            {
                BiomeDatabase.TYPES.put(name, t);
                return true;
            }
        BiomeDatabase.notTypes.add(name);
        return false;
    }

    public static boolean contains(final Biome b, final String type)
    {
        return BiomeDatabase.contains(BiomeDatabase.getKey(b), type);
    }

    public static boolean contains(final RegistryKey<Biome> b, final String type)
    {
        if (!BiomeDatabase.isAType(type)) return false;
        final BiomeDictionary.Type t = BiomeDatabase.TYPES.get(type);
        final RegistryKey<Biome> key = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, b.getRegistryName());
        return BiomeDictionary.hasType(key, t);
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