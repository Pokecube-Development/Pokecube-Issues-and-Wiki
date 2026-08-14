package pokecube.legends;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.fluids.DispenseFluidContainer;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.init.RegisterMiscItems;
import pokecube.api.events.init.RegisterPokecubes;
import pokecube.api.items.IPokecube.DefaultPokecubeBehaviour;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.gimmicks.dynamax.DynamaxHelper;
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
import pokecube.legends.init.EntityInit;
import pokecube.legends.init.FeaturesInit;
import pokecube.legends.init.FluidInit;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.LegendsCreativeTabs;
import pokecube.legends.init.MoveRegister;
import pokecube.legends.init.PokecubeDim;
import pokecube.legends.init.TileEntityInit;
import pokecube.legends.init.function.UsableItemGigantShard;
import pokecube.legends.init.function.UsableItemNatureEffects;
import pokecube.legends.init.function.UsableItemZMoveEffects;
import pokecube.legends.recipes.LegendsDistorticRecipeManager;
import pokecube.legends.recipes.LegendsLootingRecipeManager;
import pokecube.legends.spawns.WormholeSpawns;
import pokecube.legends.worldgen.UltraSpaceSurfaceRules;
import pokecube.legends.worldgen.WorldgenFeatures;
import pokecube.legends.worldgen.trees.Trees;
import thut.api.attachments.CopyMob;
import thut.api.block.flowing.FlowingBlock;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

@Mod(value = Reference.ID)
public class PokecubeLegends
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
            BuiltInRegistries.ENTITY_TYPE, Reference.ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, Reference.ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(
            NeoForgeRegistries.Keys.FLUID_TYPES, Reference.ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Reference.ID);
    public static final DeferredRegister.Blocks NO_ITEM_BLOCKS = DeferredRegister.createBlocks(Reference.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Reference.ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(
            BuiltInRegistries.PARTICLE_TYPE, Reference.ID);

    // Barrels Inventory/Container
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Reference.ID);
    public static final DeferredRegister<MenuType<?>> MENU = DeferredRegister.create(BuiltInRegistries.MENU,
            Reference.ID);

    // Features, etc
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(
            RegHelper.CONFIGURED_FEATURE_REGISTRY, Reference.ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(
            RegHelper.PLACED_FEATURE_REGISTRY, Reference.ID);
    public static final DeferredRegister<MapCodec<? extends RuleSource>> SURFACE_RULES = DeferredRegister.create(
            RegHelper.RULE_REGISTRY, Reference.ID);

    // Recipes
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(
            BuiltInRegistries.RECIPE_SERIALIZER, Reference.ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(
            RegHelper.RECIPE_TYPE_REGISTRY, Reference.ID);

    // Data

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(
            NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Reference.ID);
    //    public static final DeferredRegister<DataComponentType<?>> ITEM_DATA= DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Reference.MODID);

    /** Packs Textures,Tags,etc... */
    public static ResourceLocation TOTEM_FUEL_TAG = ResourceLocation.fromNamespaceAndPath(Reference.ID, "totem_fuel");

    @EventBusSubscriber(modid = Reference.ID)
    public static class RegistryHandler
    {
        @SubscribeEvent
        public static void onEntityAttributes(final EntityAttributeCreationEvent event)
        {
            final AttributeSupplier.Builder attribs = LivingEntity.createLivingAttributes();
            event.put(EntityInit.WORMHOLE.get(), attribs.build());
        }
    }

    public static final Config config = new Config();

    public PokecubeLegends(IEventBus modEventBus, ModContainer modContainer)
    {
        thut.core.common.config.Config.setupConfigs(modContainer, PokecubeLegends.config, PokecubeCore.MODID,
                Reference.ID);
        ThutCore.FORGE_BUS.register(this);
        PokecubeAPI.POKEMOB_BUS.register(this);

        ThutCore.FORGE_BUS.register(new ForgeEventHandlers());

        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::commonSetup);

        EventsHandler.register();

        PokecubeLegends.MENU.register(modEventBus);
        PokecubeLegends.ENTITIES.register(modEventBus);
        PokecubeLegends.FLUIDS.register(modEventBus);
        PokecubeLegends.FLUID_TYPES.register(modEventBus);
        PokecubeLegends.BLOCKS.register(modEventBus);
        PokecubeLegends.NO_ITEM_BLOCKS.register(modEventBus);
        PokecubeLegends.ITEMS.register(modEventBus);
        PokecubeLegends.RECIPE_SERIALIZER.register(modEventBus);
        PokecubeLegends.RECIPE_TYPE.register(modEventBus);
        PokecubeLegends.PARTICLES.register(modEventBus);
        PokecubeLegends.TILES.register(modEventBus);
        PokecubeLegends.ATTACHMENTS.register(modEventBus);

        PokecubeLegends.CONFIGURED_FEATURES.register(modEventBus);
        PokecubeLegends.PLACED_FEATURES.register(modEventBus);
        PokecubeLegends.SURFACE_RULES.register(modEventBus);
        LegendsCreativeTabs.TABS.register(modEventBus);

        WorldgenFeatures.init(modEventBus);
        BlockInit.init();
        EntityInit.init(modEventBus);
        FeaturesInit.init(modEventBus);
        FluidInit.init();
        ItemHelperEffect.init();
        ItemInit.init();
        MoveRegister.init();
        TileEntityInit.init();
        Trees.init(modEventBus);
        WormholeSpawns.init();
        UsableItemGigantShard.init();
        UsableItemNatureEffects.init();
        UsableItemZMoveEffects.init();

        LegendsDistorticRecipeManager.init();
        LegendsLootingRecipeManager.init();

        UltraSpaceSurfaceRules.init();
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        event.enqueueWork(() -> {
            Compostables.registerDefaults();
            Flammables.registerDefaults();
            Strippables.registerDefaults();
            Tillables.registerDefaults();
            CopyMob.register(EntityInit.WORMHOLE.get());

            SpawnHandler.MELT_GETTER = () -> BlockInit.METEORITE_MOLTEN_BLOCK.get().defaultBlockState();
            SpawnHandler.DUST_GETTER = () -> BlockInit.ASH.get().defaultBlockState().setValue(FlowingBlock.LAYERS, 5);

            DispenserBlock.registerBehavior(ItemInit.DISTORTIC_WATER_BUCKET.get(),
                    DispenseFluidContainer.getInstance());
        });
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        // Add Interactions for sources
        FluidInteractionRegistry.addInteraction(NeoForgeMod.LAVA_TYPE.value(),
                new FluidInteractionRegistry.InteractionInformation(FluidInit.DISTORTIC_WATER_TYPE.get(), fluidState ->
                        fluidState.isSource()
                                ? Blocks.OBSIDIAN.defaultBlockState()
                                : BlockInit.DISTORTIC_STONE.get().defaultBlockState()));

        FluidInteractionRegistry.addInteraction(NeoForgeMod.WATER_TYPE.value(),
                new FluidInteractionRegistry.InteractionInformation(FluidInit.DISTORTIC_WATER_TYPE.get(), fluidState ->
                        fluidState.isSource()
                                ? Blocks.PACKED_ICE.defaultBlockState()
                                : Blocks.ICE.defaultBlockState()));

        FluidInteractionRegistry.addInteraction(FluidInit.DISTORTIC_WATER_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation(
                        (level, currentPos, relativePos, currentState) -> level.getFluidState(currentPos).isSource()
                                && level.getBlockState(currentPos.below()).is(Blocks.SNOW_BLOCK),
                        Blocks.BLUE_ICE.defaultBlockState()));

        FluidInteractionRegistry.addInteraction(FluidInit.DISTORTIC_WATER_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation(
                        (level, currentPos, relativePos, currentState) -> !level.getFluidState(currentPos).isSource()
                                && level.getBlockState(currentPos.below()).is(BlockInit.DISTORTIC_MIRROR.get())
                                && level.getBlockState(relativePos).is(BlockInit.DISTORTIC_GRASS_BLOCK.get()),
                        BlockInit.CRACKED_DISTORTIC_STONE.get().defaultBlockState()));

        FluidInteractionRegistry.addInteraction(NeoForgeMod.WATER_TYPE.value(),
                new FluidInteractionRegistry.InteractionInformation(
                        (level, currentPos, relativePos, currentState) -> !level.getFluidState(currentPos).isSource()
                                && (level.getBlockState(currentPos.below()).is(BlockInit.CORRUPTED_DIRT.get())
                                || level.getBlockState(currentPos.below()).is(BlockInit.CORRUPTED_COARSE_DIRT.get()))
                                && level.getBlockState(relativePos).is(BlockInit.ULTRA_DARKSTONE.get()),
                        BlockInit.DUSK_DOLERITE.get().defaultBlockState()));
    }

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
        EntityPokecubeBase.CUBE_SIZES.put(ResourceLocation.fromNamespaceAndPath("pokecube", "dynacube"), 0.75f);
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
        if (event.getLevel().isClientSide) return;
        if (event.getItemStack().getItem() != ItemInit.WISHING_PIECE.get()) return;
        final BlockState hit = event.getLevel().getBlockState(event.getPos());
        if (hit.getBlock() != BlockInit.RAID_SPAWNER.get())
        {
            if (hit.getBlock() == DynamaxHelper.DYNAMAX.get()) thut.lib.ChatHelper.sendSystemMessage(event.getEntity(),
                    Component.translatable("msg.notaraidspot.info"));
            return;
        }
        final boolean active = hit.getValue(RaidSpawnBlock.ACTIVE).active();
        if (!active)
        {
            final State state = ThutCore.newRandom().nextInt(20) == 0 ? State.RARE : State.NORMAL;
            event.getLevel().setBlockAndUpdate(event.getPos(), hit.setValue(RaidSpawnBlock.ACTIVE, state));
            event.setUseItem(TriState.TRUE);
            if (!event.getEntity().isCreative()) event.getItemStack().grow(-1);
        }
    }
}
