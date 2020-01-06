package pokecube.core;

import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.client.ClientProxy;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.entity.pokemobs.GenericPokemob;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.entity.professor.EntityProfessor;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.events.onload.RegisterPokemobsEvent;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.ItemHandler;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.handlers.playerdata.PokecubePlayerData;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.interfaces.IEntityProvider;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.inventory.trade.TradeContainer;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.moves.implementations.MovesAdder;
import pokecube.core.network.EntityProvider;
import pokecube.core.world.dimension.SecretBaseDimension;
import thut.api.OwnableCaps;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.CapabilityAnimation;
import thut.core.client.render.particle.ThutParticles;
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
        public static void registerBiomes(final RegistryEvent.Register<Biome> event)
        {

        }

        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event)
        {
            // Register these before items and blocks, as some items might need
            // them
            final InitDatabase.Pre pre = new InitDatabase.Pre();
            PokecubeCore.POKEMOB_BUS.post(pre);
            pre.modIDs.add(PokecubeCore.MODID);
            Database.preInit();

            // register a new block here
            PokecubeCore.LOGGER.info("HELLO from Register Block");
            ItemHandler.registerBlocks(event.getRegistry());
        }

        @SubscribeEvent
        public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
        {
            // register a new container here
            PokecubeCore.LOGGER.info("Registering Pokecube Containers");

            event.getRegistry().register(ContainerPokemob.TYPE.setRegistryName(PokecubeCore.MODID, "pokemob"));
            event.getRegistry().register(HealerContainer.TYPE.setRegistryName(PokecubeCore.MODID, "healer"));
            event.getRegistry().register(PCContainer.TYPE.setRegistryName(PokecubeCore.MODID, "pc"));
            event.getRegistry().register(TMContainer.TYPE.setRegistryName(PokecubeCore.MODID, "tm_machine"));
            event.getRegistry().register(TradeContainer.TYPE.setRegistryName(PokecubeCore.MODID, "trade_machine"));
        }

        @SubscribeEvent
        public static void registerDimensions(final RegistryEvent.Register<ModDimension> event)
        {
            event.getRegistry().register(SecretBaseDimension.DIMENSION.setRegistryName(PokecubeCore.MODID,
                    "secret_bases"));
        }

        @SubscribeEvent
        public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
        {
            // register a new mob here
            PokecubeCore.LOGGER.info("Registering Pokecube Mobs");

            // Register the non-pokemobs first
            event.getRegistry().register(EntityPokecube.TYPE.setRegistryName(PokecubeCore.MODID, "pokecube"));
            event.getRegistry().register(EntityPokemobEgg.TYPE.setRegistryName(PokecubeCore.MODID, "egg"));
            event.getRegistry().register(EntityProfessor.TYPE.setRegistryName(PokecubeCore.MODID, "professor"));
            event.getRegistry().register(EntityMoveUse.TYPE.setRegistryName(PokecubeCore.MODID, "move_use"));

            Database.init();
            PokecubeCore.POKEMOB_BUS.post(new RegisterPokemobsEvent.Pre());
            PokecubeCore.POKEMOB_BUS.post(new RegisterPokemobsEvent.Register());

            // Register pokemob class for ownable caps
            OwnableCaps.MOBS.add(GenericPokemob.class);

            for (final PokedexEntry entry : Database.getSortedFormes())
            {
                if (entry.dummy) continue;
                try
                {
                    final PokemobType<GenericPokemob> type = new PokemobType<>(GenericPokemob::new, entry);
                    type.setRegistryName(PokecubeCore.MODID, entry.getTrimmedName());
                    event.getRegistry().register(type);
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
            MovesAdder.registerMoves();
            PokecubeCore.POKEMOB_BUS.post(new InitDatabase.Post());
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            // register a new item here
            PokecubeCore.LOGGER.info("Registering Pokecube Items");
            ItemHandler.registerItems(event.getRegistry());
        }

        @SubscribeEvent
        public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event)
        {
            // register a new mob here
            PokecubeCore.LOGGER.info("Registering Pokecube Sounds");
            RecipeHandler.initRecipes(event);
        }

        @SubscribeEvent
        public static void registerSounds(final RegistryEvent.Register<SoundEvent> event)
        {
            // register a new mob here
            PokecubeCore.LOGGER.info("Registering Pokecube Sounds");
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
            PokecubeCore.LOGGER.info("Registering Pokecube TEs");
            ItemHandler.registerTiles(event.getRegistry());
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void textureStitch(final TextureStitchEvent.Pre event)
        {
            if (!event.getMap().getBasePath().equals("textures")) return;
            event.addSprite(new ResourceLocation(PokecubeCore.MODID, "items/cube_slot"));
            event.addSprite(new ResourceLocation(PokecubeCore.MODID, "items/tm_slot"));
        }
    }

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID  = "pokecube";

    private static final String NETVERSION = "1.0.0";
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
    public final static CommonProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(),
            () -> () -> new CommonProxy());

    // Spawner for world spawning of pokemobs.
    public static SpawnHandler spawner = new SpawnHandler();

    // Map to store the registered mobs in.
    public static BiMap<PokedexEntry, EntityType<? extends MobEntity>> typeMap = HashBiMap.create();

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
        // Register Config stuff
        thut.core.common.config.Config.setupConfigs(PokecubeCore.config, PokecubeCore.MODID, PokecubeCore.MODID);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeCore.proxy::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeCore.proxy::setupClient);
        // Register imc comms sender
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register imc comms listender
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the loaded method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeCore.proxy::loaded);

        FMLJavaModLoadingContext.get().getModEventBus().register(PokecubeCore.proxy);

        // Register the player data we use with thutcore
        PlayerDataHandler.register(PokecubePlayerData.class);
        PlayerDataHandler.register(PokecubePlayerStats.class);
        PlayerDataHandler.register(PokecubePlayerCustomData.class);
        PlayerDataHandler.register(PlayerPokemobCache.class);

        // Register the pokemob class for animations.
        CapabilityAnimation.registerAnimateClass(GenericPokemob.class);

        // Initialize advancement triggers
        Triggers.init();

        MinecraftForge.EVENT_BUS.register(EventsHandler.class);
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
