package thut.core.common;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
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
// The value here should match an entry in the META-INF/mods.toml file
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import thut.api.AnimatedCaps;
import thut.api.LinkableCaps;
import thut.api.OwnableCaps;
import thut.api.entity.BreedableCaps;
import thut.api.entity.IMobColourable;
import thut.api.entity.IMobTexturable;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.ShearableCaps;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.BlockEntityInventory;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.particle.ThutParticles;
import thut.api.terrain.CapabilityTerrain;
import thut.api.terrain.StructureManager;
import thut.api.world.mobs.data.DataSync;
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
import thut.core.proxy.ClientProxy;
import thut.core.proxy.CommonProxy;
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

        public static EntityRayTraceResult rayTraceEntities(final Entity shooter, final Vector3d startVec,
                final Vector3d endVec, final AxisAlignedBB boundingBox, final Predicate<Entity> filter,
                final double distance)
        {
            final World world = shooter.level;
            double d0 = distance;
            Entity entity = null;
            Vector3d vector3d = null;

            for (final Entity entity1 : world.getEntities(shooter, boundingBox, filter))
            {
                final AxisAlignedBB axisalignedbb = entity1.getBoundingBox().inflate(entity1.getPickRadius());
                final Optional<Vector3d> optional = axisalignedbb.clip(startVec, endVec);
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
                    final Vector3d vector3d1 = optional.get();
                    final double d1 = startVec.distanceToSqr(vector3d1);
                    if (d1 < d0 || d0 == 0.0D) if (entity1.getRootVehicle() == shooter.getRootVehicle()
                            && !entity1.canRiderInteract())
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
            return entity == null ? null : new EntityRayTraceResult(entity, vector3d);
        }

        @SubscribeEvent
        public static void interact(final RightClickBlock event)
        {
            // Probably a block entity to interact with here.
            if (event.getWorld().isEmptyBlock(event.getPos()))
            {
                final PlayerEntity player = event.getPlayer();
                final Vector3d face = event.getPlayer().getEyePosition(0);
                final Vector3d look = event.getPlayer().getLookAngle();
                final AxisAlignedBB box = event.getPlayer().getBoundingBox().inflate(3, 3, 3);
                final EntityRayTraceResult var = MobEvents.rayTraceEntities(player, face, look, box,
                        e -> e instanceof IBlockEntity, 3);
                if (var != null && var.getType() == RayTraceResult.Type.ENTITY)
                {
                    final IBlockEntity entity = (IBlockEntity) var.getEntity();
                    if (entity.getInteractor().processInitialInteract(event.getPlayer(), event.getItemStack(), event
                            .getHand()) != ActionResultType.PASS)
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
    public static final Logger LOGGER = LogManager.getLogger(ThutCore.MODID);
    public static final String MODID  = "thutcore";

    private static final String NETVERSION = "1.0.0";

    public static final PacketHandler packets = new PacketHandler(new ResourceLocation(ThutCore.MODID, "comms"),
            ThutCore.NETVERSION);

    public static ThutCore instance;

    public static final Proxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    public static final ConfigHandler conf = new ConfigHandler();

    public static ItemStack THUTICON = ItemStack.EMPTY;

    public static final ItemGroup THUTITEMS = new ItemGroup("thut")
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
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested
        // in
        MinecraftForge.EVENT_BUS.register(this);

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
        StructureManager.clear();
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

        if (ThutCore.THUTICON.isEmpty()) ThutCore.THUTICON = new ItemStack(ThutCrafts.CRAFTMAKER);

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

        OwnableCaps.setup();
        LinkableCaps.setup();
        ShearableCaps.setup();
        BreedableCaps.setup();
        AnimatedCaps.setup();

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

        ThutCore.proxy.setup(event);

        event.enqueueWork(() ->
        {
            // Register the mob serializers
            // for seats
            DataSerializers.registerSerializer(IMultiplePassengerEntity.SEATSERIALIZER);
            // for Vec3ds
            DataSerializers.registerSerializer(BlockEntityBase.VEC3DSER);
        });
    }
}
