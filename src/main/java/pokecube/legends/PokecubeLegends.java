package pokecube.legends;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.utils.DBLoader;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.events.onload.RegisterPokecubes;
import pokecube.core.interfaces.IPokecube.DefaultPokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock.State;
import pokecube.legends.handlers.ForgeEventHandlers;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.Config;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.PokecubeDim;
import pokecube.legends.init.function.UsableItemGigantShard;
import pokecube.legends.init.function.UsableItemNatureEffects;
import pokecube.legends.init.function.UsableItemZMoveEffects;
import pokecube.legends.proxy.ClientProxy;
import pokecube.legends.proxy.CommonProxy;
import pokecube.legends.tileentity.RaidSpawn;

@Mod(value = Reference.ID)
public class PokecubeLegends
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.ID);
    public static final DeferredRegister<Block> BLOCKS_TAB = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.ID);
    public static final DeferredRegister<Item>  ITEMS  = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.ID);

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID)
    public static class RegistryHandler
    {
        @SubscribeEvent
        public static void onItemRegister(final RegistryEvent.Register<Item> event)
        {
            ItemInit.registerItems(event);
        }

        @SubscribeEvent
        public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event)
        {
            RaidSpawn.TYPE = TileEntityType.Builder.create(RaidSpawn::new, BlockInit.RAID_SPAWN.get()).build(null);
            event.getRegistry().register(RaidSpawn.TYPE.setRegistryName(BlockInit.RAID_SPAWN.get().getRegistryName()));
        }

        @SubscribeEvent
        public static void registerFeatures(final RegistryEvent.Register<Feature<?>> event)
        {
            PokecubeCore.LOGGER.debug("Registering Pokecube Legends Features");
            new WorldgenHandler(Reference.ID).processStructures(event);

        }

        /*@SubscribeEvent
        public static void registerBiomes(final RegistryEvent.Register<Biome> event)
        {
            BiomeInit.registerBiomes(event);
        }

        @SubscribeEvent
        public static void registerModDimensions(final RegistryKey<World> event)
        {
            event..register(new UltraSpaceModDimension().setRegistryName(ModDimensions.DIMENSION_ID));

            PokecubeLegends.LOGGER.debug("Registering Pokecube UltraSpace");
        }*/
    }

    public static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    public static final Config config = new Config();

    public PokecubeLegends()
    {
        thut.core.common.config.Config.setupConfigs(PokecubeLegends.config, PokecubeCore.MODID, Reference.ID);
        MinecraftForge.EVENT_BUS.register(this);
        PokecubeCore.POKEMOB_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        DBLoader.trainerDatabases.add(new ResourceLocation(Reference.ID, "database/trainer/trainers.json"));
        DBLoader.tradeDatabases.add(new ResourceLocation(Reference.ID, "database/trainer/trades.json"));

        // Register setup for proxy
        modEventBus.addListener(PokecubeLegends.proxy::setup);
        // Register the doClientStuff method for modloading
        modEventBus.addListener(PokecubeLegends.proxy::setupClient);
        // Register the loaded method for modloading
        modEventBus.addListener(PokecubeLegends.proxy::loaded);
        // Just generally register it to event bus.
        modEventBus.register(PokecubeLegends.proxy);

        PokecubeLegends.BLOCKS.register(modEventBus);
        PokecubeLegends.ITEMS.register(modEventBus);
        PokecubeLegends.BLOCKS_TAB.register(modEventBus);

        BlockInit.init();
        ItemInit.init();
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(final AttachCapabilitiesEvent<ItemStack> event)
    {
        UsableItemNatureEffects.registerCapabilities(event);
        UsableItemZMoveEffects.registerCapabilities(event);
        UsableItemGigantShard.registerCapabilities(event);
    }

    @SubscribeEvent
    public void registerDatabases(final InitDatabase.Pre evt)
    {
        Database.addDatabase("pokecube_legends:database/pokemobs/pokemobs_spawns.json", EnumDatabase.POKEMON);
    }

    public static final ItemGroup TAB = new ItemGroup("ultratab") {

    	@Override
    	public ItemStack createIcon()
    	{
    		return new ItemStack(BlockInit.ULTRA_MAGNETIC.get());
    	}
    };

    @SubscribeEvent
    public void registerPokecubes(final RegisterPokecubes event)
    {
        final PokecubeDim helper = new PokecubeDim();

        event.behaviors.add(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.beast(mob);
            }
        }.setRegistryName(Reference.ID, "beast"));
        event.behaviors.add(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.clone(mob);
            }
        }.setRegistryName(Reference.ID, "clone"));
        event.behaviors.add(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.typingB(mob);
            }
        }.setRegistryName(Reference.ID, "typing"));
        event.behaviors.add(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.teamAqua(mob);
            }
        }.setRegistryName(Reference.ID, "teamaqua"));
        event.behaviors.add(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.teamMagma(mob);
            }
        }.setRegistryName(Reference.ID, "teammagma"));
        event.behaviors.add(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.teamR(mob);
            }
        }.setRegistryName(Reference.ID, "rocket"));
    }

    @SubscribeEvent
    public void serverStarting(final FMLServerStartingEvent event)
    {
        PokecubeLegends.config.loaded = true;
        PokecubeLegends.config.onUpdated();
    }

    @SubscribeEvent
    public void reactivate_raid(final RightClickBlock event)
    {
        if (event.getWorld().isRemote) return;
        if (event.getItemStack().getItem() != ItemInit.WISHING_PIECE.get()) return;
        final BlockState hit = event.getWorld().getBlockState(event.getPos());
        if (hit.getBlock() != BlockInit.RAID_SPAWN.get())
        {
            if (hit.getBlock() == PokecubeItems.DYNABLOCK) event.getPlayer().sendMessage(new TranslationTextComponent(
                    "msg.notaraidspot.info"), Util.DUMMY_UUID);
            return;
        }
        final boolean active = hit.get(RaidSpawnBlock.ACTIVE).active();
        if (active) return;
        else
        {
            final State state = new Random().nextInt(20) == 0 ? State.RARE : State.NORMAL;
            event.getWorld().setBlockState(event.getPos(), hit.with(RaidSpawnBlock.ACTIVE, state));
            event.setUseItem(Result.ALLOW);
            event.getItemStack().grow(-1);
        }
    }
}
