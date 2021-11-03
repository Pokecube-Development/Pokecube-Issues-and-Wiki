package pokecube.adventures;

import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.fmllegacy.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.advancements.Triggers;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.poi.PointsOfInterest;
import pokecube.adventures.blocks.LaboratoryGlassBlock;
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
import pokecube.adventures.blocks.warp_pad.WarpPadBlock;
import pokecube.adventures.blocks.warp_pad.WarpPadTile;
import pokecube.adventures.blocks.statue.PokemobStatue;
import pokecube.adventures.blocks.statue.StatueEntity;
import pokecube.adventures.blocks.statue.StatueItem;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.init.SetupHandler;
import pokecube.adventures.inventory.trainer.ContainerTrainer;
import pokecube.adventures.items.Linker;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.adventures.items.bag.BagItem;
import pokecube.adventures.proxy.CommonProxy;
import pokecube.adventures.utils.RecipePokeAdv;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.utils.PokeType;
import pokecube.legends.PokecubeLegends;
import thut.api.entity.CopyCaps;
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
        public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
        {
            // register a new mob here
            event.getRegistry().register(TrainerNpc.TYPE.setRegistryName(PokecubeAdv.MODID, "trainer"));
            event.getRegistry().register(LeaderNpc.TYPE.setRegistryName(PokecubeAdv.MODID, "leader"));

            CopyCaps.register(TrainerNpc.TYPE);
            CopyCaps.register(LeaderNpc.TYPE);
        }

        @SubscribeEvent
        public static void onEntityAttributes(final EntityAttributeCreationEvent event)
        {
            final AttributeSupplier.Builder attribs = LivingEntity.createLivingAttributes()
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
                final Item badge = new Item(new Item.Properties().tab(PokecubeItems.TAB_ITEMS));
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
    public static final RegistryObject<Block> WARP_PAD;
    public static final RegistryObject<Block> STATUE;
    public static final RegistryObject<Block> LAB_GLASS;

    public static final RegistryObject<Item> EXPSHARE;
    public static final RegistryObject<Item> LINKER;
    public static final RegistryObject<Item> BAG;

    public static final RegistryObject<BlockEntityType<AfaTile>>       AFA_TYPE;
    public static final RegistryObject<BlockEntityType<CommanderTile>> COMMANDER_TYPE;
    public static final RegistryObject<BlockEntityType<DaycareTile>>   DAYCARE_TYPE;
    public static final RegistryObject<BlockEntityType<ClonerTile>>    CLONER_TYPE;
    public static final RegistryObject<BlockEntityType<ExtractorTile>> EXTRACTOR_TYPE;
    public static final RegistryObject<BlockEntityType<SplicerTile>>   SPLICER_TYPE;
    public static final RegistryObject<BlockEntityType<SiphonTile>>    SIPHON_TYPE;
    public static final RegistryObject<BlockEntityType<WarpPadTile>>   WARP_PAD_TYPE;
    public static final RegistryObject<BlockEntityType<StatueEntity>>  STATUE_TYPE;

    public static final RegistryObject<MenuType<AfaContainer>>       AFA_CONT;
    public static final RegistryObject<MenuType<ClonerContainer>>    CLONER_CONT;
    public static final RegistryObject<MenuType<ExtractorContainer>> EXTRACTOR_CONT;
    public static final RegistryObject<MenuType<SplicerContainer>>   SPLICER_CONT;
    public static final RegistryObject<MenuType<BagContainer>>       BAG_CONT;
    public static final RegistryObject<MenuType<ContainerTrainer>>   TRAINER_CONT;

    public static final DeferredRegister<Block> BLOCKS;
    public static final DeferredRegister<Block> DECORATIONS;
    public static final DeferredRegister<Item>  ITEMS;

    public static final DeferredRegister<BlockEntityType<?>> TILES;
    public static final DeferredRegister<MenuType<?>>  CONTAINERS;

    public static final Map<PokeType, Item> BADGES   = Maps.newHashMap();
    public static final Map<Item, PokeType> BADGEINV = Maps.newHashMap();

    static
    {
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PokecubeAdv.MODID);
        CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, PokecubeAdv.MODID);
        DECORATIONS = DeferredRegister.create(ForgeRegistries.BLOCKS, PokecubeAdv.MODID);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PokecubeAdv.MODID);
        TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, PokecubeAdv.MODID);

        // Blocks
        AFA = PokecubeAdv.BLOCKS.register("afa", () -> new AfaBlock(BlockBehaviour.Properties.of(Material.METAL,
                MaterialColor.COLOR_LIGHT_GREEN).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().dynamicShape()));
        COMMANDER = PokecubeAdv.BLOCKS.register("commander", () -> new CommanderBlock(BlockBehaviour.Properties.of(
                Material.METAL, MaterialColor.COLOR_RED).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().dynamicShape()));
        DAYCARE = PokecubeAdv.BLOCKS.register("daycare", () -> new DaycareBlock(BlockBehaviour.Properties.of(Material.METAL,
                MaterialColor.COLOR_BLACK).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().dynamicShape()));
        CLONER = PokecubeAdv.BLOCKS.register("cloner", () -> new ClonerBlock(BlockBehaviour.Properties.of(Material.METAL,
                MaterialColor.COLOR_PURPLE).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().dynamicShape()));
        EXTRACTOR = PokecubeAdv.BLOCKS.register("extractor", () -> new ExtractorBlock(BlockBehaviour.Properties.of(
                Material.METAL, MaterialColor.COLOR_CYAN).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().dynamicShape()));
        SPLICER = PokecubeAdv.BLOCKS.register("splicer", () -> new SplicerBlock(BlockBehaviour.Properties.of(Material.METAL,
                MaterialColor.COLOR_CYAN).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().dynamicShape()));
        SIPHON = PokecubeAdv.BLOCKS.register("siphon", () -> new SiphonBlock(BlockBehaviour.Properties.of(Material.METAL,
                MaterialColor.TERRACOTTA_GREEN).strength(5.0F, 6.0F).sound(SoundType.METAL).dynamicShape()));
        WARP_PAD = PokecubeAdv.BLOCKS.register("warp_pad", () -> new WarpPadBlock(BlockBehaviour.Properties.of(Material.METAL,
                MaterialColor.SNOW).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
        STATUE = PokecubeAdv.BLOCKS.register("statue", () -> new PokemobStatue(BlockBehaviour.Properties.of(
                Material.STONE, MaterialColor.STONE).strength(5.0F, 6.0F).sound(SoundType.STONE).dynamicShape().noOcclusion()
                .requiresCorrectToolForDrops()));
        LAB_GLASS = PokecubeAdv.DECORATIONS.register("laboratory_glass", () -> new LaboratoryGlassBlock(
                DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.COLOR_LIGHT_BLUE)
                .strength(0.3f, 0.3f).sound(SoundType.GLASS).noOcclusion()));

        // Items
        EXPSHARE = PokecubeAdv.ITEMS.register("exp_share", () -> new Item(new Item.Properties().tab(
                PokecubeItems.TAB_ITEMS)));
        LINKER = PokecubeAdv.ITEMS.register("linker", () -> new Linker(new Item.Properties().tab(
                PokecubeItems.TAB_ITEMS)));
        BAG = PokecubeAdv.ITEMS.register("bag", () -> new BagItem(new Item.Properties().tab(
                PokecubeItems.TAB_ITEMS)));

        // Tile Entities
        AFA_TYPE = PokecubeAdv.TILES.register("afa", () -> BlockEntityType.Builder.of(AfaTile::new, PokecubeAdv.AFA
                .get()).build(null));
        COMMANDER_TYPE = PokecubeAdv.TILES.register("commander", () -> BlockEntityType.Builder.of(CommanderTile::new,
                PokecubeAdv.COMMANDER.get()).build(null));
        DAYCARE_TYPE = PokecubeAdv.TILES.register("daycare", () -> BlockEntityType.Builder.of(DaycareTile::new,
                PokecubeAdv.DAYCARE.get()).build(null));
        CLONER_TYPE = PokecubeAdv.TILES.register("cloner", () -> BlockEntityType.Builder.of(ClonerTile::new,
                PokecubeAdv.CLONER.get()).build(null));
        EXTRACTOR_TYPE = PokecubeAdv.TILES.register("extractor", () -> BlockEntityType.Builder.of(ExtractorTile::new,
                PokecubeAdv.EXTRACTOR.get()).build(null));
        SPLICER_TYPE = PokecubeAdv.TILES.register("splicer", () -> BlockEntityType.Builder.of(SplicerTile::new,
                PokecubeAdv.SPLICER.get()).build(null));
        SIPHON_TYPE = PokecubeAdv.TILES.register("siphon", () -> BlockEntityType.Builder.of(SiphonTile::new,
                PokecubeAdv.SIPHON.get()).build(null));
        WARP_PAD_TYPE = PokecubeAdv.TILES.register("warp_pad", () -> BlockEntityType.Builder.of(WarpPadTile::new,
                PokecubeAdv.WARP_PAD.get()).build(null));
        STATUE_TYPE = PokecubeAdv.TILES.register("statue", () -> BlockEntityType.Builder.of(StatueEntity::new,
                PokecubeAdv.STATUE.get()).build(null));

        // Containers

        CLONER_CONT = PokecubeAdv.CONTAINERS.register("cloner", () -> new MenuType<>(ClonerContainer::new));
        EXTRACTOR_CONT = PokecubeAdv.CONTAINERS.register("extractor", () -> new MenuType<>(
                ExtractorContainer::new));
        SPLICER_CONT = PokecubeAdv.CONTAINERS.register("splicer", () -> new MenuType<>(SplicerContainer::new));
        AFA_CONT = PokecubeAdv.CONTAINERS.register("afa", () -> new MenuType<>(AfaContainer::new));
        BAG_CONT = PokecubeAdv.CONTAINERS.register("bag", () -> new MenuType<>(
                (IContainerFactory<BagContainer>) BagContainer::new));
        TRAINER_CONT = PokecubeAdv.CONTAINERS.register("trainer", () -> new MenuType<>(
                (IContainerFactory<ContainerTrainer>) ContainerTrainer::new));
    }

    private static void init()
    {
        // Register the item blocks.
        for (final RegistryObject<Block> reg : PokecubeAdv.BLOCKS.getEntries())
        {
            final Item.Properties props = new Item.Properties().tab(PokecubeItems.TAB_BLOCKS);
            // Statue does something a bit differently.
            if (reg == PokecubeAdv.STATUE) PokecubeAdv.ITEMS.register(reg.getId().getPath(), () -> new StatueItem(reg.get(), props));
            else PokecubeAdv.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), props));
        }

        for (final RegistryObject<Block> reg : PokecubeAdv.DECORATIONS.getEntries())
            PokecubeAdv.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties().tab(
                    PokecubeLegends.TAB_DECORATIONS)));

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
    public static CommonProxy proxy;

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
