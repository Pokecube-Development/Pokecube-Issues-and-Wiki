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
    public static final ResourceKey<Biome> BLINDING_DELTAS = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "blinding_deltas"));
    public static final ResourceKey<Biome> CRYSTALLIZED_BEACH = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "crystallized_beach"));
    public static final ResourceKey<Biome> DRIED_BLINDING_DELTAS = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "dried_blinding_deltas"));
    public static final ResourceKey<Biome> FORSAKEN_TAIGA = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "forsaken_taiga"));
    public static final ResourceKey<Biome> FUNGAL_FOREST = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "fungal_forest"));
    public static final ResourceKey<Biome> FUNGAL_PLAINS = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "fungal_plains"));
    public static final ResourceKey<Biome> MAGMATIC_BLINDING_DELTAS = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "magmatic_blinding_deltas"));
    public static final ResourceKey<Biome> MIRAGE_DESERT = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "mirage_desert"));
    public static final ResourceKey<Biome> OLD_GROWTH_FORSAKEN_TAIGA = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "old_growth_forsaken_taiga"));
    public static final ResourceKey<Biome> POLLUTED_OCEAN = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "polluted_ocean"));
    public static final ResourceKey<Biome> ROCKY_MIRAGE_DESERT = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "rocky_mirage_desert"));
    public static final ResourceKey<Biome> SHATTERED_BLINDING_DELTAS = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "shattered_blinding_deltas"));
    public static final ResourceKey<Biome> SHATTERED_TAINTED_BARRENS = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "shattered_tainted_barrens"));
    public static final ResourceKey<Biome> SNOWY_FORSAKEN_TAIGA = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "snowy_forsaken_taiga"));
    public static final ResourceKey<Biome> SPARSE_TEMPORAL_JUNGLE = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "sparse_temporal_jungle"));
    public static final ResourceKey<Biome> TAINTED_BARRENS = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "tainted_barrens"));
    public static final ResourceKey<Biome> TEMPORAL_BAMBOO_JUNGLE = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "temporal_bamboo_jungle"));
    public static final ResourceKey<Biome> TEMPORAL_JUNGLE = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "temporal_jungle"));
    public static final ResourceKey<Biome> WINDSWEPT_FORSAKEN_TAIGA = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "windswept_forsaken_taiga"));
    public static final ResourceKey<Biome> WINDSWEPT_TEMPORAL_JUNGLE = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "windswept_temporal_jungle"));
    public static final ResourceKey<Biome> VOLCANIC_BLINDING_DELTAS = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "volcanic_blinding_deltas"));

    public static final ResourceKey<Biome> DISTORTED_LANDS = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "distorted_lands"));
    public static final ResourceKey<Biome> SMALL_DISTORTED_ISLANDS = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Reference.ID, "small_distorted_islands"));
    //

}
