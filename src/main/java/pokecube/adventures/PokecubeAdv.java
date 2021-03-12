package pokecube.adventures;

import com.google.common.collect.Maps;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.advancements.Triggers;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.poi.PointsOfInterest;
import pokecube.adventures.blocks.LaboratoryGlass;
import pokecube.adventures.blocks.afa.AfaBlock;
import pokecube.adventures.blocks.afa.AfaContainer;
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
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeHandlers;
import pokecube.adventures.blocks.genetics.splicer.SplicerBlock;
import pokecube.adventures.blocks.genetics.splicer.SplicerContainer;
import pokecube.adventures.blocks.genetics.splicer.SplicerTile;
import pokecube.adventures.blocks.siphon.SiphonBlock;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.adventures.blocks.warppad.WarppadBlock;
import pokecube.adventures.blocks.warppad.WarppadTile;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.init.SetupHandler;
import pokecube.adventures.inventory.trainer.ContainerTrainer;
import pokecube.adventures.items.Linker;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.adventures.items.bag.BagItem;
import pokecube.adventures.proxy.ClientProxy;
import pokecube.adventures.proxy.CommonProxy;
import pokecube.adventures.utils.RecipePokeAdv;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.utils.PokeType;
import pokecube.legends.PokecubeLegends;
import thut.core.common.commands.CommandConfigs;
import thut.core.common.network.PacketHandler;

import java.util.Locale;
import java.util.Map;

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
        public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
        {
            // register a new mob here
            event.getRegistry().register(TrainerNpc.TYPE.setRegistryName(PokecubeAdv.MODID, "trainer"));
            event.getRegistry().register(LeaderNpc.TYPE.setRegistryName(PokecubeAdv.MODID, "leader"));
        }

        @SubscribeEvent
        public static void onEntityAttributes(final EntityAttributeCreationEvent event)
        {
            final AttributeModifierMap.MutableAttribute attribs = LivingEntity.createLivingAttributes()
                    .add(Attributes.FOLLOW_RANGE, 16.0D).add(
                            Attributes.ATTACK_KNOCKBACK).add(Attributes.MAX_HEALTH, 20.0D);
            event.put(TrainerNpc.TYPE, attribs.build());
            event.put(LeaderNpc.TYPE, attribs.build());
        }

        @SubscribeEvent
        public static void registerMemories(final RegistryEvent.Register<MemoryModuleType<?>> event)
        {
            MemoryTypes.register(event);
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            // register items

            // Register the badges
            for (final PokeType type : PokeType.values())
            {
                final Item badge = new Item(new Item.Properties().tab(PokecubeItems.POKECUBEITEMS));
                final String name = type.name.equals("???") ? "unknown" : type.name;
                PokecubeAdv.BADGES.put(type, badge);
                PokecubeAdv.BADGEINV.put(badge, type);
                badge.setRegistryName(PokecubeAdv.MODID, "badge_" + name.toLowerCase(Locale.ROOT));
                event.getRegistry().register(badge);
            }
        }

        @SubscribeEvent
        public static void registerProfessions(final RegistryEvent.Register<VillagerProfession> event)
        {
            // TODO figure this out again.
            // Professions.register(event);
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void textureStitch(final TextureStitchEvent.Pre event)
        {
            if (!event.getMap().location().toString().equals("minecraft:textures/atlas/blocks.png")) return;
            event.addSprite(new ResourceLocation(PokecubeAdv.MODID, "items/slot_dna"));
            event.addSprite(new ResourceLocation(PokecubeAdv.MODID, "items/slot_egg"));
            event.addSprite(new ResourceLocation(PokecubeAdv.MODID, "items/slot_bottle"));
            event.addSprite(new ResourceLocation(PokecubeAdv.MODID, "items/slot_selector"));
        }
    }

    public static final String MODID = "pokecube_adventures";

    public static final RegistryObject<Block> AFA;
    public static final RegistryObject<Block> COMMANDER;
    public static final RegistryObject<Block> DAYCARE;
    public static final RegistryObject<Block> CLONER;
    public static final RegistryObject<Block> EXTRACTOR;
    public static final RegistryObject<Block> SPLICER;
    public static final RegistryObject<Block> SIPHON;
    public static final RegistryObject<Block> WARPPAD;
    public static final RegistryObject<Block> LAB_GLASS;

    public static final RegistryObject<Item> EXPSHARE;
    public static final RegistryObject<Item> LINKER;
    public static final RegistryObject<Item> BAG;

    public static final RegistryObject<TileEntityType<AfaTile>>       AFA_TYPE;
    public static final RegistryObject<TileEntityType<CommanderTile>> COMMANDER_TYPE;
    public static final RegistryObject<TileEntityType<DaycareTile>>   DAYCARE_TYPE;
    public static final RegistryObject<TileEntityType<ClonerTile>>    CLONER_TYPE;
    public static final RegistryObject<TileEntityType<ExtractorTile>> EXTRACTOR_TYPE;
    public static final RegistryObject<TileEntityType<SplicerTile>>   SPLICER_TYPE;
    public static final RegistryObject<TileEntityType<SiphonTile>>    SIPHON_TYPE;
    public static final RegistryObject<TileEntityType<WarppadTile>>   WARPPAD_TYPE;

    public static final RegistryObject<ContainerType<AfaContainer>>       AFA_CONT;
    public static final RegistryObject<ContainerType<ClonerContainer>>    CLONER_CONT;
    public static final RegistryObject<ContainerType<ExtractorContainer>> EXTRACTOR_CONT;
    public static final RegistryObject<ContainerType<SplicerContainer>>   SPLICER_CONT;
    public static final RegistryObject<ContainerType<BagContainer>>       BAG_CONT;
    public static final RegistryObject<ContainerType<ContainerTrainer>>   TRAINER_CONT;

    public static final DeferredRegister<Block> BLOCKS;
    public static final DeferredRegister<Block> DECORATIONS;
    public static final DeferredRegister<Item>  ITEMS;

    public static final DeferredRegister<TileEntityType<?>> TILES;
    public static final DeferredRegister<ContainerType<?>>  CONTAINERS;

    public static final Map<PokeType, Item> BADGES   = Maps.newHashMap();
    public static final Map<Item, PokeType> BADGEINV = Maps.newHashMap();

    static
    {
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PokecubeAdv.MODID);
        CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, PokecubeAdv.MODID);
        DECORATIONS = DeferredRegister.create(ForgeRegistries.BLOCKS, PokecubeAdv.MODID);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PokecubeAdv.MODID);
        TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, PokecubeAdv.MODID);

        // Blocks
        AFA = PokecubeAdv.BLOCKS.register("afa", () -> new AfaBlock(AbstractBlock.Properties.of(Material.METAL)
                .dynamicShape(), MaterialColor.COLOR_LIGHT_GREEN));
        COMMANDER = PokecubeAdv.BLOCKS.register("commander", () -> new CommanderBlock(AbstractBlock.Properties.of(
                Material.METAL).dynamicShape(), MaterialColor.COLOR_RED));
        DAYCARE = PokecubeAdv.BLOCKS.register("daycare", () -> new DaycareBlock(AbstractBlock.Properties.of(Material.METAL)
                .dynamicShape(), MaterialColor.COLOR_BLACK));
        CLONER = PokecubeAdv.BLOCKS.register("cloner", () -> new ClonerBlock(AbstractBlock.Properties.of(Material.METAL)
                .dynamicShape(), MaterialColor.COLOR_PURPLE));
        EXTRACTOR = PokecubeAdv.BLOCKS.register("extractor", () -> new ExtractorBlock(AbstractBlock.Properties.of(
                Material.METAL).dynamicShape(), MaterialColor.COLOR_CYAN));
        SPLICER = PokecubeAdv.BLOCKS.register("splicer", () -> new SplicerBlock(AbstractBlock.Properties.of(Material.METAL)
                .dynamicShape(), MaterialColor.COLOR_CYAN));
        SIPHON = PokecubeAdv.BLOCKS.register("siphon", () -> new SiphonBlock(AbstractBlock.Properties.of(Material.METAL)
                .dynamicShape(), MaterialColor.TERRACOTTA_GREEN));
        WARPPAD = PokecubeAdv.BLOCKS.register("warppad", () -> new WarppadBlock(AbstractBlock.Properties.of(Material.METAL),
                MaterialColor.TERRACOTTA_WHITE));
        LAB_GLASS = PokecubeAdv.DECORATIONS.register("laboratory_glass", () -> new LaboratoryGlass(
                DyeColor.LIGHT_BLUE, AbstractBlock.Properties.of(Material.GLASS).strength(0.3f, 0.3f)
                .sound(SoundType.GLASS).noOcclusion(), MaterialColor.COLOR_LIGHT_BLUE));

        // Items
        EXPSHARE = PokecubeAdv.ITEMS.register("exp_share", () -> new Item(new Item.Properties().tab(
                PokecubeItems.POKECUBEITEMS)));
        LINKER = PokecubeAdv.ITEMS.register("linker", () -> new Linker(new Item.Properties().tab(
                PokecubeItems.POKECUBEITEMS)));
        BAG = PokecubeAdv.ITEMS.register("bag", () -> new BagItem(new Item.Properties().tab(
                PokecubeItems.POKECUBEITEMS)));

        // Tile Entities
        AFA_TYPE = PokecubeAdv.TILES.register("afa", () -> TileEntityType.Builder.of(AfaTile::new, PokecubeAdv.AFA
                .get()).build(null));
        COMMANDER_TYPE = PokecubeAdv.TILES.register("commander", () -> TileEntityType.Builder.of(CommanderTile::new,
                PokecubeAdv.COMMANDER.get()).build(null));
        DAYCARE_TYPE = PokecubeAdv.TILES.register("daycare", () -> TileEntityType.Builder.of(DaycareTile::new,
                PokecubeAdv.DAYCARE.get()).build(null));
        CLONER_TYPE = PokecubeAdv.TILES.register("cloner", () -> TileEntityType.Builder.of(ClonerTile::new,
                PokecubeAdv.CLONER.get()).build(null));
        EXTRACTOR_TYPE = PokecubeAdv.TILES.register("extractor", () -> TileEntityType.Builder.of(ExtractorTile::new,
                PokecubeAdv.EXTRACTOR.get()).build(null));
        SPLICER_TYPE = PokecubeAdv.TILES.register("splicer", () -> TileEntityType.Builder.of(SplicerTile::new,
                PokecubeAdv.SPLICER.get()).build(null));
        SIPHON_TYPE = PokecubeAdv.TILES.register("siphon", () -> TileEntityType.Builder.of(SiphonTile::new,
                PokecubeAdv.SIPHON.get()).build(null));
        WARPPAD_TYPE = PokecubeAdv.TILES.register("warppad", () -> TileEntityType.Builder.of(WarppadTile::new,
                PokecubeAdv.WARPPAD.get()).build(null));

        // Containers

        CLONER_CONT = PokecubeAdv.CONTAINERS.register("cloner", () -> new ContainerType<>(ClonerContainer::new));
        EXTRACTOR_CONT = PokecubeAdv.CONTAINERS.register("extractor", () -> new ContainerType<>(
                ExtractorContainer::new));
        SPLICER_CONT = PokecubeAdv.CONTAINERS.register("splicer", () -> new ContainerType<>(SplicerContainer::new));
        AFA_CONT = PokecubeAdv.CONTAINERS.register("afa", () -> new ContainerType<>(AfaContainer::new));
        BAG_CONT = PokecubeAdv.CONTAINERS.register("bag", () -> new ContainerType<>(
                (IContainerFactory<BagContainer>) BagContainer::new));
        TRAINER_CONT = PokecubeAdv.CONTAINERS.register("trainer", () -> new ContainerType<>(
                (IContainerFactory<ContainerTrainer>) ContainerTrainer::new));
    }

    private static void init()
    {
        // Register the item blocks.
        for (final RegistryObject<Block> reg : PokecubeAdv.BLOCKS.getEntries())
            PokecubeAdv.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .tab(PokecubeItems.POKECUBEBLOCKS)));
        
        for (final RegistryObject<Block> reg : PokecubeAdv.DECORATIONS.getEntries())
            PokecubeAdv.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()
                    .tab(PokecubeLegends.DECO_TAB)));

        // Initialize advancement triggers
        Triggers.init();

        // Initialize the recipe handlers for genetics stuff.
        RecipeHandlers.init();
    }

    public static final String TRAINERTEXTUREPATH = PokecubeAdv.MODID + ":textures/trainer/";

    // This proxy is used for the following:
    //
    // Server vs client implementations of Wearable, the client one has extended
    // rendering functions.
    public final static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    private static final String NETVERSION = "1.0.1";
    // Handler for network stuff.
    public static final PacketHandler packets = new PacketHandler(new ResourceLocation(PokecubeAdv.MODID, "comms"),
            PokecubeAdv.NETVERSION);

    public static final Config config = Config.instance;

    public PokecubeAdv()
    {
        PokecubeAdv.init();
        // Pokeplayer.init();
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        PokecubeAdv.BLOCKS.register(modEventBus);
        PokecubeAdv.DECORATIONS.register(modEventBus);
        PokecubeAdv.ITEMS.register(modEventBus);
        PokecubeAdv.TILES.register(modEventBus);
        PokecubeAdv.CONTAINERS.register(modEventBus);

        modEventBus.addListener(PokecubeAdv.proxy::setup);
        // Register the doClientStuff method for modloading
        modEventBus.addListener(PokecubeAdv.proxy::setupClient);
        // Register the loaded method for modloading
        modEventBus.addListener(PokecubeAdv.proxy::loaded);

        modEventBus.addListener(this::loadComplete);

        RecipePokeAdv.RECIPE_SERIALIZERS.register(modEventBus);
        PointsOfInterest.REG.register(modEventBus);

        // Register Config stuff
        thut.core.common.config.Config.setupConfigs(PokecubeAdv.config, PokecubeCore.MODID, PokecubeAdv.MODID);

        // Register event handlers
        SetupHandler.registerListeners();

        MinecraftForge.EVENT_BUS.register(this);
        PokecubeCore.POKEMOB_BUS.register(this);
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        PointsOfInterest.postInit();
    }

    @SubscribeEvent
    /**
     * Register the commands.
     *
     * @param event
     */
    public void registerCommands(final RegisterCommandsEvent event)
    {
        CommandConfigs.register(PokecubeAdv.config, event.getDispatcher(), "pokeadvsettings");
    }
}
