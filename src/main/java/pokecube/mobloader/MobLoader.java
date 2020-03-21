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
        for (int i = 0; i < Math.min(PokecubeCore.getConfig().configDatabases.size(), 3); i++)
        {
            final String[] args = PokecubeCore.getConfig().configDatabases.get(i).split(";");
            for (final String s : args)
                if (!s.trim().isEmpty()) Database.addDatabase(s, EnumDatabase.values()[i]);
        }
    }
}
