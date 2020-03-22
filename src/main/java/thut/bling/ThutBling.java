package thut.bling;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import thut.bling.client.BlingRender;
import thut.bling.network.PacketBag;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;

@Mod(value = ThutBling.MODID)
public class ThutBling
{
    public static final String      MODID = "thut_bling";
    public static final CommonProxy PROXY = DistExecutor.runForDist(() -> () -> new ClientProxy(),
            () -> () -> new CommonProxy());

    public static class ClientProxy extends CommonProxy
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
        }

        @Override
        @OnlyIn(value = Dist.CLIENT)
        public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                final int brightness, final int overlay)
        {
            BlingRender.INSTANCE.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness,
                    overlay);
        }
    }

    public static class CommonProxy
    {
        public void finish(final FMLLoadCompleteEvent event)
        {
        }

        public boolean isClientSide()
        {
            return false;
        }

        public boolean isServerSide()
        {
            return true;
        }

        public void setup(final FMLCommonSetupEvent event)
        {
            ThutWearables.packets.registerMessage(PacketBag.class, PacketBag::new);
        }

        public void setupClient(final FMLClientSetupEvent event)
        {

        }

        public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                final int brightness, final int overlay)
        {
            // Nothing in common
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ThutBling.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            BlingItem.initDefaults(event.getRegistry());
        }
    }

    public ThutBling()
    {
    }

}
