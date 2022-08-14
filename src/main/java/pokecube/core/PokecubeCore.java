package pokecube.core;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.NewRegistryEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.events.init.InitDatabase;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.npc.Activities;
import pokecube.core.ai.npc.Schedules;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.database.Database;
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
import pokecube.core.impl.PokecubeMod;
import pokecube.core.init.EntityTypes;
import pokecube.core.init.MenuTypes;
import pokecube.core.init.Sounds;
import pokecube.core.moves.Battle;
import pokecube.core.proxy.CommonProxy;
import pokecube.core.world.dimension.SecretBaseDimension;
import pokecube.world.PokecubeWorld;
import thut.api.entity.CopyCaps;
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
        public static void registerRegistry(final NewRegistryEvent event)
        {
            // Register these before items and blocks, as some items might need
            // them
            final InitDatabase.Pre pre = new InitDatabase.Pre();
            PokecubeAPI.POKEMOB_BUS.post(pre);
            pre.modIDs.add(PokecubeCore.MODID);
            Database.preInit();
            Sounds.initMoveSounds();
            Sounds.initConfigSounds();
            EntityTypes.registerPokemobs();
        }

        @SubscribeEvent
        public static void gatherData(final GatherDataEvent event)
        {
            final DataGenerator gen = event.getGenerator();
            gen.addProvider(new Recipes(gen));
            gen.addProvider(new Drops(gen));
        }

        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event)
        {
            // register a new block here
            PokecubeAPI.LOGGER.debug("Registering Blocks");
            ItemHandler.registerBlocks(event.getRegistry());
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            // register a new item here
            PokecubeAPI.LOGGER.debug("Registering Pokecube Items");
            ItemHandler.registerItems(event.getRegistry());
        }
    }

    public static final DeferredRegister<RecipeType<?>> RECIPETYPE;
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNKGENTYPE;
    public static final DeferredRegister<Activity> ACTIVITIES;
    public static final DeferredRegister<Schedule> SCHEDULES;
    public static final DeferredRegister<MemoryModuleType<?>> MEMORIES;
    public static final DeferredRegister<SensorType<?>> SENSORS;
    public static final DeferredRegister<Block> BERRIES_TAB;
    public static final DeferredRegister<Block> BLOCKS;
    public static final DeferredRegister<Item> ITEMS;
    public static final DeferredRegister<BlockEntityType<?>> TILES;
    public static final DeferredRegister<EntityType<?>> ENTITIES;
    public static final DeferredRegister<MenuType<?>> MENU;
    public static final DeferredRegister<SoundEvent> SOUNDS;

    static
    {
        RECIPETYPE = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, PokecubeCore.MODID);
        CHUNKGENTYPE = DeferredRegister.create(Registry.CHUNK_GENERATOR_REGISTRY, PokecubeCore.MODID);
        ACTIVITIES = DeferredRegister.create(Registry.ACTIVITY_REGISTRY, PokecubeCore.MODID);
        SCHEDULES = DeferredRegister.create(Registry.SCHEDULE_REGISTRY, PokecubeCore.MODID);
        MEMORIES = DeferredRegister.create(Registry.MEMORY_MODULE_TYPE_REGISTRY, PokecubeCore.MODID);
        SENSORS = DeferredRegister.create(Registry.SENSOR_TYPE_REGISTRY, PokecubeCore.MODID);
        BERRIES_TAB = DeferredRegister.create(ForgeRegistries.BLOCKS, PokecubeCore.MODID);
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PokecubeCore.MODID);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PokecubeCore.MODID);
        TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, PokecubeCore.MODID);
        ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, PokecubeCore.MODID);
        MENU = DeferredRegister.create(ForgeRegistries.CONTAINERS, PokecubeCore.MODID);
        SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PokecubeCore.MODID);
    }

    public static final String MODID = PokecubeAPI.MODID;

    private static final String NETVERSION = "1.0.2";
    // Handler for network stuff.
    public static final PacketHandler packets = new PacketHandler(new ResourceLocation(PokecubeCore.MODID, "comms"),
            PokecubeCore.NETVERSION);

    // Holder for our config options
    private static final Config config = new Config();

    // Sided proxy for handling server/client only stuff.
    public static CommonProxy proxy;

    // Spawner for world spawning of pokemobs.
    public static SpawnHandler spawner = new SpawnHandler();

    // Map to store the registered mobs in.
    public static Map<EntityType<? extends Mob>, PokedexEntry> typeMap = Maps.newHashMap();

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
        PokecubeMod.setLogger(PokecubeAPI.LOGGER);

        // Initialize the items and blocks.
        PokecubeItems.init();

        // Register Config stuff
        thut.core.common.config.Config.setupConfigs(PokecubeCore.config, PokecubeCore.MODID, PokecubeCore.MODID);

        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        PokecubeCore.ITEMS.register(bus);
        PokecubeCore.BLOCKS.register(bus);
        PokecubeCore.BERRIES_TAB.register(bus);
        PokecubeCore.TILES.register(bus);
        PokecubeCore.ENTITIES.register(bus);
        PokecubeCore.MENU.register(bus);
        PokecubeCore.CHUNKGENTYPE.register(bus);
        PokecubeCore.RECIPETYPE.register(bus);
        PokecubeCore.ACTIVITIES.register(bus);
        PokecubeCore.SENSORS.register(bus);
        PokecubeCore.SCHEDULES.register(bus);
        PokecubeCore.MEMORIES.register(bus);
        PokecubeCore.SOUNDS.register(bus);

        PokecubeWorld.init(bus);

        bus.addListener(this::loadComplete);
        bus.addGenericListener(Motive.class, PaintingsHandler::registerPaintings);

        RecipeHandler.init(bus);
        PointsOfInterest.REG.register(bus);

        new BerryGenManager();
        SecretBaseDimension.onConstruct(bus);

        // Register the player data we use with thutcore
        PlayerDataHandler.register(PokecubePlayerData.class);
        PlayerDataHandler.register(PokecubePlayerStats.class);
        PlayerDataHandler.register(PokecubePlayerCustomData.class);
        PlayerDataHandler.register(PlayerPokemobCache.class);

        // Initialize advancement triggers
        Triggers.init();

        // Init some more things that register stuff
        Activities.init();
        Schedules.init();
        MemoryModules.init();
        Sensors.init();
        MenuTypes.init();
        EntityTypes.init();
        Sounds.init();

        // Register the battle managers
        Battle.register();
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        ItemGenerator.strippableBlocks(event);
        ItemGenerator.compostables(event);
        ItemGenerator.flammables(event);

        event.enqueueWork(() -> {
            PointsOfInterest.postInit();
            ItemHandler.postInit();

            CopyCaps.register(EntityTypes.getNpc());
            CopyCaps.register(EntityType.ARMOR_STAND);
        });
    }
}
