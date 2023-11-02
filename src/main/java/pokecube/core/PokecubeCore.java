package pokecube.core;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.NewRegistryEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.conditions.PokemobConditionLoader;
import pokecube.api.data.spawns.matchers.MatcherLoaders;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.init.InitDatabase;
import pokecube.api.moves.Battle;
import pokecube.api.raids.RaidManager;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.npc.Activities;
import pokecube.core.ai.npc.Schedules;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.database.Database;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.handlers.DispenseBehaviors;
import pokecube.core.handlers.PaintingsHandler;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.handlers.playerdata.PokecubePlayerData;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.init.Config;
import pokecube.core.init.EntityTypes;
import pokecube.core.init.ItemGenerator;
import pokecube.core.init.ItemInit;
import pokecube.core.init.MenuTypes;
import pokecube.core.init.Sounds;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.legacy.RegistryChangeFixer;
import pokecube.core.proxy.CommonProxy;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.Permissions;
import pokecube.world.PokecubeWorld;
import pokecube.world.dimension.SecretBaseDimension;
import thut.api.ThutCaps;
import thut.api.data.StringTag;
import thut.api.entity.CopyCaps;
import thut.api.maths.Vector3;
import thut.api.particle.ThutParticles;
import thut.core.common.ThutCore;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.network.PacketHandler;
import thut.wearables.ThutWearables;

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
            MatcherLoaders.init();
            PokemobConditionLoader.init();
            Database.preInit();
            Sounds.initMoveSounds();
            Sounds.initConfigSounds();
            EntityTypes.registerPokemobs();

            // Now we can initialise some of the custom items.
            ItemInit.init();
        }

        @SubscribeEvent
        public static void registerPacks(AddPackFindersEvent event)
        {
            if (event.getPackType() == PackType.SERVER_DATA)
            {
                event.addRepositorySource(PackFinder.DEFAULT_FINDER);
            }
        }
    }

    public static final DeferredRegister<RecipeType<?>> RECIPETYPE;
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNKGENTYPE;
    public static final DeferredRegister<Activity> ACTIVITIES;
    public static final DeferredRegister<Schedule> SCHEDULES;
    public static final DeferredRegister<MemoryModuleType<?>> MEMORIES;
    public static final DeferredRegister<SensorType<?>> SENSORS;
    public static final DeferredRegister<Block> BERRY_BLOCKS;
    public static final DeferredRegister<Block> BLOCKS;
    public static final DeferredRegister<Item> ITEMS;
    public static final DeferredRegister<BlockEntityType<?>> TILES;
    public static final DeferredRegister<EntityType<?>> ENTITIES;
    public static final DeferredRegister<MenuType<?>> MENU;
    public static final DeferredRegister<SoundEvent> SOUNDS;
    public static final DeferredRegister<Motive> PAINTINGS;

    static
    {
        RECIPETYPE = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, PokecubeCore.MODID);
        CHUNKGENTYPE = DeferredRegister.create(Registry.CHUNK_GENERATOR_REGISTRY, PokecubeCore.MODID);
        ACTIVITIES = DeferredRegister.create(Registry.ACTIVITY_REGISTRY, PokecubeCore.MODID);
        SCHEDULES = DeferredRegister.create(Registry.SCHEDULE_REGISTRY, PokecubeCore.MODID);
        MEMORIES = DeferredRegister.create(Registry.MEMORY_MODULE_TYPE_REGISTRY, PokecubeCore.MODID);
        SENSORS = DeferredRegister.create(Registry.SENSOR_TYPE_REGISTRY, PokecubeCore.MODID);
        BERRY_BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PokecubeCore.MODID);
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PokecubeCore.MODID);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PokecubeCore.MODID);
        TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, PokecubeCore.MODID);
        ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, PokecubeCore.MODID);
        MENU = DeferredRegister.create(ForgeRegistries.CONTAINERS, PokecubeCore.MODID);
        SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PokecubeCore.MODID);
        PAINTINGS = DeferredRegister.create(ForgeRegistries.PAINTING_TYPES, PokecubeCore.MODID);
    }

    public static final String MODID = PokecubeAPI.MODID;

    private static final String NETVERSION = "1.0.2";
    // Handler for network stuff.
    public static final PacketHandler packets = new PacketHandler(new ResourceLocation(PokecubeCore.MODID, "comms"),
            PokecubeCore.NETVERSION);

    // Holder for our config options
    private static final Config config = new Config();

    // Sided proxy for handling server/client only stuff.
    public static final CommonProxy proxy = DistExecutor.safeRunForDist(() -> pokecube.core.proxy.ClientProxy::new,
            () -> pokecube.core.proxy.CommonProxy::new);

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
        if (type != null)
        {
            Mob mob = null;
            if (entry.stock && type.toString().equals("entity.minecraft.pig"))
            {
                type = Database.missingno.getEntityType();
                mob = type.create(world);
            }
            else mob = type.create(world);
            IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
            pokemob.setBasePokedexEntry(entry);
            pokemob.setPokedexEntry(entry);
            return mob;
        }
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

        StringTag.RESOURCE_PROVIDER = PackFinder::getAllJsonResources;

        // Initialize the items and blocks.
        PokecubeItems.init();

        // Register Config stuff
        thut.core.common.config.Config.setupConfigs(PokecubeCore.config, PokecubeCore.MODID, PokecubeCore.MODID);

        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        PokecubeCore.BERRY_BLOCKS.register(bus);
        PokecubeCore.ITEMS.register(bus);
        PokecubeCore.BLOCKS.register(bus);
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
        PokecubeCore.PAINTINGS.register(bus);

        PokecubeWorld.init(bus);

        bus.addListener(this::loadComplete);

        RecipeHandler.init(bus);
        PointsOfInterest.REG.register(bus);

        new BerryGenManager();
        SecretBaseDimension.onConstruct(bus);

        // Register the player data we use with thutcore
        PlayerDataHandler.register(PokecubePlayerData.class);
        PlayerDataHandler.register(PokecubePlayerStats.class);
        PlayerDataHandler.register(PokecubePlayerCustomData.class);
        PlayerDataHandler.register(PlayerPokemobCache.class);

        // Register the data fixer for registry changes.
        ThutCore.FORGE_BUS.register(RegistryChangeFixer.class);

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
        PaintingsHandler.init();
        RaidManager.init();

        // Register the battle managers
        Battle.register();
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        ItemGenerator.strippableBlocks(event);
        ItemGenerator.compostables(event);
        ItemGenerator.flammables(event);

        // Register all of the types to the animation holder set.
        typeMap.keySet().forEach(CopyCaps::register);

        event.enqueueWork(() -> {
            DispenseBehaviors.registerDefaults();
            ItemInit.postInit();
            PointsOfInterest.postInit();
            Permissions.register();

            CopyCaps.register(EntityTypes.getNpc());
            CopyCaps.register(EntityType.ARMOR_STAND);

            if (PokecubeItems.POKECUBE_ITEMS.isEmpty())
                PokecubeItems.POKECUBE_ITEMS = new ItemStack(PokecubeItems.POKEDEX.get());
            if (PokecubeItems.POKECUBE_BERRIES.isEmpty())
                PokecubeItems.POKECUBE_BERRIES = new ItemStack(BerryManager.berryCrops.get(0).get());
            if (PokecubeItems.POKECUBE_CUBES.isEmpty())
                PokecubeItems.POKECUBE_CUBES = PokecubeItems.getStack("pokecube");

            EntityTools.registerCachedCap(ThutCaps.OWNABLE_CAP);
            EntityTools.registerCachedCap(ThutCaps.COLOURABLE);
            EntityTools.registerCachedCap(ThutCaps.ANIMATED);
            EntityTools.registerCachedCap(ThutCaps.SHEARABLE);
            EntityTools.registerCachedCap(ThutCaps.DATASYNC);
            EntityTools.registerCachedCap(ThutCaps.COPYMOB);
            EntityTools.registerCachedCap(ThutCaps.ANIMCAP);
            EntityTools.registerCachedCap(ThutCaps.MOBTEX_CAP);

            EntityTools.registerCachedCap(PokemobCaps.POKEMOB_CAP);
            EntityTools.registerCachedCap(PokemobCaps.AFFECTED_CAP);

            EntityTools.registerCachedCap(ThutWearables.WEARABLE_CAP);
            EntityTools.registerCachedCap(ThutWearables.WEARABLES_CAP);
        });
    }
}
