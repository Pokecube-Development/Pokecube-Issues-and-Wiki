package pokecube.adventures.proxy;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.AfaContainer;
import pokecube.adventures.blocks.genetics.cloner.ClonerContainer;
import pokecube.adventures.blocks.genetics.extractor.ExtractorContainer;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.PoweredContainer;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.blocks.genetics.splicer.SplicerContainer;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.client.gui.blocks.AFA;
import pokecube.adventures.client.gui.blocks.Cloner;
import pokecube.adventures.client.gui.blocks.Extractor;
import pokecube.adventures.client.gui.blocks.Splicer;
import pokecube.adventures.client.gui.items.Bag;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.core.client.render.mobs.RenderNPC;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.bling.client.render.Back;
import thut.core.client.gui.ConfigGui;
import thut.core.client.render.x3d.X3dModel;
import thut.wearables.EnumWearable;

public class ClientProxy extends CommonProxy
{
    protected static class RenderWearable extends Wearable
    {   // One model for each layer.
        X3dModel bag;

        // One Texture for each layer.
        private final ResourceLocation BAG_1 = new ResourceLocation(PokecubeAdv.MODID, "textures/worn/bag_1.png");
        private final ResourceLocation BAG_2 = new ResourceLocation(PokecubeAdv.MODID, "textures/worn/bag_2.png");

        private final ResourceLocation[] BAG_TEXS = { this.BAG_1, this.BAG_2 };

        @Override
        public void renderWearable(final EnumWearable slot, final int index, final LivingEntity wearer,
                final ItemStack stack, final float partialTicks)
        {
            if (this.bag == null) this.bag = new X3dModel(new ResourceLocation(PokecubeAdv.MODID,
                    "models/worn/bag.x3d"));
            Back.renderBack(wearer, stack, this.bag, this.BAG_TEXS, wearer.getBrightnessForRender());
        }
    }

    private static Map<TypeTrainer, ResourceLocation> males   = Maps.newHashMap();
    private static Map<TypeTrainer, ResourceLocation> females = Maps.newHashMap();

    @Override
    public ResourceLocation getTrainerSkin(final LivingEntity mob, final TypeTrainer type, final byte gender)
    {
        ResourceLocation texture = null;
        boolean male;
        if (male = gender == 1) texture = ClientProxy.males.get(type);
        else texture = ClientProxy.females.get(type);
        if (texture == null)
        {
            texture = type.getTexture(mob);

            if (male) ClientProxy.males.put(type, texture);
            else ClientProxy.females.put(type, texture);
        }
        return texture;
    }

    @SubscribeEvent
    public void onToolTip(final ItemTooltipEvent evt)
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
        if (player.openContainer instanceof PoweredContainer || Screen.hasShiftDown() && !ClonerHelper.getGeneSelectors(
                stack).isEmpty())
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
                    comp = new TranslationTextComponent(PokecubeAdv.MODID + ".tooltip.gene.parent." + a.getExpressed()
                            .getKey().getPath(), a.getAlleles()[0], a.getAlleles()[1]);
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

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);

        RenderingRegistry.registerEntityRenderingHandler(TrainerNpc.class, (manager) -> new RenderNPC<>(manager));

        // Register container guis.
        ScreenManager.registerFactory(ClonerContainer.TYPE, Cloner::new);
        ScreenManager.registerFactory(SplicerContainer.TYPE, Splicer::new);
        ScreenManager.registerFactory(ExtractorContainer.TYPE, Extractor::new);
        ScreenManager.registerFactory(AfaContainer.TYPE, AFA::new);
        ScreenManager.registerFactory(BagContainer.TYPE, Bag<BagContainer>::new);

        // Register config gui
        ModList.get().getModContainerById(PokecubeAdv.MODID).ifPresent(c -> c.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new ConfigGui(PokecubeAdv.config, parent)));
    }

    @Override
    public Wearable getWearable()
    {
        return new RenderWearable();
    }
}
