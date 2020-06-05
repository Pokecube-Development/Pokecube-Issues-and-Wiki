package pokecube.compat.world_blender;

import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.events.onload.InitDatabase;

@Mod.EventBusSubscriber
public class Compat
{
    public static final String MODID = "world_blender";

    static
    {
        PokecubeCore.LOGGER.debug("Checking World Blender: " + ModList.get().isLoaded(Compat.MODID));
        if (ModList.get().isLoaded(Compat.MODID))
        {
            PokecubeCore.POKEMOB_BUS.register(Compat.class);
            FMLJavaModLoadingContext.get().getModEventBus().register(Compat.class);
        }
    }

    @SubscribeEvent
    public static void registerDatabases(final InitDatabase.Pre evt)
    {
        PokecubeCore.LOGGER.debug("Registering World Blender Databases");
        Database.addDatabase("world_blender:database/pokemobs/pokemobs_spawns.json", EnumDatabase.POKEMON);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerFeatures(final RegistryEvent.Register<Feature<?>> event)
    {
        PokecubeCore.LOGGER.debug("Registering World Blender Features");
        new WorldgenHandler(Compat.MODID).processStructures(event);
    }
}
