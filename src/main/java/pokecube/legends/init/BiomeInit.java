package pokecube.legends.init;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.legends.worldgen.biomes.UltraUB1;
import pokecube.legends.worldgen.biomes.UltraUB2;
import pokecube.legends.worldgen.biomes.UltraUB3;
import pokecube.legends.worldgen.biomes.UltraUB4;

public class BiomeInit
{
    public static final Biome BIOME_UB1 = new UltraUB1();
    public static final Biome BIOME_UB2 = new UltraUB2();
    public static final Biome BIOME_UB3 = new UltraUB3();
    public static final Biome BIOME_UB4 = new UltraUB4();

    public static void registerBiomes(final Register<Biome> event)
    {
        // New Biomes
        BiomeInit.initBiome(event.getRegistry(), BiomeInit.BIOME_UB1, "pokecube_legends:ub001", BiomeType.ICY,
                Type.MUSHROOM, Type.SWAMP, Type.SPOOKY, Type.DRY);
        BiomeInit.initBiome(event.getRegistry(), BiomeInit.BIOME_UB2, "pokecube_legends:ub002", BiomeType.DESERT,
                Type.DEAD, Type.SANDY, Type.WASTELAND, Type.HOT);
        BiomeInit.initBiome(event.getRegistry(), BiomeInit.BIOME_UB3, "pokecube_legends:ub003", BiomeType.WARM,
                Type.DENSE, Type.JUNGLE, Type.FOREST, Type.WATER, Type.MOUNTAIN);
        BiomeInit.initBiome(event.getRegistry(), BiomeInit.BIOME_UB4, "pokecube_legends:ub004", BiomeType.COOL,
                Type.CONIFEROUS, Type.MESA, Type.SNOWY);
    }

    private static Biome initBiome(final IForgeRegistry<Biome> registry, final Biome biome, final String name,
            final BiomeType bType, final Type... types)
    {
        biome.setRegistryName(name);
        registry.register(biome);
        BiomeDictionary.addTypes(biome, types);
        return biome;
    }
}