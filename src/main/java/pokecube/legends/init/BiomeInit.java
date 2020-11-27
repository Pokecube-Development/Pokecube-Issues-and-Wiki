package pokecube.legends.init;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import pokecube.legends.Reference;
//import pokecube.legends.worldgen.biomes.UltraUB1;
//import pokecube.legends.worldgen.biomes.UltraUB2;
//import pokecube.legends.worldgen.biomes.UltraUB3;
//import pokecube.legends.worldgen.biomes.UltraUB4;
//import pokecube.legends.worldgen.biomes.UltraUB5;
//import pokecube.legends.worldgen.biomes.UltraUB6;

public class BiomeInit
{
    public static final RegistryKey<Biome> BIOME_UB1 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, BiomeInit.name("ultra_deep_cave"));
    public static final RegistryKey<Biome> BIOME_UB2 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, BiomeInit.name("ultra_jungle"));
    public static final RegistryKey<Biome> BIOME_UB3 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, BiomeInit.name("ultra_desert"));
    public static final RegistryKey<Biome> BIOME_UB4 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, BiomeInit.name("ultra_dark_valley"));
    public static final RegistryKey<Biome> BIOME_UB5 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, BiomeInit.name("ultra_deep_hills"));
    public static final RegistryKey<Biome> BIOME_UB6 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, BiomeInit.name("ultra_forgotten_plains"));

    /*public static void registerBiomes(final RegistryKey<Biome> event)
    {
        // New Biomes
        BiomeInit.initBiome(event., BiomeInit.BIOME_UB1, "pokecube_legends:ub001", BiomeType.COOL,
                Type.MUSHROOM, Type.SWAMP, Type.SPOOKY);
        BiomeInit.initBiome(event.getRegistryName(), BiomeInit.BIOME_UB2, "pokecube_legends:ub002", BiomeType.WARM,
                Type.JUNGLE, Type.FOREST, Type.MOUNTAIN);
        BiomeInit.initBiome(event.getRegistryName(), BiomeInit.BIOME_UB3, "pokecube_legends:ub003", BiomeType.DESERT,
                Type.SANDY, Type.MESA, Type.BEACH);
        BiomeInit.initBiome(event.getRegistryName(), BiomeInit.BIOME_UB4, "pokecube_legends:ub004", BiomeType.ICY,
                Type.WASTELAND, Type.DEAD, Type.MOUNTAIN);
        BiomeInit.initBiome(event.getRegistryName(), BiomeInit.BIOME_UB5, "pokecube_legends:ub005", BiomeType.WARM,
                Type.MESA, Type.PLATEAU, Type.PLAINS);
        BiomeInit.initBiome(event.getRegistryName(), BiomeInit.BIOME_UB6, "pokecube_legends:ub006", BiomeType.COOL,
                Type.OCEAN, Type.VOID, Type.PLAINS);
    }*/


    private static ResourceLocation name(final String name) {
        return new ResourceLocation(Reference.ID, name);
    }

    /*private static Biome initBiome(final RegistryKey<Biome> registry, final Biome biome, final String name,
    		final BiomeType bType, final Type... types)
    {
        biome.setRegistryName(name);
        //registry.register(biome);
        BiomeDictionary.addTypes(registry, types);
        BiomeManager.addBiome(bType, null);

        //Biome Spawn Overworld
        //BiomeManager.addBiome(bType, new BiomeManager.BiomeEntry(biome, chance));
        //
        return biome;
    }*/
}