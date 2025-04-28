package pokecube.datagen;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import pokecube.mobs.PokecubeMobs;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeMobs.MODID)
public class PokecubeMobsDataGenerators
{

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        event.getGenerator()
                .addProvider(new PokecubeMobsAdvancements(event.getGenerator(), event.getExistingFileHelper()));
    }

}
