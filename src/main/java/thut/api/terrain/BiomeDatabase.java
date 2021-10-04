package thut.api.terrain;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import thut.core.common.ThutCore;

public class BiomeDatabase
{
    private static Map<String, BiomeDictionary.Type> TYPES = Maps.newHashMap();

    private static Set<String> notTypes = Sets.newHashSet();

    public static ResourceKey<Biome> getKey(final Biome b)
    {
        return ResourceKey.create(Registry.BIOME_REGISTRY, b.getRegistryName());
    }

    public static Biome getBiome(final ResourceKey<Biome> key)
    {
        final RegistryAccess REG = ThutCore.proxy.getRegistries();
        final Registry<Biome> biomes = REG.registryOrThrow(Registry.BIOME_REGISTRY);
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

    public static boolean contains(final ResourceKey<Biome> b, final String type)
    {
        if (!BiomeDatabase.isAType(type)) return false;
        final BiomeDictionary.Type t = BiomeDatabase.TYPES.get(type);
        return BiomeDictionary.hasType(b, t);
    }

}