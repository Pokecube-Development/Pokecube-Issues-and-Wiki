package pokecube.compat.advancedrocketry;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import pokecube.adventures.events.CompatEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.events.onload.InitDatabase;

@Mod.EventBusSubscriber
public class Compat
{

    static
    {
        pokecube.compat.Compat.BUS.register(Compat.class);
        PokecubeCore.POKEMOB_BUS.register(Compat.class);
    }

    @SubscribeEvent
    public void registerDatabases(final InitDatabase.Pre evt)
    {
        if (!ModList.get().isLoaded("advancedrocketry")) return;

        // Adds a custom spawns database
        Database.addDatabase("pokecube_compat:database/pokemobs/pokemobs_spawns.json", EnumDatabase.POKEMON);
    }

    @SubscribeEvent
    public static void register(final CompatEvent event)
    {
        if (ModList.get().isLoaded("advancedrocketry")) Impl.register();
    }
}
