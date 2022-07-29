package pokecube.legends;

import java.util.Optional;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.serialization.Codec;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.events.onload.RegisterPokecubes;
import pokecube.core.interfaces.IPokecube.DefaultPokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock.State;
import pokecube.legends.blocks.properties.Tillables;
import pokecube.legends.entity.WormholeEntity;
import pokecube.legends.handlers.EventsHandler;
import pokecube.legends.handlers.ForgeEventHandlers;
import pokecube.legends.handlers.ItemHelperEffect;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.Config;
import pokecube.legends.init.ContainerInit;
import pokecube.legends.init.EntityInit;
import pokecube.legends.init.FeaturesInit;
import pokecube.legends.init.FluidInit;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.MoveRegister;
import pokecube.legends.init.PokecubeDim;
import pokecube.legends.init.TileEntityInit;
import pokecube.legends.init.function.RaidCapture;
import pokecube.legends.init.function.UsableItemGigantShard;
import pokecube.legends.init.function.UsableItemNatureEffects;
import pokecube.legends.init.function.UsableItemZMoveEffects;
import pokecube.legends.recipes.LegendsDistorticRecipeManager;
import pokecube.legends.recipes.LegendsLootingRecipeManager;
import pokecube.legends.tileentity.RaidSpawn;
import pokecube.legends.tileentity.RingTile;
import pokecube.legends.worldgen.UltraSpaceSurfaceRules;
import pokecube.legends.worldgen.WorldgenFeatures;
import pokecube.legends.worldgen.trees.Trees;
import thut.core.common.ThutCore;

@Mod(value = Reference.ID)
public class PokecubeLegends
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Block> DECORATION_TAB = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Block> DIMENSIONS_TAB = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Block> NO_TAB = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.ID);
    public static final DeferredRegister<Block> POKECUBE_BLOCKS_TAB = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES,
            Reference.ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Reference.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister
            .create(ForgeRegistries.PARTICLE_TYPES, Reference.ID);

    // Barrels Inventory/Container
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITIES, Reference.ID);
    public static final DeferredRegister<MenuType<?>> MENU = DeferredRegister.create(ForgeRegistries.CONTAINERS,
            Reference.ID);

    // Features, etc
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister
            .create(Registry.CONFIGURED_FEATURE_REGISTRY, Reference.ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister
            .create(Registry.PLACED_FEATURE_REGISTRY, Reference.ID);
    public static final DeferredRegister<Codec<? extends RuleSource>> SURFACE_RULES = DeferredRegister
            .create(Registry.RULE_REGISTRY, Reference.ID);

    // Recipes
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, Reference.ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister
            .create(Registry.RECIPE_TYPE_REGISTRY, Reference.ID);

    /** Packs Textures,Tags,etc... */
    public static ResourceLocation FUELTAG = new ResourceLocation(Reference.ID, "fuel");

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID)
    public static class RegistryHandler
    {
        @SubscribeEvent
        public static void onItemRegister(final RegistryEvent.Register<Item> event)
        {
            ItemInit.registerItems(event);
        }

        @SubscribeEvent
        public static void registerFeatures(final RegistryEvent.Register<Feature<?>> event)
        {
            PokecubeCore.LOGGER.debug("Registering Pokecube Legends Features");

            // Register the ruby and sapphire ores
        }

        @SubscribeEvent
        public static void registerTiles(final RegistryEvent.Register<BlockEntityType<?>> event)
        {
            RaidSpawn.TYPE = BlockEntityType.Builder.of(RaidSpawn::new, BlockInit.RAID_SPAWNER.get()).build(null);
            RingTile.TYPE = BlockEntityType.Builder.of(RingTile::new, BlockInit.PORTAL.get()).build(null);
            event.getRegistry()
                    .register(RaidSpawn.TYPE.setRegistryName(BlockInit.RAID_SPAWNER.get().getRegistryName()));
            event.getRegistry().register(RingTile.TYPE.setRegistryName(BlockInit.PORTAL.get().getRegistryName()));
        }

        @SubscribeEvent
        public static void onEntityAttributes(final EntityAttributeCreationEvent event)
        {
            final AttributeSupplier.Builder attribs = LivingEntity.createLivingAttributes();
            event.put(EntityInit.WORMHOLE.get(), attribs.build());
        }
    }

    public static final Config config = new Config();

    public PokecubeLegends()
    {

        thut.core.common.config.Config.setupConfigs(PokecubeLegends.config, PokecubeCore.MODID, Reference.ID);
        MinecraftForge.EVENT_BUS.register(this);
        PokecubeCore.POKEMOB_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());

        PokecubeCore.POKEMOB_BUS.addListener(RaidCapture::CatchPokemobRaid);
        PokecubeCore.POKEMOB_BUS.addListener(RaidCapture::PostCatchPokemobRaid);

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::loadCommon);
        modEventBus.addListener(this::loadComplete);

        EventsHandler.register();

        PokecubeLegends.MENU.register(modEventBus);
        PokecubeLegends.ENTITIES.register(modEventBus);
        PokecubeLegends.FLUIDS.register(modEventBus);
        PokecubeLegends.ITEMS.register(modEventBus);
        PokecubeLegends.RECIPE_SERIALIZER.register(modEventBus);
        PokecubeLegends.RECIPE_TYPE.register(modEventBus);
        PokecubeLegends.PARTICLES.register(modEventBus);
        PokecubeLegends.TILES.register(modEventBus);

        PokecubeLegends.DECORATION_TAB.register(modEventBus);
        PokecubeLegends.DIMENSIONS_TAB.register(modEventBus);
        PokecubeLegends.NO_TAB.register(modEventBus);
        PokecubeLegends.POKECUBE_BLOCKS_TAB.register(modEventBus);

        PokecubeLegends.CONFIGURED_FEATURES.register(modEventBus);
        PokecubeLegends.PLACED_FEATURES.register(modEventBus);
        PokecubeLegends.SURFACE_RULES.register(modEventBus);

        WorldgenFeatures.init(modEventBus);
        Trees.init(modEventBus);
        FeaturesInit.init(modEventBus);
        BlockInit.init();
        ItemInit.init();
        MoveRegister.init();
        EntityInit.init();
        ItemHelperEffect.init();
        ContainerInit.init();
        FluidInit.init();

        TileEntityInit.init();

        LegendsDistorticRecipeManager.init();
        LegendsLootingRecipeManager.init();

        UltraSpaceSurfaceRules.init();

        PokecubeAdv.TAB_DECORATIONS = TAB_DECORATIONS;
    }

    private void loadCommon(final FMLCommonSetupEvent event)
    {
        Tillables.tillables(event);
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        BlockInit.strippableBlocks(event);

        event.enqueueWork(() -> {
            BlockInit.compostables();
            BlockInit.flammables();

            // Biome Dictionary
            BiomeDictionary.addTypes(FeaturesInit.AQUAMARINE_CAVES, Type.RARE);
            BiomeDictionary.addTypes(FeaturesInit.AZURE_BADLANDS, Type.DRY, Type.MESA, Type.SANDY);
            BiomeDictionary.addTypes(FeaturesInit.BLINDING_DELTAS, Type.HOT, Type.SPOOKY, Type.WET);
            BiomeDictionary.addTypes(FeaturesInit.BURNT_BEACH, Type.BEACH, Type.HOT, Type.SPOOKY, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.CORRUPTED_CAVES, Type.RARE, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.CRYSTALLIZED_BEACH, Type.BEACH, Type.HOT);
            BiomeDictionary.addTypes(FeaturesInit.DEAD_OCEAN, Type.DEAD, Type.OCEAN, Type.SPOOKY, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.DEEP_DEAD_OCEAN, Type.DEAD, Type.OCEAN, Type.SPOOKY, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.DEEP_FROZEN_DEAD_OCEAN, Type.COLD, Type.DEAD, Type.OCEAN, Type.SNOWY,
                    Type.SPOOKY, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.DEEP_FROZEN_POLLUTED_OCEAN, Type.COLD, Type.OCEAN, Type.SNOWY,
                    Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.DEEP_POLLUTED_OCEAN, Type.OCEAN, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.DEAD_RIVER, Type.DEAD, Type.RIVER, Type.SPOOKY, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.DISTORTED_LANDS, Type.MAGICAL, Type.SPOOKY);
            BiomeDictionary.addTypes(FeaturesInit.DRIED_BLINDING_DELTAS, Type.DRY, Type.HOT, Type.SPOOKY);
            BiomeDictionary.addTypes(FeaturesInit.DRIPSTONE_CAVES, Type.RARE);
            BiomeDictionary.addTypes(FeaturesInit.ERODED_AZURE_BADLANDS, Type.HOT, Type.DRY, Type.MESA, Type.MOUNTAIN,
                    Type.RARE, Type.SANDY);
            BiomeDictionary.addTypes(FeaturesInit.FORBIDDEN_GROVE, Type.COLD, Type.CONIFEROUS, Type.FOREST,
                    Type.MAGICAL, Type.MOUNTAIN, Type.SNOWY);
            BiomeDictionary.addTypes(FeaturesInit.FORBIDDEN_MEADOW, Type.COLD, Type.PLAINS, Type.MAGICAL, Type.MOUNTAIN,
                    Type.SNOWY);
            BiomeDictionary.addTypes(FeaturesInit.FORBIDDEN_TAIGA, Type.COLD, Type.CONIFEROUS, Type.FOREST,
                    Type.MAGICAL);
            BiomeDictionary.addTypes(FeaturesInit.FROZEN_DEAD_OCEAN, Type.COLD, Type.DEAD, Type.OCEAN, Type.SNOWY,
                    Type.SPOOKY, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.FROZEN_DEAD_RIVER, Type.COLD, Type.DEAD, Type.RIVER, Type.SNOWY,
                    Type.SPOOKY, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.FROZEN_PEAKS, Type.COLD, Type.MOUNTAIN, Type.SNOWY);
            BiomeDictionary.addTypes(FeaturesInit.FROZEN_POLLUTED_OCEAN, Type.COLD, Type.OCEAN, Type.SNOWY,
                    Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.FROZEN_POLLUTED_RIVER, Type.COLD, Type.RIVER, Type.SNOWY,
                    Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.FUNGAL_FLOWER_FOREST, Type.FOREST, Type.HILLS, Type.MUSHROOM,
                    Type.RARE);
            BiomeDictionary.addTypes(FeaturesInit.FUNGAL_FOREST, Type.FOREST, Type.MUSHROOM);
            BiomeDictionary.addTypes(FeaturesInit.FUNGAL_PLAINS, Type.PLAINS, Type.MUSHROOM);
            BiomeDictionary.addTypes(FeaturesInit.FUNGAL_SUNFLOWER_PLAINS, Type.PLAINS, Type.MUSHROOM, Type.RARE);
            BiomeDictionary.addTypes(FeaturesInit.JAGGED_PEAKS, Type.COLD, Type.MOUNTAIN, Type.SNOWY);
            BiomeDictionary.addTypes(FeaturesInit.MAGMATIC_BLINDING_DELTAS, Type.DRY, Type.HOT, Type.SPOOKY);
            BiomeDictionary.addTypes(FeaturesInit.METEORITE_SPIKES, Type.DRY, Type.HOT, Type.SPOOKY, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.MIRAGE_DESERT, Type.DRY, Type.HOT, Type.MAGICAL, Type.SANDY);
            BiomeDictionary.addTypes(FeaturesInit.OLD_GROWTH_FORBIDDEN_TAIGA, Type.COLD, Type.CONIFEROUS, Type.DENSE,
                    Type.FOREST, Type.MAGICAL, Type.RARE, Type.SPARSE);
            BiomeDictionary.addTypes(FeaturesInit.POLLUTED_OCEAN, Type.OCEAN, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.POLLUTED_RIVER, Type.RIVER, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.ROCKY_MIRAGE_DESERT, Type.DRY, Type.HOT, Type.MAGICAL, Type.SANDY);
            BiomeDictionary.addTypes(FeaturesInit.SHATTERED_BLINDING_DELTAS, Type.HOT, Type.MOUNTAIN, Type.RARE,
                    Type.SPOOKY, Type.WET);
            BiomeDictionary.addTypes(FeaturesInit.SHATTERED_TAINTED_BARRENS, Type.MOUNTAIN, Type.SPARSE, Type.SPOOKY,
                    Type.SWAMP, Type.WASTELAND, Type.WET);
            BiomeDictionary.addTypes(FeaturesInit.SMALL_DISTORTED_ISLANDS, Type.MAGICAL, Type.SPOOKY);
            BiomeDictionary.addTypes(FeaturesInit.SNOWY_CRYSTALLIZED_BEACH, Type.BEACH, Type.COLD, Type.SNOWY);
            BiomeDictionary.addTypes(FeaturesInit.SNOWY_FORBIDDEN_TAIGA, Type.COLD, Type.CONIFEROUS, Type.FOREST,
                    Type.MAGICAL, Type.SNOWY);
            BiomeDictionary.addTypes(FeaturesInit.SNOWY_FUNGAL_PLAINS, Type.COLD, Type.PLAINS, Type.MUSHROOM,
                    Type.SNOWY, Type.WASTELAND);
            BiomeDictionary.addTypes(FeaturesInit.SNOWY_SLOPES, Type.COLD, Type.MOUNTAIN, Type.SNOWY);
            BiomeDictionary.addTypes(FeaturesInit.SPARSE_TEMPORAL_JUNGLE, Type.DENSE, Type.HILLS, Type.HOT, Type.JUNGLE,
                    Type.MAGICAL, Type.RARE, Type.SPARSE, Type.WET);
            BiomeDictionary.addTypes(FeaturesInit.TAINTED_BARRENS, Type.SPARSE, Type.SPOOKY, Type.SWAMP, Type.WASTELAND,
                    Type.WET);
            BiomeDictionary.addTypes(FeaturesInit.TEMPORAL_BAMBOO_JUNGLE, Type.HOT, Type.JUNGLE, Type.LUSH,
                    Type.MAGICAL, Type.RARE, Type.WET);
            BiomeDictionary.addTypes(FeaturesInit.TEMPORAL_JUNGLE, Type.DENSE, Type.HOT, Type.JUNGLE, Type.LUSH,
                    Type.MAGICAL, Type.WET);
            BiomeDictionary.addTypes(FeaturesInit.ULTRA_STONY_PEAKS, Type.COLD, Type.MOUNTAIN);
            BiomeDictionary.addTypes(FeaturesInit.ULTRA_STONY_SHORE, Type.BEACH, Type.HOT);
            BiomeDictionary.addTypes(FeaturesInit.WINDSWEPT_FORBIDDEN_TAIGA, Type.COLD, Type.CONIFEROUS, Type.FOREST,
                    Type.MAGICAL, Type.MOUNTAIN, Type.RARE, Type.SNOWY);
            BiomeDictionary.addTypes(FeaturesInit.WINDSWEPT_TEMPORAL_JUNGLE, Type.HOT, Type.JUNGLE, Type.LUSH,
                    Type.MAGICAL, Type.MOUNTAIN, Type.RARE, Type.WET);
            BiomeDictionary.addTypes(FeaturesInit.WOODED_AZURE_BADLANDS, Type.DRY, Type.MESA, Type.PLATEAU, Type.SANDY,
                    Type.SPARSE);
            BiomeDictionary.addTypes(FeaturesInit.VOLCANIC_BLINDING_DELTAS, Type.DRY, Type.HOT, Type.MOUNTAIN,
                    Type.RARE, Type.SPOOKY);
        });
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(final AttachCapabilitiesEvent<ItemStack> event)
    {
        UsableItemNatureEffects.registerCapabilities(event);
        UsableItemZMoveEffects.registerCapabilities(event);
        UsableItemGigantShard.registerCapabilities(event);
    }

    public static final CreativeModeTab TAB_DIMENSIONS = new CreativeModeTab("ultratab")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(BlockInit.DISTORTIC_GRASS_BLOCK.get());
        }
    };

    public static final CreativeModeTab TAB_DECORATIONS = new CreativeModeTab("decotab")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(BlockInit.SKY_BRICKS.get());
        }
    };

    public static final CreativeModeTab TAB_LEGENDS = new CreativeModeTab("legendtab")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(ItemInit.RAINBOW_ORB.get());
        }
    };

    @SubscribeEvent
    public void registerPokecubes(final RegisterPokecubes event)
    {
        final PokecubeDim helper = new PokecubeDim();

        // Here we do some stuff to supress the annoying forge warnings
        // about "dangerous alternative prefixes.
        String namespace = Reference.ID;
        String prefix = ModLoadingContext.get().getActiveNamespace();
        ModContainer old = ModLoadingContext.get().getActiveContainer();
        if (!prefix.equals(namespace))
        {
            Optional<? extends ModContainer> swap = ModList.get().getModContainerById(namespace);
            if (swap.isPresent()) ModLoadingContext.get().setActiveContainer(swap.get());
        }

        event.behaviors.add(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.dyna(mob);
            }
        }.setRegistryName(Reference.ID, "dyna"));
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

        // Undo the suppression for the prefixes.
        if (old != ModLoadingContext.get().getActiveContainer())
        {
            ModLoadingContext.get().setActiveContainer(old);
        }
    }

    @SubscribeEvent
    public void serverStarting(final ServerStartingEvent event)
    {
        PokecubeLegends.config.loaded = true;
        WormholeEntity.clear();
        PokecubeLegends.config.onUpdated();
    }

    @SubscribeEvent
    public void reactivate_raid(final RightClickBlock event)
    {
        if (event.getWorld().isClientSide) return;
        if (event.getItemStack().getItem() != ItemInit.WISHING_PIECE.get()) return;
        final BlockState hit = event.getWorld().getBlockState(event.getPos());
        if (hit.getBlock() != BlockInit.RAID_SPAWNER.get())
        {
            if (hit.getBlock() == PokecubeItems.DYNAMAX.get())
                event.getPlayer().sendMessage(new TranslatableComponent("msg.notaraidspot.info"), Util.NIL_UUID);
            return;
        }
        final boolean active = hit.getValue(RaidSpawnBlock.ACTIVE).active();
        if (active) return;
        else
        {
            final State state = ThutCore.newRandom().nextInt(20) == 0 ? State.RARE : State.NORMAL;
            event.getWorld().setBlockAndUpdate(event.getPos(), hit.setValue(RaidSpawnBlock.ACTIVE, state));
            event.setUseItem(Result.ALLOW);
            if (!event.getPlayer().isCreative()) event.getItemStack().grow(-1);
        }
    }
}
