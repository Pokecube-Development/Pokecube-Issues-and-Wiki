package thut.wearables;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import thut.core.common.ThutCore;
import thut.core.common.network.SyncAttachments;
import thut.lib.DistExecutor;
import thut.wearables.client.gui.GuiEvents;
import thut.wearables.client.gui.GuiWearables;
import thut.wearables.client.render.WearableEventHandler;
import thut.wearables.events.WearableDroppedEvent;
import thut.wearables.impl.WearableData;
import thut.wearables.inventory.ContainerWearables;
import thut.wearables.inventory.PlayerWearables;
import thut.wearables.network.MouseOverPacket;
import thut.wearables.network.PacketGui;
import thut.wearables.network.PacketHandler;

@Mod(ThutWearables.MODID)
public class ThutWearables
{
    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = ThutWearables.MODID, value = Dist.CLIENT)
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
            GuiEvents.init();
            ThutCore.FORGE_BUS.register(new WearableEventHandler());
        }

        @SubscribeEvent
        public static void setupMenus(final RegisterMenuScreensEvent event)
        {
            final MenuScreens.ScreenConstructor<ContainerWearables, GuiWearables> factory = (c, i,
                    t) -> new GuiWearables(c, i);
            event.register(ThutWearables.WEARABLES.get(), factory);
        }
    }

    public static class CommonProxy
    {
        public void finish(final FMLLoadCompleteEvent event)
        {}

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
            ThutWearables.packets.registerToClientMessage(MouseOverPacket.class);
            ThutWearables.packets.registerBiDirectionalMessage(PacketGui.class);
        }

        public void setupClient(final FMLClientSetupEvent event)
        {

        }
    }

    public static Map<ResourceLocation, Function<ItemStack, IActiveWearable>> REGISTRY = new HashMap<>();

    public static IActiveWearable getWearable(final ItemStack in)
    {
        var data = in.get(WEARABLE_DATA);
        if (data == null) return null;
        if (data.wearable() == null)
        {
            var wearable = REGISTRY.get(data.key()).apply(in);
            data = data.withWearable(wearable);
            in.set(WEARABLE_DATA, data);
        }
        return data.wearable();
    }

    public static final String MODID = Reference.MODID;

    public final static PacketHandler packets = new PacketHandler(Reference.NETVERSION);

    public final static CommonProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    // Holder for our config options
    public static final Config config = new Config();

    public static final DeferredRegister<MenuType<?>> CONTAINERS;
    public static final DeferredRegister<CreativeModeTab> TABS;
    public static final DeferredRegister<DataComponentType<?>> ITEM_DATA;
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS;

    public static final Supplier<MenuType<ContainerWearables>> WEARABLES;
    public static final Supplier<CreativeModeTab> WEARABLES_TAB;

    public static final Supplier<DataComponentType<WearableData>> WEARABLE_DATA;

    public static ItemStack WORNICON = ItemStack.EMPTY;

    static
    {
        CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, Reference.MODID);
        TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
        ITEM_DATA = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MODID);
        ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

        WEARABLES_TAB = TABS.register("wearables_tab", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.thutcore.wearables")).icon(() -> WORNICON).build());
        WEARABLES = CONTAINERS.register("wearables",
                () -> new MenuType<>((IContainerFactory<ContainerWearables>) ContainerWearables::new,
                        FeatureFlags.REGISTRY.allFlags()));

        DataComponentType.Builder<WearableData> builderbm = new DataComponentType.Builder<>();
        WEARABLE_DATA = ITEM_DATA.register("bling_model_data", name -> builderbm.persistent(WearableData.CODEC)
                .networkSynchronized(WearableData.STREAM_CODEC).build());
    }

    public static PlayerWearables getWearables(final LivingEntity wearer)
    {
        return PlayerWearables.get(wearer);
    }

    public ThutWearables(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register Config stuff
        thut.core.common.config.Config.setupConfigs(modContainer, ThutWearables.config, ThutWearables.MODID,
                ThutWearables.MODID);

        ThutCore.FORGE_BUS.register(this);
        // Register the setup method for modloading
        modEventBus.addListener(ThutWearables.proxy::setup);
        // Register the doClientStuff method for modloading
        modEventBus.addListener(ThutWearables.proxy::setupClient);
        // Register the doClientStuff method for modloading
        modEventBus.addListener(ThutWearables.proxy::finish);

        CONTAINERS.register(modEventBus);
        ITEM_DATA.register(modEventBus);
        ATTACHMENTS.register(modEventBus);
        TABS.register(modEventBus);

        PlayerWearables.registerAttachment(ATTACHMENTS);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void dropLoot(final LivingDropsEvent event)
    {
        final LivingEntity mob = event.getEntity();
        boolean overworldRules = true;
        final GameRules rules = overworldRules ? mob.getServer().getLevel(Level.OVERWORLD).getGameRules()
                : mob.level().getGameRules();
        final PlayerWearables cap = ThutWearables.getWearables(mob);
        if (rules.getBoolean(GameRules.RULE_KEEPINVENTORY) || cap == null) return;

        for (int i = 0; i < 13; i++)
        {
            final ItemStack stack = cap.getStackInSlot(i);
            if (!stack.isEmpty())
            {
                final WearableDroppedEvent dropEvent = new WearableDroppedEvent(event, stack, i);
                ThutCore.FORGE_BUS.post(dropEvent);
                if (dropEvent.isCanceled()) continue;
                EnumWearable.takeOff(mob, stack, i);
                final double d0 = mob.getY() - 0.3D + mob.getEyeHeight();
                final ItemEntity drop = new ItemEntity(mob.level(), mob.getX(), d0, mob.getZ(), stack);
                final float f = mob.getRandom().nextFloat() * 0.5F;
                final float f1 = mob.getRandom().nextFloat() * ((float) Math.PI * 2F);
                drop.setDeltaMovement(-Mth.sin(f1) * f, Mth.cos(f1) * f, 0.2);
                event.getDrops().add(drop);
                cap.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        SyncAttachments.syncChange(PlayerWearables.TYPE, mob);
    }

    @SubscribeEvent
    public void playerTick(final EntityTickEvent.Post event)
    {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity() instanceof Player wearer && event.getEntity().isAlive())
        {
            final PlayerWearables wearables = ThutWearables.getWearables(wearer);
            for (int i = 0; i < 13; i++) EnumWearable.tick(wearer, wearables.getStackInSlot(i), i);
        }
    }

    /**
     * Register the commands.
     *
     */
    @SubscribeEvent
    public void onCommandsRegister(final RegisterCommandsEvent event)
    {
        CommandGui.register(event.getDispatcher());
    }
}