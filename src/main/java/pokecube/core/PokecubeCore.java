package pokecube.core;

import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.NewRegistry;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.npc.Activities;
import pokecube.core.ai.npc.Schedules;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.entity.pokemobs.GenericPokemob;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.events.onload.RegisterPokemobsEvent;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.handlers.ItemHandler;
import pokecube.core.handlers.PaintingsHandler;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.handlers.data.Drops;
import pokecube.core.handlers.data.Recipes;
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
import pokecube.core.moves.Battle;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.network.EntityProvider;
import pokecube.core.proxy.CommonProxy;
import pokecube.core.world.dimension.SecretBaseDimension;
import pokecube.core.world.gen.WorldgenFeatures;
import pokecube.core.world.gen.template.PokecubeStructureProcessors;
import thut.api.entity.CopyCaps;
import thut.api.maths.Vector3;
import thut.api.particle.ThutParticles;
import thut.api.terrain.BiomeDatabase;
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
        public static void registerStructures(final RegistryEvent.Register<StructureFeature<?>> event)
        {
            new BerryGenManager().processStructures(event);
        }

        @SubscribeEvent
        public static void registerFeatures(final RegistryEvent.Register<Feature<?>> event)
        {
            PokecubeCore.LOGGER.debug("Registering Pokecube Features");

            // Register the fossil stone spawning.

            final Predicate<ResourceKey<Biome>> check = k -> PokecubeCore.config.generateFossils && (BiomeDatabase
                    .contains(k, "ocean") || BiomeDatabase.contains(k, "sandy"));
            // Currently this uses same settings as gold ore.
            WorldgenHandler.INSTANCE.register(check, GenerationStep.Decoration.UNDERGROUND_ORES, Feature.ORE.configured(
                    new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, PokecubeItems.FOSSILSTONE.get()
                            .defaultBlockState(), 9)).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(31))
                    .squared().count(2), new ResourceLocation("pokecube:fossilstone"));

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
        public static void registerContainers(final RegistryEvent.Register<MenuType<?>> event)
        {
            // register a new container here
            PokecubeCore.LOGGER.debug("Registering Pokecube Containers");

            event.getRegistry().register(ContainerPokemob.TYPE.setRegistryName(PokecubeCore.MODID, "pokemob"));
            event.getRegistry().register(HealerContainer.TYPE.setRegistryName(PokecubeCore.MODID, "healer"));
            event.getRegistry().register(PCContainer.TYPE.setRegistryName(PokecubeCore.MODID, "pc"));
            event.getRegistry().register(TMContainer.TYPE.setRegistryName(PokecubeCore.MODID, "tm_machine"));
            event.getRegistry().register(TradeContainer.TYPE.setRegistryName(PokecubeCore.MODID, "trade_machine"));
        }

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
            Database.init();
            PokecubeCore.POKEMOB_BUS.post(new RegisterPokemobsEvent.Pre());
            PokecubeCore.POKEMOB_BUS.post(new RegisterPokemobsEvent.Register());
            PokedexEntryLoader.postInit();
            for (final PokedexEntry entry : Database.getSortedFormes())
            {
                if (entry.dummy) continue;
                if (!entry.stock) continue;
                try
                {
                    final PokemobType<ShoulderRidingEntity> type = new PokemobType<>(GenericPokemob::new, entry);
                    type.setRegistryName(PokecubeCore.MODID, entry.getTrimmedName());
                    event.getRegistry().register(type);
                    Pokedex.getInstance().registerPokemon(entry);
                    PokecubeCore.typeMap.put(type, entry);
                    CopyCaps.register(type);
                }
                catch (final Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
            PokecubeCore.POKEMOB_BUS.post(new RegisterPokemobsEvent.Post());
            Database.postInit();
            PokecubeCore.POKEMOB_BUS.post(new InitDatabase.Post());

            CopyCaps.register(NpcMob.TYPE);
            CopyCaps.register(EntityType.ARMOR_STAND);
        }

        @SubscribeEvent
        public static void onEntityAttributes(final EntityAttributeCreationEvent event)
        {
            // register a new mob here
            PokecubeCore.LOGGER.debug("Registering Pokecube Attributes");

            final AttributeSupplier.Builder attribs = LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE,
                    16.0D).add(Attributes.ATTACK_KNOCKBACK).add(Attributes.MAX_HEALTH, 10.0D);
            event.put(EntityPokecube.TYPE, attribs.build());
            event.put(EntityPokemobEgg.TYPE, attribs.build());
            event.put(NpcMob.TYPE, attribs.build());

            for (final PokedexEntry entry : Database.getSortedFormes())
            {
                if (entry.dummy) continue;
                if (!entry.stock) continue;
                try
                {
                    event.put(entry.getEntityType(), attribs.build());
                }
                catch (final Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            // register a new item here
            PokecubeCore.LOGGER.debug("Registering Pokecube Items");
            ItemHandler.registerItems(event.getRegistry());
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
        public static void registerTileEntities(final RegistryEvent.Register<BlockEntityType<?>> event)
        {
            // register a new TE here
            PokecubeCore.LOGGER.debug("Registering Pokecube TEs");
            ItemHandler.registerTiles(event.getRegistry());
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void textureStitch(final TextureStitchEvent.Pre event)
        {
            if (!event.getMap().location().toString().equals("minecraft:textures/atlas/blocks.png")) return;
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
    public static CommonProxy proxy;

    // Spawner for world spawning of pokemobs.
    public static SpawnHandler spawner = new SpawnHandler();

    // Map to store the registered mobs in.
    public static Map<EntityType<? extends Mob>, PokedexEntry> typeMap = Maps.newHashMap();

    // Provider for entities.
    public static IEntityProvider provider = new EntityProvider(null);

    /**
     * Generates the mobEntity for the given pokedex entry.
     *
     * @param evolution
     * @param world
     * @return
     */
    public static Mob createPokemob(final PokedexEntry entry, final Level world)
    {
        if (entry == null) return null;
        if (world == null) return null;
        EntityType<? extends Mob> type = entry.getEntityType();
        if (type == null && entry.getBaseForme() != null) type = entry.getBaseForme().getEntityType();
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
        return PokecubeCore.typeMap.get(type);
    }

    public static void spawnParticle(final Level entityWorld, final String name, final Vector3 position,
            Vector3 velocity, final int... args)
    {
        final ParticleOptions particle = ThutParticles.makeParticle(name, position, velocity, args);
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

        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        PokecubeItems.ITEMS.register(bus);
        PokecubeItems.BLOCKS.register(bus);
        PokecubeItems.TILES.register(bus);

        bus.addListener(this::loadComplete);
        bus.addGenericListener(Motive.class, PaintingsHandler::registerPaintings);

        RecipeHandler.init(bus);
        SecretBaseDimension.onConstruct(bus);
        PokecubeStructureProcessors.init(bus);
        WorldgenFeatures.init(bus);
        PointsOfInterest.REG.register(bus);
        new WorldgenHandler(bus);


        // Register the player data we use with thutcore
        PlayerDataHandler.register(PokecubePlayerData.class);
        PlayerDataHandler.register(PokecubePlayerStats.class);
        PlayerDataHandler.register(PokecubePlayerCustomData.class);
        PlayerDataHandler.register(PlayerPokemobCache.class);

        // Initialize advancement triggers
        Triggers.init();

        // Register the battle managers
        Battle.register();

        BiomeDictionary.addTypes(SecretBaseDimension.BIOME_KEY, BiomeDictionary.Type.VOID);
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        ItemGenerator.strippableBlocks(event);
        ItemGenerator.compostables(event);
        ItemGenerator.flammables(event);
        PointsOfInterest.postInit();
    }
}
