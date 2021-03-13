package pokecube.compat.world_blender;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pokecube.core.PokecubeCore;

@Mod.EventBusSubscriber
public class Compat
{
    public static final String MODID = "world_blender";

    static
    {
        PokecubeCore.LOGGER.debug("Checking World Blender: " + ModList.get().isLoaded(Compat.MODID));
        if (ModList.get().isLoaded(Compat.MODID)) FMLJavaModLoadingContext.get().getModEventBus().register(
                Compat.class);
    }
}
