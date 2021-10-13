package thut.bling.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import thut.bling.ThutBling.CommonProxy;
import thut.bling.bag.large.LargeContainer;
import thut.bling.bag.small.SmallContainer;
import thut.bling.client.gui.Bag;
import thut.wearables.EnumWearable;

public class ClientSetupHandler extends CommonProxy
{

    @Override
    public boolean isClientSide()
    {
        return EffectiveSide.get() == LogicalSide.CLIENT;
    }

    @Override
    public boolean isServerSide()
    {
        return EffectiveSide.get() == LogicalSide.SERVER;
    }

    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
        super.setup(event);
    }

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        MenuScreens.register(LargeContainer.TYPE, Bag<LargeContainer>::new);
        MenuScreens.register(SmallContainer.TYPE, ContainerScreen::new);
    }

    public static void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
            final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
            final int brightness, final int overlay)
    {
        BlingRender.INSTANCE.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
    }

}
