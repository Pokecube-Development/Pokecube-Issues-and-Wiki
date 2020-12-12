package pokecube.compat.world_blender;

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
            PokecubeCore.POKEMOB_BUS.addListener(Compat::registerDatabases);
            FMLJavaModLoadingContext.get().getModEventBus().register(Compat.class);
            new WorldgenHandler(Compat.MODID, FMLJavaModLoadingContext.get().getModEventBus());
        }
    }

    public static void registerDatabases(final InitDatabase.Pre evt)
    {
        PokecubeCore.LOGGER.debug("Registering World Blender Databases");
        Database.addDatabase("world_blender:database/pokemobs/pokemobs_spawns.json", EnumDatabase.POKEMON);
    }
}
