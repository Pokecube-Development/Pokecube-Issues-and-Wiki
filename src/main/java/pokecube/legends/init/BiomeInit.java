package pokecube.legends.init;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.legends.worldgen.biomes.UltraUB1;
import pokecube.legends.worldgen.biomes.UltraUB2;
import pokecube.legends.worldgen.biomes.UltraUB3;
import pokecube.legends.worldgen.biomes.UltraUB4;
import pokecube.legends.worldgen.biomes.UltraUB5;
import pokecube.legends.worldgen.biomes.UltraUB6;

public class BiomeInit
{
    public static final Biome BIOME_UB1 = new UltraUB1();
    public static final Biome BIOME_UB2 = new UltraUB2();
    public static final Biome BIOME_UB3 = new UltraUB3();
    public static final Biome BIOME_UB4 = new UltraUB4();
    public static final Biome BIOME_UB5 = new UltraUB5();
    public static final Biome BIOME_UB6 = new UltraUB6();

    public static void registerBiomes(final Register<Biome> event)
    {
        // New Biomes
        BiomeInit.initBiome(event.getRegistry(), BiomeInit.BIOME_UB1, "pokecube_legends:ub001", BiomeType.COOL,
                Type.MUSHROOM, Type.SWAMP, Type.SPOOKY);
        BiomeInit.initBiome(event.getRegistry(), BiomeInit.BIOME_UB2, "pokecube_legends:ub002", BiomeType.WARM,
                Type.JUNGLE, Type.FOREST, Type.MOUNTAIN);
        BiomeInit.initBiome(event.getRegistry(), BiomeInit.BIOME_UB3, "pokecube_legends:ub003", BiomeType.DESERT,
                Type.SANDY, Type.MESA, Type.BEACH);
        BiomeInit.initBiome(event.getRegistry(), BiomeInit.BIOME_UB4, "pokecube_legends:ub004", BiomeType.ICY,
                Type.WASTELAND, Type.DEAD, Type.MOUNTAIN);
        BiomeInit.initBiome(event.getRegistry(), BiomeInit.BIOME_UB5, "pokecube_legends:ub005", BiomeType.WARM,
                Type.MESA, Type.PLATEAU, Type.PLAINS);
        BiomeInit.initBiome(event.getRegistry(), BiomeInit.BIOME_UB6, "pokecube_legends:ub006", BiomeType.COOL,
                Type.OCEAN, Type.VOID, Type.PLAINS);
    }

    private static Biome initBiome(final IForgeRegistry<Biome> registry, final Biome biome, final String name,
    		final BiomeType bType, final Type... types)
    {
        biome.setRegistryName(name);
        registry.register(biome);
        BiomeDictionary.addTypes(biome, types);
        BiomeManager.addSpawnBiome(biome);
        
        //Biome Spawn Overworld
        //BiomeManager.addBiome(bType, new BiomeManager.BiomeEntry(biome, chance));
        //
        return biome;
    }
}