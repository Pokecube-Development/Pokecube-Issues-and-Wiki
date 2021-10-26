package pokecube.adventures.init;

import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
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
import pokecube.adventures.client.render.StatueBlock;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.client.render.mobs.RenderNPC;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeAdv.MODID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    public static class EventHandler
    {
        @SubscribeEvent
        public static void onKey(final InputEvent.KeyInputEvent event)
        {
            if (ClientSetupHandler.trainerEditKey.consumeClick())
            {
                final HitResult pos = Minecraft.getInstance().hitResult;
                Entity target = null;
                switch (pos.getType())
                {
                case ENTITY:
                    target = ((EntityHitResult) pos).getEntity();
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
            final Player player = evt.getPlayer();
            final ItemStack stack = evt.getItemStack();
            if (stack.isEmpty()) return;
            final CompoundTag tag = stack.hasTag() ? stack.getTag() : new CompoundTag();
            if (tag.getBoolean("isapokebag")) evt.getToolTip().add(new TranslatableComponent(PokecubeAdv.MODID
                    + ".tooltip.bag"));
            if (tag.contains("dyeColour"))
            {
                final Component colour = new TranslatableComponent(DyeColor.byId(tag.getInt("dyeColour")).getName());
                boolean has = false;
                for (final Component s : evt.getToolTip())
                {
                    has = s.equals(colour);
                    if (has) break;
                }
                if (!has) evt.getToolTip().add(colour);
            }
            if (player == null || player.containerMenu == null) return;
            if (player.containerMenu instanceof PoweredContainer || Screen.hasShiftDown() && !ClonerHelper
                    .getGeneSelectors(stack).isEmpty())
            {
                final IMobGenetics genes = ClonerHelper.getGenes(stack);
                final int index = ClonerHelper.getIndex(stack);
                if (genes != null) for (final Alleles<?, ?> a : genes.getAlleles().values())
                {
                    TranslatableComponent comp = new TranslatableComponent(PokecubeAdv.MODID
                            + ".tooltip.gene.expressed." + a.getExpressed().getKey().getPath(), a.getExpressed());
                    evt.getToolTip().add(comp);
                    if (Config.instance.expandedDNATooltips || Screen.hasControlDown())
                    {
                        comp = new TranslatableComponent(PokecubeAdv.MODID + ".tooltip.gene.parent." + a.getExpressed()
                                .getKey().getPath(), a.getAllele(0), a.getAllele(1));
                        evt.getToolTip().add(comp);
                    }
                }
                if (genes != null && !(Config.instance.expandedDNATooltips || Screen.hasControlDown())) evt.getToolTip()
                        .add(new TranslatableComponent(PokecubeAdv.MODID + ".tooltip.gene.expand"));
                if (index != -1) evt.getToolTip().add(new TranslatableComponent(PokecubeAdv.MODID
                        + ".tooltip.gene.array.index", index));
                Set<Class<? extends Gene<?>>> genesSet;
                if (!(genesSet = ClonerHelper.getGeneSelectors(stack)).isEmpty()) if (Screen.hasControlDown())
                    for (final Class<? extends Gene<?>> geneC : genesSet)
                    try
                    {
                        final Gene<?> gene = geneC.getConstructor().newInstance();
                        evt.getToolTip().add(new TranslatableComponent(PokecubeAdv.MODID + ".tooltip.selector.gene."
                                + gene.getKey().getPath()));
                    }
                    catch (final Exception e)
                    {

                    }
                else evt.getToolTip().add(new TranslatableComponent(PokecubeAdv.MODID + ".tooltip.gene.expand"));
                if (RecipeSelector.isSelector(stack))
                {
                    final SelectorValue value = ClonerHelper.getSelectorValue(stack);
                    value.addToTooltip(evt.getToolTip());
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerRenderers(final RegisterRenderers event)
    {
        event.registerEntityRenderer(TrainerNpc.TYPE, RenderNPC::new);
        event.registerEntityRenderer(LeaderNpc.TYPE, RenderNPC::new);

        event.registerBlockEntityRenderer(PokecubeAdv.STATUE_TYPE.get(), StatueBlock::new);
    }

    public static KeyMapping trainerEditKey;

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);

        // Register container guis.
        MenuScreens.register(PokecubeAdv.CLONER_CONT.get(), Cloner::new);
        MenuScreens.register(PokecubeAdv.SPLICER_CONT.get(), Splicer::new);
        MenuScreens.register(PokecubeAdv.EXTRACTOR_CONT.get(), Extractor::new);
        MenuScreens.register(PokecubeAdv.AFA_CONT.get(), AFA::new);
        MenuScreens.register(PokecubeAdv.BAG_CONT.get(), Bag<BagContainer>::new);
        MenuScreens.register(PokecubeAdv.TRAINER_CONT.get(), Trainer::new);

        ItemBlockRenderTypes.setRenderLayer(PokecubeAdv.CLONER.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(PokecubeAdv.EXTRACTOR.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(PokecubeAdv.SPLICER.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(PokecubeAdv.LAB_GLASS.get(), RenderType.translucent());

        // Register config gui
        // ModList.get().getModContainerById(PokecubeAdv.MODID).ifPresent(c ->
        // c.registerExtensionPoint(
        // ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new
        // ConfigGui(PokecubeAdv.config, parent)));

        ClientSetupHandler.trainerEditKey = new KeyMapping("EditTrainer", InputConstants.UNKNOWN.getValue(),
                "Pokecube");
        ClientRegistry.registerKeyBinding(ClientSetupHandler.trainerEditKey);
    }

}
