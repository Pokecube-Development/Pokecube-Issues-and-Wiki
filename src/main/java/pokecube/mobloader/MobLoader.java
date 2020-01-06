package pokecube.mobloader;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.events.onload.InitDatabase;

@Mod.EventBusSubscriber
public class MobLoader
{
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void registerDatabases(final InitDatabase.Pre evt)
    {
        Database.addDatabase("pokemobs_pokedex.json", EnumDatabase.POKEMON);
        Database.addDatabase("pokemobs_spawns.json", EnumDatabase.POKEMON);
        Database.addDatabase("pokemobs_drops.json", EnumDatabase.POKEMON);
        Database.addDatabase("pokemobs_interacts.json", EnumDatabase.POKEMON);

        Database.addDatabase("moves.json", EnumDatabase.MOVES);
        Database.addDatabase("spawns.json", EnumDatabase.BERRIES);
    }
}
