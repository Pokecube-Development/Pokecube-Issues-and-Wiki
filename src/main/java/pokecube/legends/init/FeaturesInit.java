package pokecube.legends.init;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import pokecube.legends.Reference;

public class FeaturesInit
{
    public static final String ID_ULTRA = Reference.ID + ":ultraspace";
    public static final String ID_DISTO = Reference.ID + ":distorted_world";

    private static final ResourceLocation IDLOC_ULTRA = new ResourceLocation(FeaturesInit.ID_ULTRA);
    private static final ResourceLocation IDLOC_DISTO = new ResourceLocation(FeaturesInit.ID_DISTO);

    // Dimensions
    public static final RegistryKey<World> ULTRASPACE_KEY = RegistryKey.create(
    		Registry.DIMENSION_REGISTRY, FeaturesInit.IDLOC_ULTRA);

    public static final RegistryKey<World> DISTORTEDWORLD_KEY = RegistryKey.create(
    		Registry.DIMENSION_REGISTRY, FeaturesInit.IDLOC_DISTO);
    //

    // Biomes
    public static final RegistryKey<Biome> BIOME_UB1 = RegistryKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_deep_cave"));
    public static final RegistryKey<Biome> BIOME_UB2 = RegistryKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_jungle"));
    public static final RegistryKey<Biome> BIOME_UB3 = RegistryKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_desert"));
    public static final RegistryKey<Biome> BIOME_UB4 = RegistryKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_dark_valley"));
    public static final RegistryKey<Biome> BIOME_UB5 = RegistryKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_deep_hills"));
    public static final RegistryKey<Biome> BIOME_UB6 = RegistryKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_forgotten_plains"));

    public static final RegistryKey<Biome> BIOME_DISTORTED = RegistryKey.create(Registry.BIOME_REGISTRY, FeaturesInit.IDLOC_DISTO);
    //

}
