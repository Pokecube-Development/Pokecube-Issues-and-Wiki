package pokecube.adventures.init;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.PoweredContainer;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.client.gui.blocks.AFA;
import pokecube.adventures.client.gui.blocks.Cloner;
import pokecube.adventures.client.gui.blocks.Extractor;
import pokecube.adventures.client.gui.blocks.Splicer;
import pokecube.adventures.client.gui.items.Bag;
import pokecube.adventures.client.gui.trainer.Trainer;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.client.render.mobs.RenderNPC;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.client.gui.ConfigGui;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeAdv.MODID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    public static class EventHandler
    {
        @SubscribeEvent
        public static void onKey(final InputEvent.KeyInputEvent event)
        {
            if (ClientSetupHandler.trainerEditKey.isPressed())
            {
                final RayTraceResult pos = Minecraft.getInstance().objectMouseOver;
                Entity target = null;
                switch (pos.getType())
                {
                case ENTITY:
                    target = ((EntityRayTraceResult) pos).getEntity();
                    break;
                default:
                    break;
                }
                PacketTrainer.requestEdit(target);
            }
        }

        @SubscribeEvent
        public static void onToolTip(final ItemTooltipEvent evt)
        {
            final PlayerEntity player = evt.getPlayer();
            final ItemStack stack = evt.getItemStack();
            if (stack.isEmpty()) return;
            final CompoundNBT tag = stack.hasTag() ? stack.getTag() : new CompoundNBT();
            if (tag.getBoolean("isapokebag")) evt.getToolTip().add(new TranslationTextComponent(PokecubeAdv.MODID
                    + ".tooltip.bag"));
            if (tag.contains("dyeColour"))
            {
                final ITextComponent colour = new TranslationTextComponent(DyeColor.byId(tag.getInt("dyeColour"))
                        .getTranslationKey());
                boolean has = false;
                for (final ITextComponent s : evt.getToolTip())
                {
                    has = s.equals(colour);
                    if (has) break;
                }
                if (!has) evt.getToolTip().add(colour);
            }
            if (player == null || player.openContainer == null) return;
            if (player.openContainer instanceof PoweredContainer || Screen.hasShiftDown() && !ClonerHelper
                    .getGeneSelectors(stack).isEmpty())
            {
                final IMobGenetics genes = ClonerHelper.getGenes(stack);
                final int index = ClonerHelper.getIndex(stack);
                if (genes != null) for (final Alleles a : genes.getAlleles().values())
                {
                    TranslationTextComponent comp = new TranslationTextComponent(PokecubeAdv.MODID
                            + ".tooltip.gene.expressed." + a.getExpressed().getKey().getPath(), a.getExpressed());
                    evt.getToolTip().add(comp);
                    if (Config.instance.expandedDNATooltips || Screen.hasControlDown())
                    {
                        comp = new TranslationTextComponent(PokecubeAdv.MODID + ".tooltip.gene.parent." + a
                                .getExpressed().getKey().getPath(), a.getAlleles()[0], a.getAlleles()[1]);
                        evt.getToolTip().add(comp);
                    }
                }
                if (genes != null && !(Config.instance.expandedDNATooltips || Screen.hasControlDown())) evt.getToolTip()
                        .add(new TranslationTextComponent(PokecubeAdv.MODID + ".tooltip.gene.expand"));
                if (index != -1) evt.getToolTip().add(new TranslationTextComponent(PokecubeAdv.MODID
                        + ".tooltip.gene.array.index", index));
                Set<Class<? extends Gene>> genesSet;
                if (!(genesSet = ClonerHelper.getGeneSelectors(stack)).isEmpty()) if (Screen.hasControlDown())
                    for (final Class<? extends Gene> geneC : genesSet)
                    try
                    {
                        final Gene gene = geneC.newInstance();
                        evt.getToolTip().add(new TranslationTextComponent(PokecubeAdv.MODID + ".tooltip.selector.gene."
                                + gene.getKey().getPath()));
                    }
                    catch (InstantiationException | IllegalAccessException e)
                    {

                    }
                else evt.getToolTip().add(new TranslationTextComponent(PokecubeAdv.MODID + ".tooltip.gene.expand"));
                if (RecipeSelector.isSelector(stack))
                {
                    final SelectorValue value = ClonerHelper.getSelectorValue(stack);
                    value.addToTooltip(evt.getToolTip());
                }
            }
        }
    }

    public static KeyBinding trainerEditKey;

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);

        RenderingRegistry.registerEntityRenderingHandler(TrainerNpc.TYPE, (manager) -> new RenderNPC<>(manager));
        RenderingRegistry.registerEntityRenderingHandler(LeaderNpc.TYPE, (manager) -> new RenderNPC<>(manager));

        // Register container guis.
        ScreenManager.registerFactory(PokecubeAdv.CLONER_CONT.get(), Cloner::new);
        ScreenManager.registerFactory(PokecubeAdv.SPLICER_CONT.get(), Splicer::new);
        ScreenManager.registerFactory(PokecubeAdv.EXTRACTOR_CONT.get(), Extractor::new);
        ScreenManager.registerFactory(PokecubeAdv.AFA_CONT.get(), AFA::new);
        ScreenManager.registerFactory(PokecubeAdv.BAG_CONT.get(), Bag<BagContainer>::new);
        ScreenManager.registerFactory(PokecubeAdv.TRAINER_CONT.get(), Trainer::new);

        RenderTypeLookup.setRenderLayer(PokecubeAdv.CLONER.get(), RenderType.getTranslucent());
        RenderTypeLookup.setRenderLayer(PokecubeAdv.EXTRACTOR.get(), RenderType.getTranslucent());
        RenderTypeLookup.setRenderLayer(PokecubeAdv.SPLICER.get(), RenderType.getTranslucent());

        // Register config gui
        ModList.get().getModContainerById(PokecubeAdv.MODID).ifPresent(c -> c.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new ConfigGui(PokecubeAdv.config, parent)));

        ClientSetupHandler.trainerEditKey = new KeyBinding("EditTrainer", InputMappings.INPUT_INVALID.getKeyCode(),
                "Pokecube");
        ClientRegistry.registerKeyBinding(ClientSetupHandler.trainerEditKey);
    }

}
