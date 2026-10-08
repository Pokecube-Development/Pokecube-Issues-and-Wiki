package pokecube.mobs.init;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.items.berries.BerryManager;
import pokecube.mobs.PokecubeMobs;
import pokecube.mobs.client.smd.SMDModel;
import thut.core.client.render.model.ModelFactory;

@EventBusSubscriber(modid = PokecubeMobs.MODID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    @SubscribeEvent
    public static void colourBlocks(final RegisterColorHandlersEvent.Block event)
    {
        final Block qualotLeaves = BerryManager.berryLeaves.get(23).get();
        event.register(
                (state, reader, pos, tintIndex) -> reader != null && pos != null ? BiomeColors.getAverageFoliageColor(
                        reader, pos) : FoliageColor.getDefaultColor(), qualotLeaves);
    }

    @SubscribeEvent
    public static void colourItems(RegisterColorHandlersEvent.Item event)
    {
        final Block qualotLeaves = BerryManager.berryLeaves.get(23).get();
        event.register((stack, tintIndex) -> {
            final BlockState blockstate = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
            return event.getBlockColors().getColor(blockstate, null, null, tintIndex);
        }, qualotLeaves);
    }

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        // Register smd format for models
        ModelFactory.registerIModel("smd", SMDModel::new);

        // Override the pokemobs gui size map with ours
        GuiPokemobHelper.SIZEMAP = ResourceLocation.fromNamespaceAndPath(PokecubeMobs.MODID, "pokemobs_gui_sizes.json");
        GuiPokemobHelper.initSizeMap();
    }

    @EventBusSubscriber(value = Dist.CLIENT)
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
            GuiPokemobHelper.initSizeMap();
        }

    }
}
