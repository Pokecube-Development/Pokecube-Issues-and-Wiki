package pokecube.adventures;

import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pokecube.adventures.advancements.Triggers;
import pokecube.adventures.blocks.BlockEventHandler;
import pokecube.adventures.blocks.afa.AfaBlock;
import pokecube.adventures.blocks.afa.AfaTile;
import pokecube.adventures.blocks.commander.CommanderBlock;
import pokecube.adventures.blocks.commander.CommanderTile;
import pokecube.adventures.blocks.daycare.DaycareBlock;
import pokecube.adventures.blocks.daycare.DaycareTile;
import pokecube.adventures.blocks.genetics.cloner.ClonerBlock;
import pokecube.adventures.blocks.genetics.cloner.ClonerContainer;
import pokecube.adventures.blocks.genetics.cloner.ClonerTile;
import pokecube.adventures.blocks.genetics.extractor.ExtractorBlock;
import pokecube.adventures.blocks.genetics.extractor.ExtractorContainer;
import pokecube.adventures.blocks.genetics.extractor.ExtractorTile;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredProcess;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeHandlers;
import pokecube.adventures.blocks.genetics.splicer.SplicerBlock;
import pokecube.adventures.blocks.genetics.splicer.SplicerContainer;
import pokecube.adventures.blocks.genetics.splicer.SplicerTile;
import pokecube.adventures.blocks.siphon.SiphonBlock;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.adventures.blocks.warppad.WarppadBlock;
import pokecube.adventures.blocks.warppad.WarppadTile;
import pokecube.adventures.client.ClientProxy;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.events.TrainerEventHandler;
import pokecube.adventures.events.TrainerSpawnHandler;
import pokecube.adventures.items.Linker;
import pokecube.adventures.items.TrainerEditor;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.adventures.items.bag.BagItem;
import pokecube.adventures.utils.EnergyHandler;
import pokecube.adventures.utils.InventoryHandler;
import pokecube.adventures.utils.TrainerTracker;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.recipes.XMLRecipeHandler;
import pokecube.core.database.rewards.XMLRewardsHandler;
import pokecube.core.utils.PokeType;
import thut.core.common.commands.CommandConfigs;
import thut.core.common.network.PacketHandler;

@Mod(value = PokecubeAdv.MODID)
public class PokecubeAdv
{
    // You can use EventBusSubscriber to automatically subscribe events on
    // the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeAdv.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event)
        {
            // register blocks
            event.getRegistry().register(PokecubeAdv.AFA);
            event.getRegistry().register(PokecubeAdv.COMMANDER);
            event.getRegistry().register(PokecubeAdv.DAYCARE);
            event.getRegistry().register(PokecubeAdv.CLONER);
            event.getRegistry().register(PokecubeAdv.EXTRACTOR);
            event.getRegistry().register(PokecubeAdv.SPLICER);
            event.getRegistry().register(PokecubeAdv.SIPHON);
            event.getRegistry().register(PokecubeAdv.WARPPAD);
        }

        @SubscribeEvent
        public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
        {
            // Register Containers
            event.getRegistry().register(ClonerContainer.TYPE.setRegistryName(PokecubeAdv.MODID, "cloner"));
            event.getRegistry().register(ExtractorContainer.TYPE.setRegistryName(PokecubeAdv.MODID, "extractor"));
            event.getRegistry().register(SplicerContainer.TYPE.setRegistryName(PokecubeAdv.MODID, "splicer"));
            event.getRegistry().register(BagContainer.TYPE.setRegistryName(PokecubeAdv.MODID, "bag"));
        }

        @SubscribeEvent
        public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
        {
            // register a new mob here
            event.getRegistry().register(TrainerNpc.TYPE.setRegistryName(PokecubeAdv.MODID, "trainer"));
            event.getRegistry().register(LeaderNpc.TYPE.setRegistryName(PokecubeAdv.MODID, "leader"));
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            // register items

            // Register the item blocks.
            event.getRegistry().register(new BlockItem(PokecubeAdv.AFA, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.AFA.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.COMMANDER, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.COMMANDER.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.DAYCARE, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.DAYCARE.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.CLONER, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.CLONER.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.EXTRACTOR, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.EXTRACTOR.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.SPLICER, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.SPLICER.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.SIPHON, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.SIPHON.getRegistryName()));
            event.getRegistry().register(new BlockItem(PokecubeAdv.WARPPAD, new Item.Properties().group(
                    PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeAdv.WARPPAD.getRegistryName()));

            // Register some items
            event.getRegistry().register(PokecubeAdv.EXPSHARE);
            event.getRegistry().register(PokecubeAdv.LINKER);
            event.getRegistry().register(PokecubeAdv.BAG);
            event.getRegistry().register(PokecubeAdv.TRAINEREDITOR);

            // Register the badges
            for (final PokeType type : PokeType.values())
            {
                final Item badge = new Item(new Item.Properties().group(PokecubeItems.POKECUBEITEMS));
                final String name = type.name.equals("???") ? "unknown" : type.name;
                PokecubeAdv.BADGES.put(type, badge);
                PokecubeAdv.BADGEINV.put(badge, type);
                badge.setRegistryName(PokecubeAdv.MODID, "badge_" + name.toLowerCase(Locale.ROOT));
                event.getRegistry().register(badge);
            }
        }

        @SubscribeEvent
        public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event)
        {
            PoweredProcess.init(event);
        }

        @SubscribeEvent
        public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event)
        {

            AfaTile.TYPE = TileEntityType.Builder.create(AfaTile::new, PokecubeAdv.AFA).build(null);
            CommanderTile.TYPE = TileEntityType.Builder.create(CommanderTile::new, PokecubeAdv.COMMANDER).build(null);
            DaycareTile.TYPE = TileEntityType.Builder.create(DaycareTile::new, PokecubeAdv.DAYCARE).build(null);
            ClonerTile.TYPE = TileEntityType.Builder.create(ClonerTile::new, PokecubeAdv.CLONER).build(null);
            ExtractorTile.TYPE = TileEntityType.Builder.create(ExtractorTile::new, PokecubeAdv.EXTRACTOR).build(null);
            SplicerTile.TYPE = TileEntityType.Builder.create(SplicerTile::new, PokecubeAdv.SPLICER).build(null);
            SiphonTile.TYPE = TileEntityType.Builder.create(SiphonTile::new, PokecubeAdv.SIPHON).build(null);
            WarppadTile.TYPE = TileEntityType.Builder.create(WarppadTile::new, PokecubeAdv.WARPPAD).build(null);
            // register tile entities
            event.getRegistry().register(AfaTile.TYPE.setRegistryName(PokecubeAdv.AFA.getRegistryName()));
            event.getRegistry().register(CommanderTile.TYPE.setRegistryName(PokecubeAdv.COMMANDER.getRegistryName()));
            event.getRegistry().register(DaycareTile.TYPE.setRegistryName(PokecubeAdv.DAYCARE.getRegistryName()));
            event.getRegistry().register(ClonerTile.TYPE.setRegistryName(PokecubeAdv.CLONER.getRegistryName()));
            event.getRegistry().register(ExtractorTile.TYPE.setRegistryName(PokecubeAdv.EXTRACTOR.getRegistryName()));
            event.getRegistry().register(SplicerTile.TYPE.setRegistryName(PokecubeAdv.SPLICER.getRegistryName()));
            event.getRegistry().register(SiphonTile.TYPE.setRegistryName(PokecubeAdv.SIPHON.getRegistryName()));
            event.getRegistry().register(WarppadTile.TYPE.setRegistryName(PokecubeAdv.WARPPAD.getRegistryName()));
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void textureStitch(final TextureStitchEvent.Pre event)
        {
            if (!event.getMap().getBasePath().equals("textures")) return;
            event.addSprite(new ResourceLocation(PokecubeAdv.MODID, "items/slot_dna"));
            event.addSprite(new ResourceLocation(PokecubeAdv.MODID, "items/slot_bottle"));
            event.addSprite(new ResourceLocation(PokecubeAdv.MODID, "items/slot_selector"));
        }
    }

    public static final String MODID = "pokecube_adventures";

    public static Block AFA;
    public static Block COMMANDER;
    public static Block DAYCARE;
    public static Block CLONER;
    public static Block EXTRACTOR;
    public static Block SPLICER;
    public static Block SIPHON;
    public static Block WARPPAD;

    public static Item EXPSHARE;
    public static Item LINKER;
    public static Item BAG;
    public static Item TRAINEREDITOR;

    public static final Map<PokeType, Item> BADGES   = Maps.newHashMap();
    public static final Map<Item, PokeType> BADGEINV = Maps.newHashMap();

    static void init()
    {
        PokecubeAdv.AFA = new AfaBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.MODID,
                "afa");
        PokecubeAdv.COMMANDER = new CommanderBlock(Block.Properties.create(Material.IRON)).setRegistryName(
                PokecubeAdv.MODID, "commander");
        PokecubeAdv.DAYCARE = new DaycareBlock(Block.Properties.create(Material.IRON)).setRegistryName(
                PokecubeAdv.MODID, "daycare");
        PokecubeAdv.CLONER = new ClonerBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.MODID,
                "cloner");
        PokecubeAdv.EXTRACTOR = new ExtractorBlock(Block.Properties.create(Material.IRON)).setRegistryName(
                PokecubeAdv.MODID, "extractor");
        PokecubeAdv.SPLICER = new SplicerBlock(Block.Properties.create(Material.IRON)).setRegistryName(
                PokecubeAdv.MODID, "splicer");
        PokecubeAdv.SIPHON = new SiphonBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.MODID,
                "siphon");
        PokecubeAdv.WARPPAD = new WarppadBlock(Block.Properties.create(Material.IRON)).setRegistryName(
                PokecubeAdv.MODID, "warppad");
        PokecubeAdv.EXPSHARE = new Item(new Item.Properties().group(PokecubeItems.POKECUBEITEMS)).setRegistryName(
                PokecubeAdv.MODID, "exp_share");
        PokecubeAdv.LINKER = new Linker(new Item.Properties().group(PokecubeItems.POKECUBEITEMS)).setRegistryName(
                PokecubeAdv.MODID, "linker");
        PokecubeAdv.BAG = new BagItem(new Item.Properties().group(PokecubeItems.POKECUBEITEMS)).setRegistryName(
                PokecubeAdv.MODID, "bag");
        PokecubeAdv.TRAINEREDITOR = new TrainerEditor(new Item.Properties().group(PokecubeItems.POKECUBEITEMS))
                .setRegistryName(PokecubeAdv.MODID, "trainer_editor");

        // Initialize advancement triggers
        Triggers.init();

        // Initialize the recipe handlers for genetics stuff.
        RecipeHandlers.init();
    }

    public static final String TRAINERTEXTUREPATH = PokecubeAdv.MODID + ":textures/trainer/";

    public final static CommonProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(),
            () -> () -> new CommonProxy());

    private static final String NETVERSION = "1.0.0";
    // Handler for network stuff.
    public static final PacketHandler packets = new PacketHandler(new ResourceLocation(PokecubeAdv.MODID, "comms"),
            PokecubeAdv.NETVERSION);

    public static final Config config = Config.instance;

    public PokecubeAdv()
    {
        // Initialize items and blocks
        PokecubeAdv.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeAdv.proxy::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeAdv.proxy::setupClient);
        // Register the loaded method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeAdv.proxy::loaded);

        // Register Config stuff
        thut.core.common.config.Config.setupConfigs(PokecubeAdv.config, PokecubeCore.MODID, PokecubeAdv.MODID);

        // Register event handlers

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(TrainerEventHandler.class);
        MinecraftForge.EVENT_BUS.register(TrainerSpawnHandler.class);
        MinecraftForge.EVENT_BUS.register(BagItem.class);
        MinecraftForge.EVENT_BUS.register(Linker.class);
        MinecraftForge.EVENT_BUS.register(EnergyHandler.class);
        MinecraftForge.EVENT_BUS.register(InventoryHandler.class);
        MinecraftForge.EVENT_BUS.register(BlockEventHandler.class);
        MinecraftForge.EVENT_BUS.register(TrainerTracker.class);

        PokecubeCore.POKEMOB_BUS.register(TrainerEventHandler.class);
        PokecubeCore.POKEMOB_BUS.register(this);

        XMLRewardsHandler.recipeFiles.add(new ResourceLocation(PokecubeAdv.MODID, "database/rewards.json"));
        XMLRecipeHandler.recipeFiles.add(new ResourceLocation(PokecubeAdv.MODID, "database/recipes.json"));
    }

    @SubscribeEvent
    /**
     * Register the commands.
     *
     * @param event
     */
    public void serverStarting(final FMLServerStartingEvent event)
    {
        CommandConfigs.register(PokecubeAdv.config, event.getCommandDispatcher(), "pokeadvsettings");
    }
}
