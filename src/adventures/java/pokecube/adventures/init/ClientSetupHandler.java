package pokecube.adventures.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin.Model;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.PoweredContainer;
import pokecube.adventures.blocks.genetics.helper.SelectorImpl;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.statue.StatueEntity;
import pokecube.adventures.client.gui.blocks.AFA;
import pokecube.adventures.client.gui.blocks.Cloner;
import pokecube.adventures.client.gui.blocks.Extractor;
import pokecube.adventures.client.gui.blocks.Splicer;
import pokecube.adventures.client.gui.items.Bag;
import pokecube.adventures.client.gui.trainer.Trainer;
import pokecube.adventures.client.render.AFABlock;
import pokecube.adventures.client.render.StatueBlock;
import pokecube.adventures.client.render.layers.BeltLayerRender;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.client.render.mobs.RenderNPC;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = PokecubeAdv.MODID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    public static class EventHandler
    {
        @SubscribeEvent
        public static void onKey(final InputEvent.Key event)
        {
            if (ClientSetupHandler.trainerEditKey.consumeClick())
            {
                final HitResult pos = Minecraft.getInstance().hitResult;
                Entity target = null;
                if (pos.getType() == HitResult.Type.ENTITY)
                {
                    target = ((EntityHitResult) pos).getEntity();
                    if (target instanceof PartEntity<?> part) target = part.getParent();
                }
                PacketTrainer.requestEdit(target);
            }
        }

        @SubscribeEvent
        public static void onToolTip(final ItemTooltipEvent evt)
        {
            final Player player = evt.getEntity();
            if (player == null) return;
            final ItemStack stack = evt.getItemStack();
            var access = player.registryAccess();
            if (stack.isEmpty()) return;

            if (stack.getItem() == PokecubeAdv.STATUE.get().asItem())
            {
                var info = StatueEntity.unpackStatue(stack, Minecraft.getInstance().level);
                if (info != null && info.copy() != null)
                {
                    final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(info.copy().getCopiedID());
                    evt.getToolTip().add(1,
                            type.getDescription().copy().withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD));
                }
            }
            if (player == null || player.containerMenu == null) return;
            if (player.containerMenu instanceof PoweredContainer
                    || Screen.hasShiftDown() && !ClonerHelper.getGeneSelectors(access, stack).isEmpty())
            {
                final IMobGenetics genes = ClonerHelper.getGenes(access, stack);
                final int index = ClonerHelper.getIndex(stack);
                if (genes != null) for (final Alleles<?, ?> a : genes.getAlleles().values())
                {
                    MutableComponent comp = TComponent.translatable(
                            PokecubeAdv.MODID + ".tooltip.gene.expressed." + a.getExpressed().getKey().getPath(),
                            a.getExpressed());
                    evt.getToolTip().add(comp);
                    if (Config.instance.expandedDNATooltips || Screen.hasControlDown())
                    {
                        comp = TComponent.translatable(
                                PokecubeAdv.MODID + ".tooltip.gene.parent." + a.getExpressed().getKey().getPath(),
                                a.getAllele(0), a.getAllele(1));
                        evt.getToolTip().add(comp);
                    }
                }
                if (genes != null && !(Config.instance.expandedDNATooltips || Screen.hasControlDown()))
                    evt.getToolTip().add(TComponent.translatable(PokecubeAdv.MODID + ".tooltip.gene.expand"));
                if (index != -1) evt.getToolTip()
                        .add(TComponent.translatable(PokecubeAdv.MODID + ".tooltip.gene.array.index", index));
                Set<Class<? extends Gene<?>>> genesSet;
                if (!(genesSet = ClonerHelper.getGeneSelectors(access, stack)).isEmpty())
                    if (Screen.hasControlDown()) for (final Class<? extends Gene<?>> geneC : genesSet)
                        try
                        {
                            final Gene<?> gene = geneC.getConstructor().newInstance();
                            evt.getToolTip().add(TComponent.translatable(
                                    PokecubeAdv.MODID + ".tooltip.selector.gene." + gene.getKey().getPath()));
                        }
                        catch (final Exception e)
                        {

                        }
                    else evt.getToolTip().add(TComponent.translatable(PokecubeAdv.MODID + ".tooltip.gene.expand"));
                if (RecipeSelector.isSelector(access, stack))
                {
                    final SelectorImpl.SelectorValue value = ClonerHelper.getSelectorValue(stack);
                    value.addToTooltip(evt.getToolTip());
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event)
    {
        event.registerItem(new IClientItemExtensions()
        {
            private final BlockEntityWithoutLevelRenderer renderer = new pokecube.adventures.client.render.StatueItem();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                return this.renderer;
            }
        }, PokecubeAdv.STATUE.asItem());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SubscribeEvent
    public static void registerLayers(final EntityRenderersEvent.AddLayers event)
    {
        try
        {
            Field f = event.getClass().getDeclaredField("renderers");
            f.setAccessible(true);
            Map<EntityType<?>, EntityRenderer<?>> renderers = (Map<EntityType<?>, EntityRenderer<?>>) f.get(event);
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE.stream().toList())
            {
                EntityRenderer<?> render = renderers.get(type);
                if (render instanceof LivingEntityRenderer livingRender
                        && livingRender.getModel() instanceof HumanoidModel)
                {
                    livingRender.addLayer(new BeltLayerRender(livingRender));
                }
            }

            EntityRenderer<? extends Player> renderer = event.getSkin(Model.SLIM);
            if (renderer instanceof LivingEntityRenderer livingRenderer)
            {
                livingRenderer.addLayer(new BeltLayerRender<>(livingRenderer));
            }

            renderer = event.getSkin(Model.WIDE);
            if (renderer instanceof LivingEntityRenderer livingRenderer)
            {
                livingRenderer.addLayer(new BeltLayerRender<>(livingRenderer));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @SubscribeEvent
    public static void registerRenderers(final RegisterRenderers event)
    {
        event.registerEntityRenderer(EntityTypes.getTrainer(), RenderNPC::new);
        event.registerEntityRenderer(EntityTypes.getLeader(), RenderNPC::new);

        event.registerBlockEntityRenderer(PokecubeAdv.STATUE_TYPE.get(), StatueBlock::new);
        event.registerBlockEntityRenderer(PokecubeAdv.AFA_TYPE.get(), AFABlock::new);
    }

    public static KeyMapping trainerEditKey;

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        ThutCore.FORGE_BUS.register(EventHandler.class);

    }

    @SubscribeEvent
    public static void setupMenus(final RegisterMenuScreensEvent event)
    {
        // Register container guis.
        event.register(PokecubeAdv.CLONER_CONT.get(), Cloner::new);
        event.register(PokecubeAdv.SPLICER_CONT.get(), Splicer::new);
        event.register(PokecubeAdv.EXTRACTOR_CONT.get(), Extractor::new);
        event.register(PokecubeAdv.AFA_CONT.get(), AFA::new);
        event.register(PokecubeAdv.BAG_CONT.get(), Bag<BagContainer>::new);
        event.register(PokecubeAdv.TRAINER_CONT.get(), Trainer::new);
    }

    @SubscribeEvent
    public static void registetKeys(final RegisterKeyMappingsEvent event)
    {
        ClientSetupHandler.trainerEditKey = new KeyMapping("key.trainer.edit", InputConstants.UNKNOWN.getValue(),
                "key.categories.pokecube");
        event.register(ClientSetupHandler.trainerEditKey);
    }

    @SubscribeEvent
    public static void colourItems(final RegisterColorHandlersEvent.Item event)
    {
        event.register((stack, tintIndex) -> {
            if (!(stack.has(DataComponents.DYED_COLOR))) return 0xFFFFFFFF;
            return tintIndex == 0 ? stack.get(DataComponents.DYED_COLOR).rgb() | 0xFF000000 : 0xFFF8F855;
        }, PokecubeAdv.BAG.get());
    }
}
