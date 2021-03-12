package thut.api.terrain;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import thut.core.common.ThutCore;

public class BiomeDatabase
{
    private static Map<String, BiomeDictionary.Type> TYPES = Maps.newHashMap();

    private static Set<String> notTypes = Sets.newHashSet();

    public static RegistryKey<Biome> getKey(final Biome b)
    {
        return RegistryKey.create(Registry.BIOME_REGISTRY, b.getRegistryName());
    }

    public static Biome getBiome(final RegistryKey<Biome> key)
    {
        final DynamicRegistries REG = ThutCore.proxy.getRegistries();
        final MutableRegistry<Biome> biomes = REG.registryOrThrow(Registry.BIOME_REGISTRY);
        return biomes.get(key.location());
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
        return BiomeDictionary.hasType(b, t);
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