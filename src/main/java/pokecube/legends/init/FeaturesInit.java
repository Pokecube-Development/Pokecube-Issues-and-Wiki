package pokecube.legends.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import pokecube.legends.Reference;

public class FeaturesInit
{
    public static final String ID_ULTRA = Reference.ID + ":ultraspace";
    public static final String ID_DISTO = Reference.ID + ":distorted_world";

    private static final ResourceLocation IDLOC_ULTRA = new ResourceLocation(FeaturesInit.ID_ULTRA);
    private static final ResourceLocation IDLOC_DISTO = new ResourceLocation(FeaturesInit.ID_DISTO);

    // Dimensions
    public static final ResourceKey<Level> ULTRASPACE_KEY = ResourceKey.create(
    		Registry.DIMENSION_REGISTRY, FeaturesInit.IDLOC_ULTRA);

    public static final ResourceKey<Level> DISTORTEDWORLD_KEY = ResourceKey.create(
    		Registry.DIMENSION_REGISTRY, FeaturesInit.IDLOC_DISTO);
    //

    // Biomes
    public static final ResourceKey<Biome> BIOME_UB1 = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_deep_cave"));
    public static final ResourceKey<Biome> BIOME_UB2 = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_jungle"));
    public static final ResourceKey<Biome> BIOME_UB3 = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_desert"));
    public static final ResourceKey<Biome> BIOME_UB4 = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_dark_valley"));
    public static final ResourceKey<Biome> BIOME_UB5 = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_deep_hills"));
    public static final ResourceKey<Biome> BIOME_UB6 = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "ultra_forgotten_plains"));

    public static final ResourceKey<Biome> BIOME_DISTORTED = ResourceKey.create(Registry.BIOME_REGISTRY, FeaturesInit.IDLOC_DISTO);
    //

}
