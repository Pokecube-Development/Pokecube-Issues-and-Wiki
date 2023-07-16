package thut.bling.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thut.bling.BlingItem;
import thut.bling.ThutBling;
import thut.bling.bag.large.LargeContainer;
import thut.bling.client.gui.Bag;
import thut.bling.client.render.Util;
import thut.core.client.render.model.IModel;
import thut.wearables.EnumWearable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ThutBling.MODID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        MenuScreens.register(ThutBling.BIG_BAG.get(), Bag<LargeContainer>::new);
        MenuScreens.register(ThutBling.SMALL_BAG.get(), ContainerScreen::new);

        event.enqueueWork(() -> {
            for (Item i : BlingItem.bling)
            {
                ItemProperties.register(i, new ResourceLocation(ThutBling.MODID, "has_model"),
                        (stack, level, living, id) ->
                        {
                            IModel model = Util.getCustomModel(stack);
                            return model != null && model.isLoaded() && model.isValid() ? 1.0F : 0.0F;
                        });
            }
        });
    }

    @SubscribeEvent
    public static void colourItems(final ColorHandlerEvent.Item event)
    {
        for (Item i : BlingItem.bling)
        {
            event.getItemColors().register((stack, tintIndex) -> {
                if (!(stack.getItem() instanceof DyeableLeatherItem item)) return 0xFFFFFFFF;
                return tintIndex == 0 ? item.getColor(stack) : 0xFFFFFFFF;
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
