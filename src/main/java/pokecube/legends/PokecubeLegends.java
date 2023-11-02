package pokecube.legends;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
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
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.init.RegisterMiscItems;
import pokecube.api.events.init.RegisterPokecubes;
import pokecube.api.items.IPokecube.DefaultPokecubeBehaviour;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock.State;
import pokecube.legends.blocks.properties.Compostables;
import pokecube.legends.blocks.properties.Flammables;
import pokecube.legends.blocks.properties.Strippables;
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
import pokecube.legends.init.function.UsableItemGigantShard;
import pokecube.legends.init.function.UsableItemNatureEffects;
import pokecube.legends.init.function.UsableItemZMoveEffects;
import pokecube.legends.recipes.LegendsDistorticRecipeManager;
import pokecube.legends.recipes.LegendsLootingRecipeManager;
import pokecube.legends.worldgen.UltraSpaceSurfaceRules;
import pokecube.legends.worldgen.WorldgenFeatures;
import pokecube.legends.worldgen.trees.Trees;
import thut.api.block.flowing.FlowingBlock;
import thut.api.entity.CopyCaps;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

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
    public static ResourceLocation TOTEM_FUEL_TAG = new ResourceLocation(Reference.ID, "totem_fuel");

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID)
    public static class RegistryHandler
    {
        @SubscribeEvent
        public static void onEntityAttributes(final EntityAttributeCreationEvent event)
        {
            final AttributeSupplier.Builder attribs = LivingEntity.createLivingAttributes()
                    .add(SharedAttributes.MOB_SIZE_SCALE.get());
            event.put(EntityInit.WORMHOLE.get(), attribs.build());
        }
    }

    public static final Config config = new Config();

    public PokecubeLegends()
    {

        thut.core.common.config.Config.setupConfigs(PokecubeLegends.config, PokecubeCore.MODID, Reference.ID);
        ThutCore.FORGE_BUS.register(this);
        PokecubeAPI.POKEMOB_BUS.register(this);

        ThutCore.FORGE_BUS.register(new ForgeEventHandlers());

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

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
        BlockInit.init();
        ContainerInit.init();
        EntityInit.init();
        FeaturesInit.init(modEventBus);
        FluidInit.init();
        ItemHelperEffect.init();
        ItemInit.init();
        MoveRegister.init();
        TileEntityInit.init();
        Trees.init(modEventBus);

        LegendsDistorticRecipeManager.init();
        LegendsLootingRecipeManager.init();

        UltraSpaceSurfaceRules.init();

        PokecubeAdv.TAB_DECORATIONS = TAB_DECORATIONS;
    }

    @SuppressWarnings("deprecation")
    private void initBiomeDicts()
    {

        // Biome Dictionary
        net.minecraftforge.common.BiomeDictionary.Type ULTRASPACE = net.minecraftforge.common.BiomeDictionary.Type
                .getType("ultraspace");
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.AQUAMARINE_CAVES,
                net.minecraftforge.common.BiomeDictionary.Type.RARE, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.AZURE_BADLANDS,
                net.minecraftforge.common.BiomeDictionary.Type.DRY, net.minecraftforge.common.BiomeDictionary.Type.MESA,
                net.minecraftforge.common.BiomeDictionary.Type.SANDY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.BLINDING_DELTAS,
                net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.WET, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.BURNT_BEACH,
                net.minecraftforge.common.BiomeDictionary.Type.BEACH,
                net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.CORRUPTED_CAVES,
                net.minecraftforge.common.BiomeDictionary.Type.RARE,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.CRYSTALLIZED_BEACH,
                net.minecraftforge.common.BiomeDictionary.Type.BEACH,
                net.minecraftforge.common.BiomeDictionary.Type.HOT, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.DEAD_OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.DEAD,
                net.minecraftforge.common.BiomeDictionary.Type.OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.DEEP_DEAD_OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.DEAD,
                net.minecraftforge.common.BiomeDictionary.Type.OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.DEEP_FROZEN_DEAD_OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.DEAD,
                net.minecraftforge.common.BiomeDictionary.Type.OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.DEEP_FROZEN_POLLUTED_OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.DEEP_POLLUTED_OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.DEAD_RIVER,
                net.minecraftforge.common.BiomeDictionary.Type.DEAD,
                net.minecraftforge.common.BiomeDictionary.Type.RIVER,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.DISTORTED_LANDS,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.DRIED_BLINDING_DELTAS,
                net.minecraftforge.common.BiomeDictionary.Type.DRY, net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.DRIPSTONE_CAVES,
                net.minecraftforge.common.BiomeDictionary.Type.RARE, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.ERODED_AZURE_BADLANDS,
                net.minecraftforge.common.BiomeDictionary.Type.HOT, net.minecraftforge.common.BiomeDictionary.Type.DRY,
                net.minecraftforge.common.BiomeDictionary.Type.MESA,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN,
                net.minecraftforge.common.BiomeDictionary.Type.RARE,
                net.minecraftforge.common.BiomeDictionary.Type.SANDY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FORBIDDEN_GROVE,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.CONIFEROUS,
                net.minecraftforge.common.BiomeDictionary.Type.FOREST,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FORBIDDEN_MEADOW,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.PLAINS,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FORBIDDEN_TAIGA,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.CONIFEROUS,
                net.minecraftforge.common.BiomeDictionary.Type.FOREST,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FROZEN_DEAD_OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.DEAD,
                net.minecraftforge.common.BiomeDictionary.Type.OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FROZEN_DEAD_RIVER,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.DEAD,
                net.minecraftforge.common.BiomeDictionary.Type.RIVER,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FROZEN_PEAKS,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FROZEN_POLLUTED_OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FROZEN_POLLUTED_RIVER,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.RIVER,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FUNGAL_FLOWER_FOREST,
                net.minecraftforge.common.BiomeDictionary.Type.FOREST,
                net.minecraftforge.common.BiomeDictionary.Type.HILLS,
                net.minecraftforge.common.BiomeDictionary.Type.MUSHROOM,
                net.minecraftforge.common.BiomeDictionary.Type.RARE, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FUNGAL_FOREST,
                net.minecraftforge.common.BiomeDictionary.Type.FOREST,
                net.minecraftforge.common.BiomeDictionary.Type.MUSHROOM, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FUNGAL_PLAINS,
                net.minecraftforge.common.BiomeDictionary.Type.PLAINS,
                net.minecraftforge.common.BiomeDictionary.Type.MUSHROOM, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.FUNGAL_SUNFLOWER_PLAINS,
                net.minecraftforge.common.BiomeDictionary.Type.PLAINS,
                net.minecraftforge.common.BiomeDictionary.Type.MUSHROOM,
                net.minecraftforge.common.BiomeDictionary.Type.RARE, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.JAGGED_PEAKS,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.MAGMATIC_BLINDING_DELTAS,
                net.minecraftforge.common.BiomeDictionary.Type.DRY, net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.METEORITE_SPIKES,
                net.minecraftforge.common.BiomeDictionary.Type.DRY, net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.MIRAGE_DESERT,
                net.minecraftforge.common.BiomeDictionary.Type.DRY, net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.SANDY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.OLD_GROWTH_FORBIDDEN_TAIGA,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.CONIFEROUS,
                net.minecraftforge.common.BiomeDictionary.Type.DENSE,
                net.minecraftforge.common.BiomeDictionary.Type.FOREST,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.RARE,
                net.minecraftforge.common.BiomeDictionary.Type.SPARSE, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.POLLUTED_OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.OCEAN,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.POLLUTED_RIVER,
                net.minecraftforge.common.BiomeDictionary.Type.RIVER,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.ROCKY_MIRAGE_DESERT,
                net.minecraftforge.common.BiomeDictionary.Type.DRY, net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.SANDY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.SHATTERED_BLINDING_DELTAS,
                net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN,
                net.minecraftforge.common.BiomeDictionary.Type.RARE,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.WET, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.SHATTERED_TAINTED_BARRENS,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN,
                net.minecraftforge.common.BiomeDictionary.Type.SPARSE,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.SWAMP,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND,
                net.minecraftforge.common.BiomeDictionary.Type.WET, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.SMALL_DISTORTED_ISLANDS,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.SNOWY_CRYSTALLIZED_BEACH,
                net.minecraftforge.common.BiomeDictionary.Type.BEACH,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.SNOWY_FORBIDDEN_TAIGA,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.CONIFEROUS,
                net.minecraftforge.common.BiomeDictionary.Type.FOREST,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.SNOWY_FUNGAL_PLAINS,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.PLAINS,
                net.minecraftforge.common.BiomeDictionary.Type.MUSHROOM,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.SNOWY_SLOPES,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.SPARSE_TEMPORAL_JUNGLE,
                net.minecraftforge.common.BiomeDictionary.Type.DENSE,
                net.minecraftforge.common.BiomeDictionary.Type.HILLS,
                net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.JUNGLE,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.RARE,
                net.minecraftforge.common.BiomeDictionary.Type.SPARSE,
                net.minecraftforge.common.BiomeDictionary.Type.WET, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.TAINTED_BARRENS,
                net.minecraftforge.common.BiomeDictionary.Type.SPARSE,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY,
                net.minecraftforge.common.BiomeDictionary.Type.SWAMP,
                net.minecraftforge.common.BiomeDictionary.Type.WASTELAND,
                net.minecraftforge.common.BiomeDictionary.Type.WET, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.TEMPORAL_BAMBOO_JUNGLE,
                net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.JUNGLE,
                net.minecraftforge.common.BiomeDictionary.Type.LUSH,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.RARE, net.minecraftforge.common.BiomeDictionary.Type.WET,
                ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.TEMPORAL_JUNGLE,
                net.minecraftforge.common.BiomeDictionary.Type.DENSE,
                net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.JUNGLE,
                net.minecraftforge.common.BiomeDictionary.Type.LUSH,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.WET, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.ULTRA_STONY_PEAKS,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.ULTRA_STONY_SHORE,
                net.minecraftforge.common.BiomeDictionary.Type.BEACH,
                net.minecraftforge.common.BiomeDictionary.Type.HOT, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.WINDSWEPT_FORBIDDEN_TAIGA,
                net.minecraftforge.common.BiomeDictionary.Type.COLD,
                net.minecraftforge.common.BiomeDictionary.Type.CONIFEROUS,
                net.minecraftforge.common.BiomeDictionary.Type.FOREST,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN,
                net.minecraftforge.common.BiomeDictionary.Type.RARE,
                net.minecraftforge.common.BiomeDictionary.Type.SNOWY, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.WINDSWEPT_TEMPORAL_JUNGLE,
                net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.JUNGLE,
                net.minecraftforge.common.BiomeDictionary.Type.LUSH,
                net.minecraftforge.common.BiomeDictionary.Type.MAGICAL,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN,
                net.minecraftforge.common.BiomeDictionary.Type.RARE, net.minecraftforge.common.BiomeDictionary.Type.WET,
                ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.WOODED_AZURE_BADLANDS,
                net.minecraftforge.common.BiomeDictionary.Type.DRY, net.minecraftforge.common.BiomeDictionary.Type.MESA,
                net.minecraftforge.common.BiomeDictionary.Type.PLATEAU,
                net.minecraftforge.common.BiomeDictionary.Type.SANDY,
                net.minecraftforge.common.BiomeDictionary.Type.SPARSE, ULTRASPACE);
        net.minecraftforge.common.BiomeDictionary.addTypes(FeaturesInit.VOLCANIC_BLINDING_DELTAS,
                net.minecraftforge.common.BiomeDictionary.Type.DRY, net.minecraftforge.common.BiomeDictionary.Type.HOT,
                net.minecraftforge.common.BiomeDictionary.Type.MOUNTAIN,
                net.minecraftforge.common.BiomeDictionary.Type.RARE,
                net.minecraftforge.common.BiomeDictionary.Type.SPOOKY, ULTRASPACE);
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        event.enqueueWork(() -> {
            Compostables.registerDefaults();
            Flammables.registerDefaults();
            Strippables.registerDefaults();
            Tillables.registerDefaults();
            CopyCaps.register(EntityInit.WORMHOLE.get());

            SpawnHandler.MELT_GETTER = () -> BlockInit.METEORITE_MOLTEN_BLOCK.get().defaultBlockState();
            SpawnHandler.DUST_GETTER = () -> BlockInit.ASH.get().defaultBlockState().setValue(FlowingBlock.LAYERS, 5);

            initBiomeDicts();
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
    public void registerItems(final RegisterMiscItems event)
    {
        ItemInit.registerItems();
    }

    @SubscribeEvent
    public void registerPokecubes(final RegisterPokecubes event)
    {
        final PokecubeDim helper = new PokecubeDim();

        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.dyna(mob);
            }
        }.setName("dyna"));
        EntityPokecubeBase.CUBE_SIZES.put(new ResourceLocation("pokecube", "dynacube"), 0.75f);
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.beast(mob);
            }
        }.setName("beast"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.clone(mob);
            }
        }.setName("clone"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.typingB(mob);
            }
        }.setName("typing"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.teamAqua(mob);
            }
        }.setName("teamaqua"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.teamMagma(mob);
            }
        }.setName("teammagma"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.teamR(mob);
            }
        }.setName("rocket"));
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
            if (hit.getBlock() == PokecubeItems.DYNAMAX.get()) thut.lib.ChatHelper.sendSystemMessage(event.getPlayer(),
                    TComponent.translatable("msg.notaraidspot.info"));
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
