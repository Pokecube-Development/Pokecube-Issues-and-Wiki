package thut.bling.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import thut.bling.BlingItem;
import thut.bling.ThutBling;
import thut.bling.bag.large.LargeContainer;
import thut.bling.client.gui.LargeEnderBag;
import thut.bling.client.gui.SmallBag;
import thut.bling.client.render.Util;
import thut.core.client.render.model.IModel;
import thut.wearables.EnumWearable;

@EventBusSubscriber(modid = ThutBling.MODID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            for (Item i : BlingItem.bling)
            {
                ItemProperties.register(i, ResourceLocation.fromNamespaceAndPath(ThutBling.MODID, "has_model"),
                        (stack, level, living, id) ->
                        {
                            IModel model = Util.getCustomModel(stack);
                            return model != null && model.isLoaded() && model.isValid() ? 1.0F : 0.0F;
                        });
            }
        });
    }

    @SubscribeEvent
    public static void setupClient(final RegisterClientExtensionsEvent event)
    {
        event.registerItem(new IClientItemExtensions()
        {
            private final BlockEntityWithoutLevelRenderer renderer = BlingitemRenderer.INSTANCE;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                return this.renderer;
            }
        }, BlingItem.bling.toArray(new Item[0]));
    }

    @SubscribeEvent
    public static void setupMenus(final RegisterMenuScreensEvent event)
    {
        event.register(ThutBling.BIG_BAG.get(), LargeEnderBag<LargeContainer>::new);
        event.register(ThutBling.SMALL_BAG.get(), SmallBag<ChestMenu>::new);
    }

    @SubscribeEvent
    public static void colourItems(final RegisterColorHandlersEvent.Item event)
    {
        for (Item i : BlingItem.bling)
        {
            event.register((stack, tintIndex) -> {
                if (!(stack.is(ItemTags.DYEABLE))) return 0xFFFFFFFF;
                return tintIndex == 0 ? DyedItemColor.getOrDefault(stack, 0xFFFFFFFF) : 0xFFFFFFFF;
            }, i);
        }
    }

    public static void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
            final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
            final int brightness, final int overlay)
    {
        BlingRender.INSTANCE.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
    }

}
