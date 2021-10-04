package pokecube.mobs.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.mobs.PokecubeMobs;
import pokecube.mobs.client.smd.SMDModel;
import thut.core.client.render.model.ModelFactory;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeMobs.MODID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        // Register smd format for models
        ModelFactory.registerIModel("smd", SMDModel::new);

        // Override the pokemobs gui size map with ours
        GuiPokemobBase.SIZEMAP = new ResourceLocation(PokecubeMobs.MODID, "pokemobs_gui_sizes.json");
        GuiPokemobBase.initSizeMap();
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static class Listener extends SimplePreparableReloadListener<Object>
    {
        @SubscribeEvent
        public static void resourcesLoaded(final AddReloadListenerEvent event)
        {
            event.addListener(new Listener());
        }

        @Override
        protected Object prepare(final ResourceManager resourceManagerIn, final ProfilerFiller profilerIn)
        {
            return null;
        }

        @Override
        protected void apply(final Object objectIn, final ResourceManager resourceManagerIn,
                final ProfilerFiller profilerIn)
        {
            GuiPokemobBase.initSizeMap();
        }

    }
}
