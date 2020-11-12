package pokecube.legends.init;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import pokecube.legends.Reference;

public class DimensionInit
{
    public static final RegistryKey<World> ULTRASPACE_KEY = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, name("ultraspace"));
    public static final RegistryKey<DimensionType> ULTRASPACE_TYPE = RegistryKey.getOrCreateKey(Registry.DIMENSION_TYPE_KEY, name("ultraspace"));
    
    private static ResourceLocation name (String name) {
        return new ResourceLocation(Reference.ID, name);
    }
}
