package pokecube.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import pokecube.api.entity.EntityProvider;
import pokecube.api.entity.IEntityProvider;
import thut.core.common.ThutCore;

public class PokecubeAPI
{
    public static final Logger LOGGER = LogManager.getLogger(PokecubeAPI.MODID);
    public static final String MODID = "pokecube";

    // Bus for move events
    public static final IEventBus MOVE_BUS = BusBuilder.builder().build();
    // Bus for Pokemob Events
    public static final IEventBus POKEMOB_BUS = BusBuilder.builder().build();

    // Provider for entities.
    public static IEntityProvider provider = new EntityProvider(null);

    /**
     * Allows for dealing with cases like pokeplayer, where the entity that the
     * world stores is not necessarily the one wanted for pokemob interaction.
     *
     * @return
     */
    public static IEntityProvider getEntityProvider()
    {
        return PokecubeAPI.provider;
    }

    public static void logInfo(Object... args)
    {
        ThutCore.log(LOGGER::info, args);
    }

    public static void logDebug(Object... args)
    {
        ThutCore.log(LOGGER::debug, args);
    }
}
