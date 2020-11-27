package pokecube.core;

import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.NewRegistry;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.npc.Activities;
import pokecube.core.ai.npc.Schedules;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.entity.pokemobs.GenericPokemob;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.events.onload.RegisterPokemobsEvent;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.ItemHandler;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.handlers.data.Drops;
import pokecube.core.handlers.data.Recipes;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.events.PokemobEventsHandler;
import pokecube.core.handlers.events.SpawnEventsHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.handlers.playerdata.PokecubePlayerData;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.interfaces.IEntityProvider;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.inventory.trade.TradeContainer;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.network.EntityProvider;
import pokecube.core.proxy.ClientProxy;
import pokecube.core.proxy.CommonProxy;
import pokecube.core.utils.PokemobTracker;
import pokecube.mobloader.MobLoader;
import thut.api.maths.Vector3;
import thut.api.particle.ThutParticles;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.network.PacketHandler;

@Mod(value = PokecubeCore.MODID)
public class PokecubeCore
{
    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerRegistry(final NewRegistry event)
        {
            // Register these before items and blocks, as some items might need
            // them
            final InitDatabase.Pre pre = new InitDatabase.Pre();
            PokecubeCore.POKEMOB_BUS.post(pre);
            pre.modIDs.add(PokecubeCore.MODID);
            Database.preInit();
        }
        //
        // @SubscribeEvent TODO Dimensions
        // public static void registerBiomes(final RegistryEvent.Register<Biome>
        // event)
        // {
        // PokecubeCore.LOGGER.debug("Registering Pokecube Biomes");
        // SecretBaseDimension.BIOME = new SecretBiome();
        // event.getRegistry().register(SecretBaseDimension.BIOME);
        // BiomeDictionary.addTypes(SecretBaseDimension.BIOME,
        // BiomeDictionary.Type.VOID);
        // }

        @SubscribeEvent
        public static void registerActivities(final RegistryEvent.Register<Activity> event)
        {
            Activities.register(event);
        }

        @SubscribeEvent
        public static void registerSchedules(final RegistryEvent.Register<Schedule> event)
        {
            Schedules.register(event);
        }

        @SubscribeEvent
        public static void registerMemories(final RegistryEvent.Register<MemoryModuleType<?>> event)
        {
            MemoryModules.register(event);
        }

        @SubscribeEvent
        public static void registerSensors(final RegistryEvent.Register<SensorType<?>> event)
        {
            Sensors.register(event);
        }

        @SubscribeEvent
        public static void gatherData(final GatherDataEvent event)
        {
            final DataGenerator gen = event.getGenerator();
            gen.addProvider(new Recipes(gen));
            gen.addProvider(new Drops(gen));
        }

        @SubscribeEvent
        public static void registerFeatures(final RegistryEvent.Register<Feature<?>> event)
        {
            PokecubeCore.LOGGER.debug("Registering Pokecube Features");

            // // Register the fossil stone spawning. TODO Ore Generation
            // if (PokecubeCore.config.generateFossils) for (final Biome b :
            // ForgeRegistries.BIOMES.getValues())
            // {
            // if (!(BiomeDatabase.contains(b, "sandy") ||
            // BiomeDatabase.contains(b, "ocean"))) continue;
            // // Currently this uses same settings as gold ore.
            // b.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
            // Feature.ORE.withConfiguration(
            // new
            // OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
            // PokecubeItems.FOSSILSTONE
            // .getDefaultState(),
            // 9)).withPlacement(Placement.COUNT_RANGE.configure(
            // new CountRangeConfig(2, 0, 0, 32))));
            // }

            // Register the general structure piece we use
            // Registry.register(Registry.STRUCTURE_PIECE,
            // "pokecube:jigsaw_piece", JigsawPieces.CSP);

            // Register structure processor types TODO Structure Processors
            // PokecubeStructureProcessor.TYPE =
            // IStructureProcessorType.register("pokecube:struct_process",
            // PokecubeStructureProcessor::new);
            // FillerProcessor.TYPE =
            // IStructureProcessorType.register("pokecube:struct_process_filler",
            // FillerProcessor::new);

            // Register the configurable worldgen things from datapack
            new WorldgenHandler().processStructures(event);
            new BerryGenManager().processStructures(event);

            // Register village stuff
            // TODO add in forge village stuff maybe here when it is done.
        }

        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event)
        {
            // register a new block here
            PokecubeCore.LOGGER.debug("Registering Blocks");
            ItemHandler.registerBlocks(event.getRegistry());
        }

        @SubscribeEvent
        public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
        {
            // register a new container here
            PokecubeCore.LOGGER.debug("Registering Pokecube Containers");

            event.getRegistry().register(ContainerPokemob.TYPE.setRegistryName(PokecubeCore.MODID, "pokemob"));
            event.getRegistry().register(HealerContainer.TYPE.setRegistryName(PokecubeCore.MODID, "healer"));
            event.getRegistry().register(PCContainer.TYPE.setRegistryName(PokecubeCore.MODID, "pc"));
            event.getRegistry().register(TMContainer.TYPE.setRegistryName(PokecubeCore.MODID, "tm_machine"));
            event.getRegistry().register(TradeContainer.TYPE.setRegistryName(PokecubeCore.MODID, "trade_machine"));
        }

        // @SubscribeEvent TODO Dimensions
        // public static void registerDimensions(final
        // RegistryEvent.Register<ModDimension> event)
        // {
        // PokecubeCore.LOGGER.debug("Registering Pokecube Dimensions");
        // event.getRegistry().register(SecretBaseDimension.DIMENSION.setRegistryName(SecretBaseDimension.ID));
        // }

        @SubscribeEvent
        public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
        {
            // register a new mob here
            PokecubeCore.LOGGER.debug("Registering Pokecube Mobs");

            // Register the non-pokemobs first
            event.getRegistry().register(EntityPokecube.TYPE.setRegistryName(PokecubeCore.MODID, "pokecube"));
            event.getRegistry().register(EntityPokemobEgg.TYPE.setRegistryName(PokecubeCore.MODID, "egg"));
            event.getRegistry().register(NpcMob.TYPE.setRegistryName(PokecubeCore.MODID, "npc"));
            event.getRegistry().register(EntityMoveUse.TYPE.setRegistryName(PokecubeCore.MODID, "move_use"));

            final AttributeModifierMap.MutableAttribute attribs = LivingEntity.registerAttributes()
                    .createMutableAttribute(Attributes.FOLLOW_RANGE, 16.0D).createMutableAttribute(
                            Attributes.ATTACK_KNOCKBACK).createMutableAttribute(Attributes.MAX_HEALTH, 10.0D);

            GlobalEntityTypeAttributes.put(EntityPokecube.TYPE, attribs.create());
            GlobalEntityTypeAttributes.put(EntityPokemobEgg.TYPE, attribs.create());
            GlobalEntityTypeAttributes.put(NpcMob.TYPE, attribs.create());

            Database.init();
            PokecubeCore.POKEMOB_BUS.post(new RegisterPokemobsEvent.Pre());
            PokecubeCore.POKEMOB_BUS.post(new RegisterPokemobsEvent.Register());

            for (final PokedexEntry entry : Database.getSortedFormes())
            {
                if (entry.dummy) continue;
                try
                {
                    final PokemobType<ShoulderRidingEntity> type = new PokemobType<>(GenericPokemob::new, entry);
                    type.setRegistryName(PokecubeCore.MODID, entry.getTrimmedName());
                    event.getRegistry().register(type);
                    GlobalEntityTypeAttributes.put(type, attribs.create());
                    Pokedex.getInstance().registerPokemon(entry);
                    PokecubeCore.typeMap.put(entry, type);
                }
                catch (final Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
            PokecubeCore.POKEMOB_BUS.post(new RegisterPokemobsEvent.Post());
            Database.postInit();
            PokecubeCore.POKEMOB_BUS.post(new InitDatabase.Post());
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            // register a new item here
            PokecubeCore.LOGGER.debug("Registering Pokecube Items");
            ItemHandler.registerItems(event.getRegistry());
        }

        @SubscribeEvent
        public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event)
        {
            // register a new mob here
            PokecubeCore.LOGGER.debug("Registering Pokecube Recipes");
            RecipeHandler.initRecipes(event);
        }

        @SubscribeEvent
        public static void registerSounds(final RegistryEvent.Register<SoundEvent> event)
        {
            // register a new mob here
            PokecubeCore.LOGGER.debug("Registering Pokecube Sounds");
            Database.initSounds(event.getRegistry());

            ResourceLocation sound = new ResourceLocation(PokecubeCore.MODID + ":pokecube_caught");
            event.getRegistry().register((EntityPokecubeBase.POKECUBESOUND = new SoundEvent(sound)).setRegistryName(
                    sound));
            sound = new ResourceLocation(PokecubeCore.MODID + ":pokecenter");
            event.getRegistry().register((HealerContainer.HEAL_SOUND = new SoundEvent(sound)).setRegistryName(sound));
            sound = new ResourceLocation(PokecubeCore.MODID + ":pokecenterloop");
            event.getRegistry().register((HealerTile.MUSICLOOP = new SoundEvent(sound)).setRegistryName(sound));
        }

        @SubscribeEvent
        public static void registerTileEntities(final RegistryEvent.Register<TileEntityType<?>> event)
        {
            // register a new TE here
            PokecubeCore.LOGGER.debug("Registering Pokecube TEs");
            ItemHandler.registerTiles(event.getRegistry());
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void textureStitch(final TextureStitchEvent.Pre event)
        {
            if (!event.getMap().getTextureLocation().toString().equals("minecraft:textures/atlas/blocks.png")) return;
            PokecubeCore.LOGGER.debug("Registering Pokecube Slot Textures");
            event.addSprite(new ResourceLocation(PokecubeCore.MODID, "items/slot_cube"));
            event.addSprite(new ResourceLocation(PokecubeCore.MODID, "items/slot_tm"));
        }
    }

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger(PokecubeCore.MODID);
    public static final String MODID  = "pokecube";

    private static final String NETVERSION = "1.0.2";
    // Handler for network stuff.
    public static final PacketHandler packets = new PacketHandler(new ResourceLocation(PokecubeCore.MODID, "comms"),
            PokecubeCore.NETVERSION);
    // Bus for move events
    public static final IEventBus MOVE_BUS = BusBuilder.builder().build();

    // Bus for Pokemob Events
    public static final IEventBus POKEMOB_BUS = BusBuilder.builder().build();

    // Holder for our config options
    private static final Config config = new Config();

    // Sided proxy for handling server/client only stuff.
    public final static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    // Spawner for world spawning of pokemobs.
    public static SpawnHandler spawner = new SpawnHandler();

    // Map to store the registered mobs in.
    public static BiMap<PokedexEntry, EntityType<ShoulderRidingEntity>> typeMap = HashBiMap.create();

    // Provider for entities.
    public static IEntityProvider provider = new EntityProvider(null);

    /**
     * Generates the mobEntity for the given pokedex entry.
     *
     * @param evolution
     * @param world
     * @return
     */
    public static MobEntity createPokemob(final PokedexEntry entry, final World world)
    {
        if (entry == null) return null;
        if (world == null) return null;
        EntityType<? extends MobEntity> type = PokecubeCore.typeMap.get(entry);
        if (type == null) type = PokecubeCore.typeMap.get(entry.getBaseForme());
        if (type != null) return type.create(world);
        return null;
    }

    /**
     * The config storing all our stuff.
     *
     * @return
     */
    public static Config getConfig()
    {
        return PokecubeCore.config;
    }

    /**
     * Allows for dealing with cases like pokeplayer, where the entity that the
     * world stores is not necessarily the one wanted for pokemob interaction.
     *
     * @return
     */
    public static IEntityProvider getEntityProvider()
    {
        return PokecubeCore.provider;
    }

    /**
     * For pokemob type mobs, this returns the pokedex entry for the
     * corresponding entity type, for other mobs it returns null.
     *
     * @param type
     * @return
     */
    @Nullable
    public static PokedexEntry getEntryFor(final EntityType<?> type)
    {
        return PokecubeCore.typeMap.inverse().get(type);
    }

    public static void spawnParticle(final World entityWorld, final String name, final Vector3 position,
            Vector3 velocity, final int... args)
    {
        final IParticleData particle = ThutParticles.makeParticle(name, position, velocity, args);
        if (velocity == null) velocity = Vector3.empty;
        entityWorld.addParticle(particle, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
    }

    public PokecubeCore()
    {
        PokecubeMod.setLogger(PokecubeCore.LOGGER);

        // Initialize the items and blocks.
        PokecubeItems.init();

        // Register Config stuff
        thut.core.common.config.Config.setupConfigs(PokecubeCore.config, PokecubeCore.MODID, PokecubeCore.MODID);

        PokecubeCore.POKEMOB_BUS.register(MobLoader.class);
        PokecubeCore.POKEMOB_BUS.register(SpawnEventsHandler.class);
        PokecubeCore.POKEMOB_BUS.register(PokemobEventsHandler.class);

        MinecraftForge.EVENT_BUS.register(SpawnEventsHandler.class);
        MinecraftForge.EVENT_BUS.register(EventsHandler.class);
        MinecraftForge.EVENT_BUS.register(PokemobTracker.class);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeCore.proxy::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeCore.proxy::setupClient);
        // Register imc comms sender
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register imc comms listender
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the loaded method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeCore.proxy::loaded);

        RecipeHandler.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());

        FMLJavaModLoadingContext.get().getModEventBus().register(PokecubeCore.proxy);
        MinecraftForge.EVENT_BUS.register(PokecubeCore.proxy);

        // Register the player data we use with thutcore
        PlayerDataHandler.register(PokecubePlayerData.class);
        PlayerDataHandler.register(PokecubePlayerStats.class);
        PlayerDataHandler.register(PokecubePlayerCustomData.class);
        PlayerDataHandler.register(PlayerPokemobCache.class);

        // Initialize advancement triggers
        Triggers.init();
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("thutcore", "helloworld", () ->
        {
            PokecubeCore.LOGGER.info("Hello from Pokecube Core");
            return "Hello ThutCore, sincerely Pokecube Core";
        });
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other
        // mods
        PokecubeCore.LOGGER.info("Got IMC {}", event.getIMCStream().map(m -> m.getMessageSupplier().get()).collect(
                Collectors.toList()));
    }
}
