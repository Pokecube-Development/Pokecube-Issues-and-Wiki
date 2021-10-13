package thut.bling;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import thut.bling.bag.large.LargeContainer;
import thut.bling.bag.small.SmallContainer;
import thut.bling.network.PacketBag;
import thut.wearables.Reference;
import thut.wearables.ThutWearables;
import thut.wearables.network.PacketHandler;

@Mod(value = ThutBling.MODID)
public class ThutBling
{
    public static final String MODID = "thut_bling";

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ThutBling.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            BlingItem.initDefaults(event.getRegistry());
        }

        @SubscribeEvent
        public static void registerContainers(final RegistryEvent.Register<MenuType<?>> event)
        {
            // Register Containers
            event.getRegistry().register(LargeContainer.TYPE.setRegistryName(ThutBling.MODID, "bling_bag_ender_large"));
            event.getRegistry().register(SmallContainer.TYPE.setRegistryName(ThutBling.MODID, "bling_bag"));
        }

        public static void setup(final FMLCommonSetupEvent event)
        {
            ThutBling.packets.registerMessage(PacketBag.class, PacketBag::new);
        }
    }

    public final static PacketHandler packets = new PacketHandler(new ResourceLocation(ThutBling.MODID, "comms"),
            Reference.NETVERSION);

    public static Config config = new Config();

    public ThutBling()
    {
        thut.core.common.config.Config.setupConfigs(ThutBling.config, ThutWearables.MODID, ThutBling.MODID);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(RegistryEvents::setup);
        GemRecipe.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
