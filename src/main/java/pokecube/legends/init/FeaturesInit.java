package pokecube.legends.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import pokecube.legends.Reference;

public class FeaturesInit
{
    public static final String ID_ULTRA = Reference.ID + ":ultraspace";
    public static final String ID_DISTO = Reference.ID + ":distorted_world";

    private static final ResourceLocation IDLOC_ULTRA = new ResourceLocation(FeaturesInit.ID_ULTRA);
    private static final ResourceLocation IDLOC_DISTO = new ResourceLocation(FeaturesInit.ID_DISTO);

    // Dimensions
    public static final ResourceKey<Level> ULTRASPACE_KEY = ResourceKey.create(Registry.DIMENSION_REGISTRY,
            FeaturesInit.IDLOC_ULTRA);

    public static final ResourceKey<Level> DISTORTEDWORLD_KEY = ResourceKey.create(Registry.DIMENSION_REGISTRY,
            FeaturesInit.IDLOC_DISTO);

    public static void init(IEventBus bus)
    {
    }
}
