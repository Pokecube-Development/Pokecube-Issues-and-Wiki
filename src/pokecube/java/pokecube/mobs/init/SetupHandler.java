package pokecube.mobs.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.core.database.Database;
import pokecube.mobs.PokecubeMobs;

@EventBusSubscriber(modid = PokecubeMobs.MODID)
public class SetupHandler
{
    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        TypeTrainer.merchant.pokemon.add(Database.getEntry("rattata"));
        TypeTrainer.merchant.overrideLevel = 50;
    }
}
