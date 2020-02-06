package thut.core.common;

import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import thut.api.LinkableCaps;
import thut.api.OwnableCaps;
import thut.api.boom.ExplosionCustom;
import thut.api.entity.IMobColourable;
import thut.api.entity.IMobTexturable;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.ShearableCaps;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Vector3;
import thut.api.terrain.CapabilityTerrain;
import thut.api.terrain.ITerrainProvider;
import thut.api.terrain.TerrainManager;
import thut.api.world.mobs.data.DataSync;
import thut.core.client.ClientProxy;
import thut.core.client.render.animation.CapabilityAnimation;
import thut.core.client.render.particle.ThutParticles;
import thut.core.common.config.Config;
import thut.core.common.genetics.DefaultGeneStorage;
import thut.core.common.genetics.DefaultGenetics;
import thut.core.common.handlers.ConfigHandler;
import thut.core.common.mobs.DefaultColourable;
import thut.core.common.mobs.DefaultColourableStorage;
import thut.core.common.network.EntityUpdate;
import thut.core.common.network.PacketHandler;
import thut.core.common.network.TerrainUpdate;
import thut.core.common.network.TileUpdate;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.core.common.world.mobs.data.PacketDataSync;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ThutCore.MODID)
public class ThutCore
{
    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the main event bus, as it gets
    // generic minecraft events.)
    public static class MobEvents
    {
        @SubscribeEvent
        public static void interact(final RightClickBlock event)
        {
            // Probably a block entity to interact with here.
            if (event.getWorld().isAirBlock(event.getPos()))
            {
                final PlayerEntity player = event.getPlayer();
                final Vec3d face = event.getPlayer().getEyePosition(0);
                final Vec3d look = event.getPlayer().getLookVec();
                final AxisAlignedBB box = event.getPlayer().getBoundingBox().grow(3, 3, 3);
                final EntityRayTraceResult var = ProjectileHelper.func_221273_a(player, face, look, box,
                        e -> e instanceof IBlockEntity, 3);
                if (var != null && var.getType() == EntityRayTraceResult.Type.ENTITY)
                {
                    final IBlockEntity entity = (IBlockEntity) var.getEntity();
                    if (entity.getInteractor().processInitialInteract(event.getPlayer(), event.getItemStack(), event
                            .getHand()))
                    {
                        event.setCanceled(true);
                        return;
                    }
                    if (entity.getInteractor().interactInternal(event.getPlayer(), event.getPos(), event.getItemStack(),
                            event.getHand()) != ActionResultType.PASS)
                    {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
            if (event.getSide() == LogicalSide.CLIENT) return;

            switch (event.getHand())
            {
            case MAIN_HAND:
                if (!event.getItemStack().isEmpty()) return;

                final Entity mob = event.getEntity();
                final ServerWorld world = (ServerWorld) event.getEntity().getEntityWorld();
                final Vector3 center = Vector3.getNewVector().set(mob);
                final float power = 100;
                final ExplosionCustom boom = new ExplosionCustom(world, mob, center, power);
                boom.maxPerTick[0] = 1000;
                boom.maxPerTick[1] = 5000;
                // boom.doExplosion();
                break;
            case OFF_HAND:
                break;
            default:
                break;

            }
        }
    }

    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ThutCore.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerParticles(final RegistryEvent.Register<ParticleType<?>> event)
        {
            ThutCore.LOGGER.debug("Registering Particle Types");
            event.getRegistry().register(ThutParticles.AURORA.setRegistryName(ThutCore.MODID, "aurora"));
            event.getRegistry().register(ThutParticles.LEAF.setRegistryName(ThutCore.MODID, "leaf"));
            event.getRegistry().register(ThutParticles.MISC.setRegistryName(ThutCore.MODID, "misc"));
            event.getRegistry().register(ThutParticles.STRING.setRegistryName(ThutCore.MODID, "string"));
            event.getRegistry().register(ThutParticles.POWDER.setRegistryName(ThutCore.MODID, "powder"));
        }
    }

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID  = "thutcore";

    private static final String NETVERSION = "1.0.0";

    public static final PacketHandler packets = new PacketHandler(new ResourceLocation(ThutCore.MODID, "comms"),
            ThutCore.NETVERSION);

    public static ThutCore instance;

    public static final Proxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(),
            () -> () -> new CommonProxy());

    public static final ConfigHandler conf = new ConfigHandler();

    public static ItemStack THUTICON = ItemStack.EMPTY;

    public static final ItemGroup THUTITEMS = new ItemGroup("thut")
    {
        @Override
        public ItemStack createIcon()
        {
            return ThutCore.THUTICON;
        }
    };

    public static String trim(String name)
    {
        // ROOT locale to prevent issues with turkish letters.
        name = name.toLowerCase(Locale.ROOT).trim();
        // Replace all not-resourcelocation chars
        name = name.replaceAll("([^a-zA-Z0-9 _-])", "");
        // Replace these too.
        name = name.replaceAll(" ", "_");
        return name;
    }

    public ThutCore()
    {
        ThutCore.instance = this;

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested
        // in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the mob serializers
        // for seats
        DataSerializers.registerSerializer(IMultiplePassengerEntity.SEATSERIALIZER);
        // for Vec3ds
        DataSerializers.registerSerializer(BlockEntityBase.VEC3DSER);

        // Register Config stuff
        Config.setupConfigs(ThutCore.conf, ThutCore.MODID, ThutCore.MODID);
    }

    private void doClientStuff(final FMLClientSetupEvent event)
    {
        ThutCore.proxy.setupClient(event);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("thutcore", "helloworld", () ->
        {
            ThutCore.LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerAboutToStart(final FMLServerAboutToStartEvent event)
    {
        // do something when the server starts
        ThutCore.LOGGER.debug("Clearing terrain cache");
        ITerrainProvider.pendingCache.clear();
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other
        // mods
        ThutCore.LOGGER.info("Got IMC {}", event.getIMCStream().map(m -> m.getMessageSupplier().get()).collect(
                Collectors.toList()));
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        ThutCore.LOGGER.info("Setup");

        if (ThutCore.THUTICON.isEmpty()) ThutCore.THUTICON = new ItemStack(Blocks.STONE);

        // Register the actual packets
        ThutCore.packets.registerMessage(EntityUpdate.class, EntityUpdate::new);
        ThutCore.packets.registerMessage(TileUpdate.class, TileUpdate::new);
        ThutCore.packets.registerMessage(TerrainUpdate.class, TerrainUpdate::new);
        ThutCore.packets.registerMessage(PacketDataSync.class, PacketDataSync::new);

        // Register capabilities.

        // Register genetics
        CapabilityManager.INSTANCE.register(IMobGenetics.class, new DefaultGeneStorage(), DefaultGenetics::new);
        // Register colourable
        CapabilityManager.INSTANCE.register(IMobColourable.class, new DefaultColourableStorage(),
                DefaultColourable::new);
        // Register texturable
        CapabilityManager.INSTANCE.register(IMobTexturable.class, new IMobTexturable.Storage(),
                IMobTexturable.Defaults::new);

        CapabilityAnimation.setup();
        OwnableCaps.setup();
        LinkableCaps.setup();
        ShearableCaps.setup();

        // Register terrain capabilies
        CapabilityManager.INSTANCE.register(CapabilityTerrain.ITerrainProvider.class, new CapabilityTerrain.Storage(),
                CapabilityTerrain.DefaultProvider::new);
        // Datasync capability
        CapabilityManager.INSTANCE.register(DataSync.class, new Capability.IStorage<DataSync>()
        {
            @Override
            public void readNBT(final Capability<DataSync> capability, final DataSync instance, final Direction side,
                    final INBT nbt)
            {
            }

            @Override
            public INBT writeNBT(final Capability<DataSync> capability, final DataSync instance, final Direction side)
            {
                return null;
            }
        }, DataSync_Impl::new);

        // Initialize terrain manager.
        TerrainManager.getInstance();

        ThutCore.proxy.setup(event);
    }
}
