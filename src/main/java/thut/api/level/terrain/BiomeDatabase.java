package thut.api.level.terrain;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

@SuppressWarnings("deprecation")
public class BiomeDatabase
{
    private static Map<String, BiomeDictionary.Type> TYPES = Maps.newHashMap();

    private static Set<String> notTypes = Sets.newHashSet();

    public static boolean isAType(final String name)
    {
        if (BiomeDatabase.notTypes.contains(name)) return false;
        if (BiomeDatabase.TYPES.containsKey(name)) return true;
        if (BiomeDictionary.Type.hasType(name))
        {
            TYPES.put(name, BiomeDictionary.Type.getType(name));
            return true;
        }
        BiomeDatabase.notTypes.add(name);
        return false;
    }

    public static boolean contains(final ResourceKey<Biome> b, final String type)
    {
        if (!BiomeDatabase.isAType(type)) return false;
        final BiomeDictionary.Type t = BiomeDatabase.TYPES.get(type);
        return BiomeDictionary.hasType(b, t);
    }

    public static boolean isBiomeTag(final String name)
    {
        return name.startsWith("#");
    }
}