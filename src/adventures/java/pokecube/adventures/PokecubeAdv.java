package pokecube.adventures;

import com.google.common.collect.Maps;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import pokecube.adventures.advancements.Triggers;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.poi.PointsOfInterest;
import pokecube.adventures.ai.poi.Professions;
import pokecube.adventures.blocks.LaboratoryGlassBlock;
import pokecube.adventures.blocks.LaboratoryGlassPaneBlock;
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
import pokecube.adventures.blocks.genetics.helper.SelectorImpl;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeHandlers;
import pokecube.adventures.blocks.genetics.splicer.SplicerBlock;
import pokecube.adventures.blocks.genetics.splicer.SplicerContainer;
import pokecube.adventures.blocks.genetics.splicer.SplicerTile;
import pokecube.adventures.blocks.siphon.SiphonBlock;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.adventures.blocks.statue.PokemobStatue;
import pokecube.adventures.blocks.statue.StatueEntity;
import pokecube.adventures.blocks.statue.StatueItem;
import pokecube.adventures.blocks.warp_pad.WarpPadBlock;
import pokecube.adventures.blocks.warp_pad.WarpPadTile;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.InitCaps;
import pokecube.adventures.init.AdvCreativeTabs;
import pokecube.adventures.init.EntityTypes;
import pokecube.adventures.init.SetupHandler;
import pokecube.adventures.inventory.trainer.ContainerTrainer;
import pokecube.adventures.items.Linker;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.adventures.items.bag.BagItem;
import pokecube.adventures.utils.EnergyHandler;
import pokecube.adventures.utils.RecipePokeAdv;
import pokecube.api.PokecubeAPI;
import pokecube.api.events.init.RegisterMiscItems;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import thut.api.attachments.CopyMob;
import thut.core.common.ThutCore;
import thut.core.common.commands.CommandConfigs;
import thut.core.common.network.PacketHandler;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@Mod(value = PokecubeAdv.MODID)
public class PokecubeAdv
{
    // You can use EventBusSubscriber to automatically subscribe events on
    // the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = PokecubeAdv.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void onEntityAttributes(final EntityAttributeCreationEvent event)
        {
            final AttributeSupplier.Builder attribs = LivingEntity.createLivingAttributes()
                    .add(Attributes.FOLLOW_RANGE, 16.0D).add(Attributes.ATTACK_KNOCKBACK)
                    .add(Attributes.MAX_HEALTH, 20.0D);
            event.put(EntityTypes.getTrainer(), attribs.build());
            event.put(EntityTypes.getLeader(), attribs.build());
        }
    }

    public static final String MODID = "pokecube_adventures";

    public static final DeferredBlock<Block> AFA;
    public static final DeferredBlock<Block> COMMANDER;
    public static final DeferredBlock<Block> DAYCARE;
    public static final DeferredBlock<Block> CLONER;
    public static final DeferredBlock<Block> EXTRACTOR;
    public static final DeferredBlock<Block> SPLICER;
    public static final DeferredBlock<Block> SIPHON;
    public static final DeferredBlock<Block> WARP_PAD;
    public static final DeferredBlock<Block> STATUE;
    public static final DeferredBlock<Block> LAB_GLASS;
    public static final DeferredBlock<Block> LAB_GLASS_PANE;

    public static final DeferredItem<Item> EXPSHARE;
    public static final DeferredItem<Item> LINKER;
    public static final DeferredItem<Item> BAG;

    public static final Supplier<BlockEntityType<AfaTile>> AFA_TYPE;
    public static final Supplier<BlockEntityType<CommanderTile>> COMMANDER_TYPE;
    public static final Supplier<BlockEntityType<DaycareTile>> DAYCARE_TYPE;
    public static final Supplier<BlockEntityType<ClonerTile>> CLONER_TYPE;
    public static final Supplier<BlockEntityType<ExtractorTile>> EXTRACTOR_TYPE;
    public static final Supplier<BlockEntityType<SplicerTile>> SPLICER_TYPE;
    public static final Supplier<BlockEntityType<SiphonTile>> SIPHON_TYPE;
    public static final Supplier<BlockEntityType<WarpPadTile>> WARP_PAD_TYPE;
    public static final Supplier<BlockEntityType<StatueEntity>> STATUE_TYPE;

    public static final Supplier<MenuType<AfaContainer>> AFA_CONT;
    public static final Supplier<MenuType<ClonerContainer>> CLONER_CONT;
    public static final Supplier<MenuType<ExtractorContainer>> EXTRACTOR_CONT;
    public static final Supplier<MenuType<SplicerContainer>> SPLICER_CONT;
    public static final Supplier<MenuType<BagContainer>> BAG_CONT;
    public static final Supplier<MenuType<ContainerTrainer>> TRAINER_CONT;

    public static final DeferredRegister.Blocks BLOCKS;
    public static final DeferredRegister.Blocks DECORATIONS;
    public static final DeferredRegister.Items ITEMS;

    public static final DeferredRegister<BlockEntityType<?>> TILES;
    public static final DeferredRegister<MenuType<?>> CONTAINERS;
    public static final DeferredRegister<VillagerProfession> PROFESSIONS;
    public static final DeferredRegister<MemoryModuleType<?>> MEMORIES;
    public static final DeferredRegister<EntityType<?>> ENTITIES;
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS;
    public static final DeferredRegister<DataComponentType<?>> ITEM_DATA;

    public static final Map<PokeType, Item> BADGES = Maps.newHashMap();
    public static final Map<Item, PokeType> BADGEINV = Maps.newHashMap();

    static
    {
        BLOCKS = DeferredRegister.createBlocks(PokecubeAdv.MODID);
        CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
        DECORATIONS = DeferredRegister.createBlocks(PokecubeAdv.MODID);
        ITEMS = DeferredRegister.createItems(PokecubeAdv.MODID);
        TILES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
        PROFESSIONS = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, MODID);
        MEMORIES = DeferredRegister.create(BuiltInRegistries.MEMORY_MODULE_TYPE, MODID);
        ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);
        ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);
        ITEM_DATA = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MODID);

        // Blocks
        AFA = PokecubeAdv.BLOCKS.register("afa", () -> new AfaBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(5.0F, 6.0F)
                        .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));
        COMMANDER = PokecubeAdv.BLOCKS.register("commander", () -> new CommanderBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(5.0F, 6.0F)
                        .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));
        DAYCARE = PokecubeAdv.BLOCKS.register("daycare", () -> new DaycareBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(5.0F, 6.0F)
                        .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));
        CLONER = PokecubeAdv.BLOCKS.register("cloner", () -> new ClonerBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(5.0F, 6.0F)
                        .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));
        EXTRACTOR = PokecubeAdv.BLOCKS.register("extractor", () -> new ExtractorBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(5.0F, 6.0F)
                        .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));
        SPLICER = PokecubeAdv.BLOCKS.register("splicer", () -> new SplicerBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(5.0F, 6.0F)
                        .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));
        SIPHON = PokecubeAdv.BLOCKS.register("siphon", () -> new SiphonBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN).strength(5.0F, 6.0F)
                        .sound(SoundType.NETHERITE_BLOCK)));
        WARP_PAD = PokecubeAdv.BLOCKS.register("warp_pad", () -> new WarpPadBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(5.0F, 6.0F)
                        .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));
        STATUE = PokecubeAdv.BLOCKS.register("statue", () -> new PokemobStatue(
                BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5.0F, 6.0F).sound(SoundType.STONE)
                        .dynamicShape().noOcclusion().requiresCorrectToolForDrops()));
        LAB_GLASS = PokecubeAdv.DECORATIONS.register("laboratory_glass",
                () -> new LaboratoryGlassBlock(DyeColor.LIGHT_BLUE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.3f).noOcclusion()
                                .isValidSpawn(PokecubeItems::never).isRedstoneConductor(PokecubeItems::never)
                                .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                                .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)));
        LAB_GLASS_PANE = PokecubeAdv.DECORATIONS.register("laboratory_glass_pane",
                () -> new LaboratoryGlassPaneBlock(DyeColor.LIGHT_BLUE,
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.3f).noOcclusion()
                                .isValidSpawn(PokecubeItems::never).isRedstoneConductor(PokecubeItems::never)
                                .isSuffocating(PokecubeItems::never).isViewBlocking(PokecubeItems::never)
                                .sound(SoundType.GLASS).instrument(NoteBlockInstrument.HAT)));

        // Items
        EXPSHARE = PokecubeAdv.ITEMS.register("exp_share", () -> new Item(new Item.Properties().stacksTo(1)));
        LINKER = PokecubeAdv.ITEMS.register("linker", () -> new Linker(new Item.Properties().stacksTo(1)));
        BAG = PokecubeAdv.ITEMS.register("bag", () -> new BagItem(new Item.Properties().stacksTo(1)));

        // Tile Entities
        AFA_TYPE = PokecubeAdv.TILES.register("afa",
                () -> BlockEntityType.Builder.of(AfaTile::new, PokecubeAdv.AFA.get()).build(null));
        COMMANDER_TYPE = PokecubeAdv.TILES.register("commander",
                () -> BlockEntityType.Builder.of(CommanderTile::new, PokecubeAdv.COMMANDER.get()).build(null));
        DAYCARE_TYPE = PokecubeAdv.TILES.register("daycare",
                () -> BlockEntityType.Builder.of(DaycareTile::new, PokecubeAdv.DAYCARE.get()).build(null));
        CLONER_TYPE = PokecubeAdv.TILES.register("cloner",
                () -> BlockEntityType.Builder.of(ClonerTile::new, PokecubeAdv.CLONER.get()).build(null));
        EXTRACTOR_TYPE = PokecubeAdv.TILES.register("extractor",
                () -> BlockEntityType.Builder.of(ExtractorTile::new, PokecubeAdv.EXTRACTOR.get()).build(null));
        SPLICER_TYPE = PokecubeAdv.TILES.register("splicer",
                () -> BlockEntityType.Builder.of(SplicerTile::new, PokecubeAdv.SPLICER.get()).build(null));
        SIPHON_TYPE = PokecubeAdv.TILES.register("siphon",
                () -> BlockEntityType.Builder.of(SiphonTile::new, PokecubeAdv.SIPHON.get()).build(null));
        WARP_PAD_TYPE = PokecubeAdv.TILES.register("warp_pad",
                () -> BlockEntityType.Builder.of(WarpPadTile::new, PokecubeAdv.WARP_PAD.get()).build(null));
        STATUE_TYPE = PokecubeAdv.TILES.register("statue",
                () -> BlockEntityType.Builder.of(StatueEntity::new, PokecubeAdv.STATUE.get()).build(null));

        // Containers

        CLONER_CONT = PokecubeAdv.CONTAINERS.register("cloner",
                () -> new MenuType<>(ClonerContainer::new, FeatureFlags.REGISTRY.allFlags()));
        EXTRACTOR_CONT = PokecubeAdv.CONTAINERS.register("extractor",
                () -> new MenuType<>(ExtractorContainer::new, FeatureFlags.REGISTRY.allFlags()));
        SPLICER_CONT = PokecubeAdv.CONTAINERS.register("splicer",
                () -> new MenuType<>(SplicerContainer::new, FeatureFlags.REGISTRY.allFlags()));
        AFA_CONT = PokecubeAdv.CONTAINERS.register("afa",
                () -> new MenuType<>(AfaContainer::new, FeatureFlags.REGISTRY.allFlags()));
        BAG_CONT = PokecubeAdv.CONTAINERS.register("bag",
                () -> new MenuType<>((IContainerFactory<BagContainer>) BagContainer::new,
                        FeatureFlags.REGISTRY.allFlags()));
        TRAINER_CONT = PokecubeAdv.CONTAINERS.register("trainer",
                () -> new MenuType<>((IContainerFactory<ContainerTrainer>) ContainerTrainer::new,
                        FeatureFlags.REGISTRY.allFlags()));
    }

    private static void init()
    {
        // Register the item blocks.
        for (final DeferredHolder<Block, ? extends Block> reg : PokecubeAdv.BLOCKS.getEntries())
        {
            final Item.Properties props = new Item.Properties();
            // Statue does something a bit differently.
            if (reg == PokecubeAdv.STATUE)
                PokecubeAdv.ITEMS.register(reg.getId().getPath(), () -> new StatueItem(reg.get(), props));
            else PokecubeAdv.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), props));
        }

        for (final DeferredHolder<Block, ? extends Block> reg : PokecubeAdv.DECORATIONS.getEntries())
            PokecubeAdv.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()));

        // Initialize advancement triggers
        Triggers.init();

        // Initialize the recipe handlers for genetics stuff.
        RecipeHandlers.init();
    }

    public static final String TRAINERTEXTUREPATH = PokecubeAdv.MODID + ":textures/trainer/";

    private static final String NETVERSION = "2.0.0";
    // Handler for network stuff.
    public static final PacketHandler packets = new PacketHandler(PokecubeAdv.NETVERSION);

    public static final Config config = Config.instance;

    public PokecubeAdv(IEventBus modEventBus, ModContainer modContainer)
    {
        PokecubeAdv.init();

        PokecubeAdv.BLOCKS.register(modEventBus);
        PokecubeAdv.DECORATIONS.register(modEventBus);
        PokecubeAdv.ITEMS.register(modEventBus);
        PokecubeAdv.TILES.register(modEventBus);
        PokecubeAdv.CONTAINERS.register(modEventBus);
        PokecubeAdv.PROFESSIONS.register(modEventBus);
        PokecubeAdv.MEMORIES.register(modEventBus);
        PokecubeAdv.ENTITIES.register(modEventBus);
        PokecubeAdv.ATTACHMENTS.register(modEventBus);
        PokecubeAdv.ITEM_DATA.register(modEventBus);
        Triggers.REGISTER.register(modEventBus);

        modEventBus.addListener(this::loadComplete);

        AdvCreativeTabs.TABS.register(modEventBus);
        RecipePokeAdv.RECIPE_SERIALIZERS.register(modEventBus);
        RecipePokeAdv.RECIPE_TYPES.register(modEventBus);
        PointsOfInterest.REG.register(modEventBus);

        // Register Config stuff
        thut.core.common.config.Config.setupConfigs(modContainer, PokecubeAdv.config, PokecubeCore.MODID,
                PokecubeAdv.MODID);

        // Register event handlers
        SetupHandler.registerListeners();

        Professions.init();
        EntityTypes.init();
        MemoryTypes.init();
        CapabilityHasPokemobs.DefaultPokemobs.init();

        ThutCore.FORGE_BUS.register(this);
        PokecubeAPI.POKEMOB_BUS.register(this);

        InitCaps.registerAttachment(ATTACHMENTS);
        SelectorImpl.registerItemData(ITEM_DATA);

        modEventBus.addListener(EnergyHandler::AttachCaps);
    }

    @SubscribeEvent
    public void registerItems(final RegisterMiscItems event)
    {
        for (final PokeType type : PokeType.values())
        {
            final String name = type.name.equals("???") ? "unknown" : type.name;
            PokecubeAdv.ITEMS.register("badge_" + name.toLowerCase(Locale.ROOT), () -> {
                final Item badge = new Item(new Item.Properties());
                PokecubeAdv.BADGES.put(type, badge);
                PokecubeAdv.BADGEINV.put(badge, type);
                return badge;
            });
        }
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        event.enqueueWork(PointsOfInterest::postInit);
        event.enqueueWork(() -> {
            Professions.postInit();
            CopyMob.register(EntityTypes.getTrainer());
            CopyMob.register(EntityTypes.getLeader());
        });
    }

    /**
     * Register the commands.
     */
    @SubscribeEvent
    public void registerCommands(final RegisterCommandsEvent event)
    {
        CommandConfigs.register(PokecubeAdv.config, event.getDispatcher(), "pokeadvsettings");
    }
}
