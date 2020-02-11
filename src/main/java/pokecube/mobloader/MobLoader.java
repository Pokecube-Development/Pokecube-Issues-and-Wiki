package pokecube.mobloader;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.events.onload.InitDatabase;

public class MobLoader
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerDatabases(final InitDatabase.Pre evt)
    {
        PokecubeCore.LOGGER.debug("Registering Loadable Databases");
        Database.addDatabase("database/pokemobs/pokemobs_pokedex.json", EnumDatabase.POKEMON);
        Database.addDatabase("database/moves.json", EnumDatabase.MOVES);
        Database.addDatabase("database/spawns.json", EnumDatabase.BERRIES);
    }
}
