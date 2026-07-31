package pokecube.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import pokecube.mobs.PokecubeMobs;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = PokecubeMobs.MODID)
public class PokecubeMobsDataGenerators
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        // other providers here
        generator.addProvider(event.includeServer(),
                new PokecubeMobsAdvancements(output, lookupProvider, existingFileHelper));
    }
}
