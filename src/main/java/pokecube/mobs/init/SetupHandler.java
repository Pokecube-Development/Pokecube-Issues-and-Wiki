package pokecube.mobs.init;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.core.database.Database;
import pokecube.mobs.MiscItemHelper;
import pokecube.mobs.PokecubeMobs;
import thut.core.common.ThutCore;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeMobs.MODID)
public class SetupHandler
{
    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        ThutCore.FORGE_BUS.register(MiscItemHelper.class);
        TypeTrainer.merchant.pokemon.add(Database.getEntry("rattata"));
        TypeTrainer.merchant.overrideLevel = 50;
    }
}
