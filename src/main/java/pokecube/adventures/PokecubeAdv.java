package pokecube.adventures;

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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
import pokecube.adventures.blocks.genetics.splicer.SplicerBlock;
import pokecube.adventures.blocks.genetics.splicer.SplicerContainer;
import pokecube.adventures.blocks.genetics.splicer.SplicerTile;
import pokecube.adventures.blocks.siphon.SiphonBlock;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.adventures.blocks.warppad.WarppadBlock;
import pokecube.adventures.blocks.warppad.WarppadTile;
import pokecube.adventures.client.ClientProxy;
import pokecube.adventures.entity.trainer.EntityTrainer;
import pokecube.adventures.items.Linker;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.adventures.items.bag.BagItem;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.utils.PokeType;
import thut.core.common.network.PacketHandler;

@Mod(value = PokecubeAdv.ID)
public class PokecubeAdv
{
    // You can use EventBusSubscriber to automatically subscribe events on
    // the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeAdv.ID)
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
            event.getRegistry().register(ClonerContainer.TYPE.setRegistryName(PokecubeAdv.ID, "cloner"));
            event.getRegistry().register(ExtractorContainer.TYPE.setRegistryName(PokecubeAdv.ID, "extractor"));
            event.getRegistry().register(SplicerContainer.TYPE.setRegistryName(PokecubeAdv.ID, "splicer"));
            event.getRegistry().register(BagContainer.TYPE.setRegistryName(PokecubeAdv.ID, "bag"));
        }

        @SubscribeEvent
        public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
        {
            // register a new mob here
            event.getRegistry().register(EntityTrainer.TYPE.setRegistryName(PokecubeAdv.ID, "trainer"));
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

            // Register the badges
            for (final PokeType type : PokeType.values())
            {
                final Item badge = new Item(new Item.Properties().group(PokecubeItems.POKECUBEITEMS));
                final String name = type.name.equals("???") ? "unknown" : type.name;
                PokecubeAdv.BADGES.put(type, badge);
                badge.setRegistryName(PokecubeAdv.ID, "badge_" + name);
                event.getRegistry().register(badge);
            }
        }

        @SubscribeEvent
        public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event)
        {
            PoweredProcess.init();
        }

        @SubscribeEvent
        public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event)
        {
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
            System.out.println("registering sprites");
            event.addSprite(new ResourceLocation(PokecubeAdv.ID, "items/slot_dna"));
            event.addSprite(new ResourceLocation(PokecubeAdv.ID, "items/slot_bottle"));
            event.addSprite(new ResourceLocation(PokecubeAdv.ID, "items/slot_selector"));
        }
    }

    public static final String ID = "pokecube_adventures";

    public static final Block AFA;
    public static final Block COMMANDER;
    public static final Block DAYCARE;
    public static final Block CLONER;
    public static final Block EXTRACTOR;
    public static final Block SPLICER;
    public static final Block SIPHON;
    public static final Block WARPPAD;

    public static final Item EXPSHARE;
    public static final Item LINKER;
    public static final Item BAG;

    public static final Map<PokeType, Item> BADGES = Maps.newHashMap();

    static
    {
        AFA = new AfaBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "afa");
        COMMANDER = new CommanderBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID,
                "commander");
        DAYCARE = new DaycareBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "daycare");
        CLONER = new ClonerBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "cloner");
        EXTRACTOR = new ExtractorBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID,
                "extractor");
        SPLICER = new SplicerBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "splicer");
        SIPHON = new SiphonBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "siphon");
        WARPPAD = new WarppadBlock(Block.Properties.create(Material.IRON)).setRegistryName(PokecubeAdv.ID, "warppad");

        EXPSHARE = new Item(new Item.Properties().group(PokecubeItems.POKECUBEITEMS)).setRegistryName(PokecubeAdv.ID,
                "exp_share");
        LINKER = new Linker(new Item.Properties().group(PokecubeItems.POKECUBEITEMS)).setRegistryName(PokecubeAdv.ID,
                "linker");
        BAG = new BagItem(new Item.Properties().group(PokecubeItems.POKECUBEITEMS)).setRegistryName(PokecubeAdv.ID,
                "bag");
    }

    public static final String TRAINERTEXTUREPATH = PokecubeAdv.ID + ":textures/trainer/";

    public final static CommonProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(),
            () -> () -> new CommonProxy());

    private static final String NETVERSION = "1.0.0";
    // Handler for network stuff.
    public static final PacketHandler packets = new PacketHandler(new ResourceLocation(PokecubeAdv.ID, "comms"),
            PokecubeAdv.NETVERSION);

    public static final Config config = Config.instance;

    public PokecubeAdv()
    {

        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeAdv.proxy::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeAdv.proxy::setupClient);
        // Register the loaded method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeAdv.proxy::loaded);

        MinecraftForge.EVENT_BUS.register(this);
        PokecubeCore.POKEMOB_BUS.register(this);

        // Register Config stuff
        thut.core.common.config.Config.setupConfigs(PokecubeAdv.config, PokecubeCore.MODID, PokecubeAdv.ID);
    }
}
