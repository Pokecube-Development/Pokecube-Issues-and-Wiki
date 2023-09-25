package thut.bling;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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

    public static class RegistryEvents
    {
        public static void setup(final FMLCommonSetupEvent event)
        {
            ThutBling.packets.registerMessage(PacketBag.class, PacketBag::new);
        }
    }

    public final static PacketHandler packets = new PacketHandler(new ResourceLocation(ThutBling.MODID, "comms"),
            Reference.NETVERSION);

    public static Config config = new Config();

    public static final DeferredRegister<Item> ITEMS;
    public static final DeferredRegister<MenuType<?>> CONTAINERS;

    public static final RegistryObject<MenuType<LargeContainer>> BIG_BAG;
    public static final RegistryObject<MenuType<SmallContainer>> SMALL_BAG;

    static
    {
        CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ThutBling.MODID);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ThutBling.MODID);

        BIG_BAG = CONTAINERS.register("bling_bag_ender_large",
                () -> new MenuType<>((IContainerFactory<LargeContainer>) LargeContainer::new,
                        FeatureFlags.REGISTRY.allFlags()));
        SMALL_BAG = CONTAINERS.register("bling_bag",
                () -> new MenuType<>((IContainerFactory<SmallContainer>) SmallContainer::new,
                        FeatureFlags.REGISTRY.allFlags()));
    }

    public ThutBling()
    {
        thut.core.common.config.Config.setupConfigs(ThutBling.config, ThutWearables.MODID, ThutBling.MODID);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(RegistryEvents::setup);

        GemRecipe.RECIPE_SERIALIZERS.register(modEventBus);
        ITEMS.register(modEventBus);
        CONTAINERS.register(modEventBus);
        modEventBus.addListener(this::addCreative);

        BlingItem.init();
    }

    public void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey().equals(ThutWearables.WEARABLES_TAB.getKey()))
        {
            ThutWearables.WORNICON = BlingItem.getStack("bling_hat");

            event.accept(BlingItem.getStack("bling_bag"));
            event.accept(BlingItem.getStack("bling_bag_ender_vanilla"));
            event.accept(BlingItem.getStack("bling_bag_ender_large"));
            event.accept(BlingItem.getStack("bling_hat"));
            event.accept(BlingItem.getStack("bling_eye"));
            event.accept(BlingItem.getStack("bling_neck"));
            event.accept(BlingItem.getStack("bling_ear"));
            event.accept(BlingItem.getStack("bling_waist"));
            event.accept(BlingItem.getStack("bling_ring"));
            event.accept(BlingItem.getStack("bling_wrist"));
            event.accept(BlingItem.getStack("bling_ankle"));

            for (final String type : BlingItem.blingWearables.keySet())
                event.accept(BlingItem.blingWearables.get(type).get());
        }

    }
}
