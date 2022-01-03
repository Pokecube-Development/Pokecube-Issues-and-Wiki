package thut.concrete.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import thut.concrete.Concrete;
import thut.concrete.data.Datagens.BStateProvider;
import thut.concrete.data.Datagens.BTagsProvider;
import thut.concrete.data.Datagens.ConcRecipes;
import thut.concrete.data.Datagens.ITagsProvider;
import thut.concrete.data.Datagens.LangsProvider;
import thut.concrete.data.Datagens.LootTablesProv;

@Mod.EventBusSubscriber(modid = Concrete.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneratorReg
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer())
        {
            generator.addProvider(new ConcRecipes(generator));
            generator.addProvider(new LootTablesProv(generator));
            BTagsProvider blockTags = new BTagsProvider(generator, event.getExistingFileHelper());
            generator.addProvider(blockTags);
            generator.addProvider(new ITagsProvider(generator, blockTags, event.getExistingFileHelper()));
        }
        if (event.includeClient())
        {
            generator.addProvider(new BStateProvider(generator, event.getExistingFileHelper()));
            generator.addProvider(new LangsProvider(generator, "en_us"));
        }
    }
}
