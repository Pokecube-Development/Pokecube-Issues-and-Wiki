package thut.core.common;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import com.google.common.collect.Maps;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
// The value here should match an entry in the META-INF/mods.toml file
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import thut.api.AnimatedCaps;
import thut.api.LinkableCaps;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.entity.BreedableCaps;
import thut.api.entity.CopyCaps;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.ShearableCaps;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.BlockEntityInventory;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.particle.ThutParticles;
import thut.api.terrain.StructureManager;
import thut.core.common.config.Config;
import thut.core.common.handlers.ConfigHandler;
import thut.core.common.network.CapabilitySync;
import thut.core.common.network.EntityUpdate;
import thut.core.common.network.GeneralUpdate;
import thut.core.common.network.PacketHandler;
import thut.core.common.network.TerrainUpdate;
import thut.core.common.network.TileUpdate;
import thut.core.common.terrain.CapabilityTerrainAffected;
import thut.core.common.world.mobs.data.PacketDataSync;
import thut.crafts.ThutCrafts;

@Mod(ThutCore.MODID)
public class ThutCore
{
    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the main event bus, as it gets
    // generic minecraft events.)
    public static class MobEvents
    {
        private static final ResourceLocation CAPID = new ResourceLocation(ThutCore.MODID, "inventory");

        @SubscribeEvent
        public static void onMobCapabilityAttach(final AttachCapabilitiesEvent<Entity> event)
        {
            if (event.getCapabilities().containsKey(MobEvents.CAPID)) return;
            if (!(event.getObject() instanceof IBlockEntity)) return;
            event.addCapability(MobEvents.CAPID, new BlockEntityInventory((IBlockEntity) event.getObject()));
        }

        public static EntityHitResult rayTraceEntities(final Entity shooter, final Vec3 startVec, final Vec3 endVec,
                final AABB boundingBox, final Predicate<Entity> filter, final double distance)
        {
            final Level world = shooter.level;
            double d0 = distance;
            Entity entity = null;
            Vec3 vector3d = null;

            for (final Entity entity1 : world.getEntities(shooter, boundingBox, filter))
            {
                final AABB axisalignedbb = entity1.getBoundingBox().inflate(entity1.getPickRadius());
                final Optional<Vec3> optional = axisalignedbb.clip(startVec, endVec);
                if (axisalignedbb.contains(startVec))
                {
                    if (d0 >= 0.0D)
                    {
                        entity = entity1;
                        vector3d = optional.orElse(startVec);
                        d0 = 0.0D;
                    }
                }
                else if (optional.isPresent())
                {
                    final Vec3 vector3d1 = optional.get();
                    final double d1 = startVec.distanceToSqr(vector3d1);
                    if (d1 < d0 || d0 == 0.0D) if (entity1.getRootVehicle() == shooter.getRootVehicle() && !entity1
                            .canRiderInteract())
                    {
                        if (d0 == 0.0D)
                        {
                            entity = entity1;
                            vector3d = vector3d1;
                        }
                    }
                    else
                    {
                        entity = entity1;
                        vector3d = vector3d1;
                        d0 = d1;
                    }
                }
            }
            return entity == null ? null : new EntityHitResult(entity, vector3d);
        }

        @SubscribeEvent
        public static void interact(final RightClickBlock event)
        {
            // Probably a block entity to interact with here.
            if (event.getWorld().isEmptyBlock(event.getPos()))
            {
                final Player player = event.getPlayer();
                final Vec3 face = event.getPlayer().getEyePosition(0);
                final Vec3 look = event.getPlayer().getLookAngle();
                final AABB box = event.getPlayer().getBoundingBox().inflate(3, 3, 3);
                final EntityHitResult var = MobEvents.rayTraceEntities(player, face, look, box,
                        e -> e instanceof IBlockEntity, 3);
                if (var != null && var.getType() == HitResult.Type.ENTITY)
                {
                    final IBlockEntity entity = (IBlockEntity) var.getEntity();
                    if (entity.getInteractor().processInitialInteract(event.getPlayer(), event.getItemStack(), event
                            .getHand()) != InteractionResult.PASS)
                    {
                        event.setCanceled(true);
                        return;
                    }
                    if (entity.getInteractor().interactInternal(event.getPlayer(), event.getPos(), event.getItemStack(),
                            event.getHand()) != InteractionResult.PASS)
                    {
                        event.setCanceled(true);
                        return;
                    }
                }
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
        public static void registerCapabilities(final RegisterCapabilitiesEvent event)
        {
            ThutCaps.registerCapabilities(event);
        }

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
    public static final Logger LOGGER = LogManager.getLogger(ThutCore.MODID);
    public static final String MODID  = "thutcore";

    private static final String NETVERSION = "1.0.0";

    public static final PacketHandler packets = new PacketHandler(new ResourceLocation(ThutCore.MODID, "comms"),
            ThutCore.NETVERSION);

    public static ThutCore instance;

    public static final Proxy proxy = DistExecutor.safeRunForDist(() -> thut.core.proxy.ClientProxy::new,
            () -> thut.core.proxy.CommonProxy::new);

    public static final ConfigHandler conf = new ConfigHandler();

    public static ItemStack THUTICON = ItemStack.EMPTY;

    public static final CreativeModeTab THUTITEMS = new CreativeModeTab("thut")
    {
        @Override
        public ItemStack makeIcon()
        {
            return ThutCore.THUTICON;
        }
    };

    private static Map<String, String> trimmed = Maps.newConcurrentMap();

    public static String trim(final String name)
    {
        if (name == null) return null;
        if (ThutCore.trimmed.containsKey(name)) return ThutCore.trimmed.get(name);
        String trim = name;
        // ROOT locale to prevent issues with turkish letters.
        trim = trim.toLowerCase(Locale.ROOT).trim();
        // Replace all not-resourcelocation chars
        trim = trim.replaceAll("([^a-zA-Z0-9 _-])", "");
        // Replace these too.
        trim = trim.replaceAll(" ", "_");
        ThutCore.trimmed.put(name, trim);
        return trim;
    }

    public static Random newRandom()
    {
        return new Random(System.nanoTime());
    }

    public ThutCore()
    {
        ThutCore.instance = this;

        final File logfile = FMLPaths.GAMEDIR.get().resolve("logs").resolve(ThutCore.MODID + ".log").toFile();
        if (logfile.exists()) logfile.delete();
        final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) ThutCore.LOGGER;
        final FileAppender appender = FileAppender.newBuilder().withFileName(logfile.getAbsolutePath()).setName(
                ThutCore.MODID).build();
        logger.addAppender(appender);
        appender.start();

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested
        // in
        MinecraftForge.EVENT_BUS.register(this);

        Tracker.init();

        // Register Config stuff
        Config.setupConfigs(ThutCore.conf, ThutCore.MODID, ThutCore.MODID);
    }

    private void doClientStuff(final FMLClientSetupEvent event)
    {
        ThutCore.proxy.setupClient(event);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerAboutToStart(final ServerAboutToStartEvent event)
    {
        // do something when the server starts
        ThutCore.LOGGER.debug("Clearing terrain cache");
        StructureManager.clear();
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        ThutCore.LOGGER.info("Setup");

        if (ThutCore.THUTICON.isEmpty()) ThutCore.THUTICON = new ItemStack(ThutCrafts.CRAFTMAKER);

        // Register the actual packets
        ThutCore.packets.registerMessage(EntityUpdate.class, EntityUpdate::new);
        ThutCore.packets.registerMessage(TileUpdate.class, TileUpdate::new);
        ThutCore.packets.registerMessage(TerrainUpdate.class, TerrainUpdate::new);
        ThutCore.packets.registerMessage(PacketDataSync.class, PacketDataSync::new);
        ThutCore.packets.registerMessage(GeneralUpdate.class, GeneralUpdate::new);
        ThutCore.packets.registerMessage(CapabilitySync.class, CapabilitySync::new);

        GeneralUpdate.init();
        CapabilitySync.init();

        // Register capabilities.

        CapabilityTerrainAffected.init();

        LinkableCaps.setup();
        ShearableCaps.setup();
        BreedableCaps.setup();
        AnimatedCaps.setup();
        CopyCaps.setup();

        ThutCore.proxy.setup(event);

        event.enqueueWork(() ->
        {
            // Register the mob serializers
            // for seats
            EntityDataSerializers.registerSerializer(IMultiplePassengerEntity.SEATSERIALIZER);
            // for Vec3ds
            EntityDataSerializers.registerSerializer(BlockEntityBase.VEC3DSER);
        });
    }
}
