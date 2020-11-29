package pokecube.legends.init;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.Reference;

public class FeaturesInit
{	
    public static final String ID_ULTRA = Reference.ID + ":ultraspace";
    public static final String ID_DISTO = Reference.ID + ":distorted_world";

    private static final ResourceLocation IDLOC_ULTRA = new ResourceLocation(FeaturesInit.ID_ULTRA);
    private static final ResourceLocation IDLOC_DISTO = new ResourceLocation(FeaturesInit.ID_DISTO);
    
    // Dimensions
    public static final RegistryKey<World> ULTRASPACE_KEY = RegistryKey.getOrCreateKey(
    		Registry.WORLD_KEY, FeaturesInit.IDLOC_ULTRA);
    
    public static final RegistryKey<World> DISTORTEDWORLD_KEY = RegistryKey.getOrCreateKey(
    		Registry.WORLD_KEY, FeaturesInit.IDLOC_DISTO);
    //
    
    // Biomes
    public static final RegistryKey<Biome> BIOME_UB1 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, FeaturesInit.IDLOC_ULTRA);
    public static final RegistryKey<Biome> BIOME_UB2 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, FeaturesInit.IDLOC_ULTRA);
    public static final RegistryKey<Biome> BIOME_UB3 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, FeaturesInit.IDLOC_ULTRA);
    public static final RegistryKey<Biome> BIOME_UB4 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, FeaturesInit.IDLOC_ULTRA);
    public static final RegistryKey<Biome> BIOME_UB5 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, FeaturesInit.IDLOC_ULTRA);
    public static final RegistryKey<Biome> BIOME_UB6 = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, FeaturesInit.IDLOC_ULTRA);
    
    public static final RegistryKey<Biome> BIOME_DISTORTED = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, FeaturesInit.IDLOC_DISTO);
    //
    
}
