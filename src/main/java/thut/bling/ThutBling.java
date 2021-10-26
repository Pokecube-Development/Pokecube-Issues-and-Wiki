package thut.bling;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import thut.bling.bag.large.LargeContainer;
import thut.bling.bag.small.SmallContainer;
import thut.bling.client.BlingRender;
import thut.bling.client.gui.Bag;
import thut.bling.network.PacketBag;
import thut.wearables.EnumWearable;
import thut.wearables.Reference;
import thut.wearables.ThutWearables;
import thut.wearables.network.PacketHandler;

@Mod(value = ThutBling.MODID)
public class ThutBling
{
    public static final String      MODID = "thut_bling";
    public static final CommonProxy PROXY = DistExecutor.safeRunForDist(
            () -> ClientProxy::new, () -> CommonProxy::new);

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
            ScreenManager.register(LargeContainer.TYPE, Bag<LargeContainer>::new);
            ScreenManager.register(SmallContainer.TYPE, ChestScreen::new);
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
            ThutBling.packets.registerMessage(PacketBag.class, PacketBag::new);
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

        @SubscribeEvent
        public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
        {
            // Register Containers
            event.getRegistry().register(LargeContainer.TYPE.setRegistryName(ThutBling.MODID, "bling_bag_ender_large"));
            event.getRegistry().register(SmallContainer.TYPE.setRegistryName(ThutBling.MODID, "bling_bag"));
        }
    }

    public final static PacketHandler packets = new PacketHandler(new ResourceLocation(ThutBling.MODID, "comms"),
            Reference.NETVERSION);

    public static Config config = new Config();

    public ThutBling()
    {
        thut.core.common.config.Config.setupConfigs(ThutBling.config, ThutWearables.MODID, ThutBling.MODID);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ThutBling.PROXY::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ThutBling.PROXY::setupClient);
        // Register the loaded method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ThutBling.PROXY::finish);
        GemRecipe.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
