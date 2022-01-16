package thut.wearables;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import thut.wearables.client.gui.GuiEvents;
import thut.wearables.client.gui.GuiWearables;
import thut.wearables.client.render.WearableEventHandler;
import thut.wearables.events.WearableDroppedEvent;
import thut.wearables.impl.ConfigWearable;
import thut.wearables.inventory.ContainerWearables;
import thut.wearables.inventory.IWearableInventory;
import thut.wearables.inventory.PlayerWearables;
import thut.wearables.inventory.WearableHandler;
import thut.wearables.network.MouseOverPacket;
import thut.wearables.network.PacketGui;
import thut.wearables.network.PacketHandler;
import thut.wearables.network.PacketSyncWearables;

@Mod(ThutWearables.MODID)
public class ThutWearables
{
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
            MinecraftForge.EVENT_BUS.register(new WearableEventHandler());
        }

        @Override
        public void setupClient(final FMLClientSetupEvent event)
        {
            final MenuScreens.ScreenConstructor<ContainerWearables, GuiWearables> factory = (c, i,
                    t) -> new GuiWearables(c, i);
            MenuScreens.register(ContainerWearables.TYPE, factory);
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
            ThutWearables.packets.registerMessage(PacketSyncWearables.class, PacketSyncWearables::new);
            ThutWearables.packets.registerMessage(MouseOverPacket.class, MouseOverPacket::new);
            ThutWearables.packets.registerMessage(PacketGui.class, PacketGui::new);
        }

        public void setupClient(final FMLClientSetupEvent event)
        {

        }
    }

    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ThutWearables.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerCapabilities(final RegisterCapabilitiesEvent event)
        {
            event.register(IActiveWearable.class);
            event.register(IWearableInventory.class);
        }

        @SubscribeEvent
        public static void registerContainers(final RegistryEvent.Register<MenuType<?>> event)
        {
            event.getRegistry().register(ContainerWearables.TYPE.setRegistryName(ThutWearables.MODID, "wearables"));
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void textureStitch(final TextureStitchEvent.Pre event)
        {
            if (!event.getAtlas().location().toString().equals("minecraft:textures/atlas/blocks.png")) return;
            for (int i = 0; i < EnumWearable.BYINDEX.length; i++)
                event.addSprite(new ResourceLocation(EnumWearable.getIcon(i)));
        }
    }

    public static final Capability<IActiveWearable>    WEARABLE_CAP  = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IWearableInventory> WEARABLES_CAP = CapabilityManager.get(new CapabilityToken<>(){});

    public static final ResourceLocation WEARABLES_ITEM_TAG = new ResourceLocation(Reference.MODID, "wearable");

    public static final String MODID = Reference.MODID;

    public final static PacketHandler packets = new PacketHandler(new ResourceLocation(Reference.MODID, "comms"),
            Reference.NETVERSION);

    public final static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    // Holder for our config options
    public static final Config config = new Config();

    public static PlayerWearables getWearables(final LivingEntity wearer)
    {
        final PlayerWearables wearables = null;
        final IWearableInventory inven = wearer.getCapability(WearableHandler.WEARABLES_CAP).orElse(wearables);
        if (inven instanceof PlayerWearables)
        {
            final PlayerWearables ret = (PlayerWearables) inven;
            return ret;
        }
        return wearables;
    }

    public static void syncWearables(final LivingEntity player)
    {
        if (ThutWearables.proxy.isClientSide())
        {
            Thread.dumpStack();
            return;
        }
        final PacketSyncWearables packet = new PacketSyncWearables(player);
        ThutWearables.packets.sendToTracking(packet, player);
        if (player instanceof ServerPlayer) ThutWearables.packets.sendTo(packet, (ServerPlayer) player);
    }

    private final boolean overworldRules = true;

    Map<ResourceLocation, EnumWearable> configWearables = Maps.newHashMap();

    /** Cache of wearables for players that die when keep inventory is on. */
    Map<UUID, PlayerWearables> player_inventory_cache = Maps.newHashMap();

    Set<UUID> toKeep = Sets.newHashSet();

    public ThutWearables()
    {
        // Register Config stuff
        thut.core.common.config.Config.setupConfigs(ThutWearables.config, ThutWearables.MODID, ThutWearables.MODID);

        MinecraftForge.EVENT_BUS.register(this);

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ThutWearables.proxy::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ThutWearables.proxy::setupClient);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ThutWearables.proxy::finish);
        RecipeDye.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void dropLoot(final LivingDropsEvent event)
    {
        final LivingEntity mob = event.getEntityLiving();
        final GameRules rules = this.overworldRules ? mob.getServer().getLevel(Level.OVERWORLD).getGameRules()
                : mob.getLevel().getGameRules();
        final PlayerWearables cap = ThutWearables.getWearables(mob);
        if (rules.getBoolean(GameRules.RULE_KEEPINVENTORY) || cap == null) return;

        for (int i = 0; i < 13; i++)
        {
            final ItemStack stack = cap.getStackInSlot(i);
            if (!stack.isEmpty())
            {
                EnumWearable.takeOff(mob, stack, i);
                final WearableDroppedEvent dropEvent = new WearableDroppedEvent(event, stack, i);
                if (MinecraftForge.EVENT_BUS.post(dropEvent)) continue;
                final double d0 = mob.getY() - 0.3D + mob.getEyeHeight();
                final ItemEntity drop = new ItemEntity(mob.getLevel(), mob.getX(), d0, mob.getZ(), stack);
                final float f = mob.getRandom().nextFloat() * 0.5F;
                final float f1 = mob.getRandom().nextFloat() * ((float) Math.PI * 2F);
                drop.setDeltaMovement(-Mth.sin(f1) * f, Mth.cos(f1) * f, 0.2);
                event.getDrops().add(drop);
                cap.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        ThutWearables.syncWearables(mob);
    }

    /**
     * Syncs wearables to the player when they join a world. This fixes client
     * issues when they use nether portals, etc
     *
     * @param event
     */
    @SubscribeEvent
    public void joinWorld(final EntityJoinWorldEvent event)
    {
        if (event.getWorld().isClientSide) return;
        if (event.getEntity() instanceof ServerPlayer) ThutWearables.packets.sendTo(new PacketSyncWearables(
                (LivingEntity) event.getEntity()), (ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LivingEntity) event.addCapability(new ResourceLocation(ThutWearables.MODID,
                "wearables"), new PlayerWearables());
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(final AttachCapabilitiesEvent<ItemStack> event)
    {
        final ResourceLocation loc = event.getObject().getItem().getRegistryName();
        if (this.configWearables.containsKey(loc))
        {
            final EnumWearable slot = this.configWearables.get(loc);
            event.addCapability(new ResourceLocation(ThutWearables.MODID, "configwearable"), new ConfigWearable(slot));
        }
    }

    @SubscribeEvent
    public void PlayerLoggedOutEvent(final PlayerLoggedOutEvent event)
    {
        this.player_inventory_cache.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public void playerTick(final LivingUpdateEvent event)
    {
        if (event.getEntity().getLevel().isClientSide) return;
        if (event.getEntity() instanceof Player && event.getEntity().isAlive())
        {
            final Player wearer = (Player) event.getEntity();
            final PlayerWearables wearables = ThutWearables.getWearables(wearer);
            for (int i = 0; i < 13; i++)
                EnumWearable.tick(wearer, wearables.getStackInSlot(i), i);
            if (wearer instanceof ServerPlayer) this.player_inventory_cache.put(wearer.getUUID(), wearables);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void preDrop(final LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        final Player player = (Player) event.getEntity();
        final GameRules rules = this.overworldRules ? player.getServer().getLevel(Level.OVERWORLD).getGameRules()
                : player.getLevel().getGameRules();
        if (rules.getBoolean(GameRules.RULE_KEEPINVENTORY))
        {
            final PlayerWearables cap = ThutWearables.getWearables(player);
            this.player_inventory_cache.put(player.getUUID(), cap);
            this.toKeep.add(player.getUUID());
            return;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void respawn(final PlayerRespawnEvent event)
    {
        final Player wearer = event.getPlayer();
        if (wearer instanceof ServerPlayer && (this.toKeep.contains(wearer.getUUID()) || event
                .isEndConquered()) && this.player_inventory_cache.containsKey(wearer.getUUID()))
        {
            final CompoundTag tag = this.player_inventory_cache.get(wearer.getUUID()).serializeNBT();
            final PlayerWearables wearables = ThutWearables.getWearables(wearer);
            wearables.deserializeNBT(tag);
            this.toKeep.remove(wearer.getUUID());
            ThutWearables.syncWearables(wearer);
        }
    }

    @SubscribeEvent
    /**
     * Register the commands.
     *
     * @param event
     */
    public void onCommandsRegister(final RegisterCommandsEvent event)
    {
        CommandGui.register(event.getDispatcher());
    }

    /**
     * Syncs wearables of other mobs to player when they start tracking them.
     *
     * @param event
     */
    @SubscribeEvent
    public void startTracking(final StartTracking event)
    {
        if (event.getTarget() instanceof LivingEntity && ThutWearables.getWearables((LivingEntity) event
                .getTarget()) != null && event.getPlayer().isEffectiveAi()) ThutWearables.packets.sendTo(
                        new PacketSyncWearables((LivingEntity) event.getTarget()), (ServerPlayer) event
                                .getPlayer());
    }
}