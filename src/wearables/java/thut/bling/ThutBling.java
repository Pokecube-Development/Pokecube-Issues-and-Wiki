package thut.bling;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.bling.bag.large.LargeContainer;
import thut.bling.bag.small.SmallContainer;
import thut.bling.data.GemData;
import thut.bling.data.ModelData;
import thut.bling.data.SmallBagData;
import thut.bling.network.PacketBag;
import thut.wearables.Reference;
import thut.wearables.ThutWearables;
import thut.wearables.network.PacketHandler;

import java.util.function.Supplier;

@Mod(value = ThutBling.MODID)
public class ThutBling
{
    public static final String MODID = "thut_bling";

    public static class RegistryEvents
    {
        public static void setup(final FMLCommonSetupEvent event)
        {
            ThutBling.packets.registerBiDirectionalMessage(PacketBag.class);
        }
    }

    public final static PacketHandler packets = new PacketHandler(Reference.NETVERSION);

    public static Config config = new Config();

    public static final DeferredRegister.Items ITEMS;
    public static final DeferredRegister<MenuType<?>> CONTAINERS;
    public static final DeferredRegister<DataComponentType<?>> ITEM_DATA;

    public static final Supplier<MenuType<LargeContainer>> BIG_BAG;
    public static final Supplier<MenuType<SmallContainer>> SMALL_BAG;

    public static final Supplier<DataComponentType<SmallBagData>> SMALL_BAG_DATA;
    public static final Supplier<DataComponentType<GemData>> BLING_GEM_DATA;
    public static final Supplier<DataComponentType<ModelData>> BLING_MODEL_DATA;

    static
    {
        CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, ThutBling.MODID);
        ITEMS = DeferredRegister.createItems(ThutBling.MODID);
        ITEM_DATA = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MODID);

        BIG_BAG = CONTAINERS.register("bling_bag_ender_large",
                () -> new MenuType<>((IContainerFactory<LargeContainer>) LargeContainer::new,
                        FeatureFlags.REGISTRY.allFlags()));
        SMALL_BAG = CONTAINERS.register("bling_bag",
                () -> new MenuType<>((IContainerFactory<SmallContainer>) SmallContainer::new,
                        FeatureFlags.REGISTRY.allFlags()));

        DataComponentType.Builder<SmallBagData> buildersb = new DataComponentType.Builder<>();
        SMALL_BAG_DATA = ITEM_DATA.register("small_bag_data",
                name -> buildersb.persistent(SmallBagData.CODEC).networkSynchronized(SmallBagData.STREAM_CODEC)
                        .build());
        DataComponentType.Builder<GemData> builderbg = new DataComponentType.Builder<>();
        BLING_GEM_DATA = ITEM_DATA.register("bling_gem_data",
                name -> builderbg.persistent(GemData.CODEC).networkSynchronized(GemData.STREAM_CODEC).build());
        DataComponentType.Builder<ModelData> builderbm = new DataComponentType.Builder<>();
        BLING_MODEL_DATA = ITEM_DATA.register("bling_model_data",
                name -> builderbm.persistent(ModelData.CODEC).networkSynchronized(ModelData.STREAM_CODEC).build());
    }

    public ThutBling(IEventBus modEventBus, ModContainer modContainer)
    {
        thut.core.common.config.Config.setupConfigs(modContainer, ThutBling.config, ThutWearables.MODID,
                ThutBling.MODID);
        modEventBus.addListener(RegistryEvents::setup);

        GemRecipe.RECIPE_SERIALIZERS.register(modEventBus);
        ITEMS.register(modEventBus);
        CONTAINERS.register(modEventBus);
        ITEM_DATA.register(modEventBus);
        modEventBus.addListener(EventPriority.HIGHEST, this::addCreative);
        modEventBus.addListener(this::modifyComponents);

        BlingItem.init();
    }

    public void modifyComponents(ModifyDefaultComponentsEvent event)
    {
        event.getAllItems().forEach(item -> {
            if (item instanceof BlingItem bling)
            {
                event.modify(bling,
                        builder -> builder.set(DataComponents.DYED_COLOR, BlingItem._getDefault(bling.slot)));
            }
        });
    }

    public void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTab().equals(ThutWearables.WEARABLES_TAB.get()))
        {
            ThutWearables.WORNICON = BlingItem.getStack("bling_hat");

            for (final String type : BlingItem.blingWearables.keySet())
                event.accept(BlingItem.blingWearables.get(type).get());
        }
    }
}
