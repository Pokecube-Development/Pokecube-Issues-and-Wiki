package pokecube.legends;

import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.events.onload.RegisterPokecubes;
import pokecube.core.interfaces.IPokecube.DefaultPokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock.State;
import pokecube.legends.entity.WormholeEntity;
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
import pokecube.legends.worldgen.trees.Trees;
import thut.api.terrain.BiomeDatabase;
import thut.core.common.ThutCore;

@Mod(value = Reference.ID)
public class PokecubeLegends
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Block>         BLOCKS         = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Block>         BLOCKS_TAB     = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Block>         DECORATION_TAB = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Block>         NO_TAB         = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Item>          ITEMS          = DeferredRegister.create(ForgeRegistries.ITEMS,
            Reference.ID);
    public static final DeferredRegister<Fluid>         FLUIDS         = DeferredRegister.create(ForgeRegistries.FLUIDS,
            Reference.ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES       = DeferredRegister.create(
            ForgeRegistries.ENTITIES, Reference.ID);

    // Barrels Inventory/Container
    public static final DeferredRegister<BlockEntityType<?>> TILES     = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITIES, Reference.ID);
    public static final DeferredRegister<MenuType<?>>        CONTAINER = DeferredRegister.create(
            ForgeRegistries.CONTAINERS, Reference.ID);

    // Recipes
    public static final DeferredRegister<RecipeSerializer<?>> LEGENDS_SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, Reference.ID);

    /** Packs Textures,Tags,etc... */
    public static ResourceLocation FUELTAG = new ResourceLocation(Reference.ID, "fuel");
    //

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
            final Predicate<ResourceKey<Biome>> check = k -> PokecubeLegends.config.generateOres && (BiomeDatabase
                    .contains(k, "FOREST") || BiomeDatabase.contains(k, "OCEAN") || BiomeDatabase.contains(k, "HILLS")
                    || BiomeDatabase.contains(k, "PLAINS") || BiomeDatabase.contains(k, "SWAMP") || BiomeDatabase
                            .contains(k, "MOUNTAIN") || BiomeDatabase.contains(k, "SNOWY") || BiomeDatabase.contains(k,
                                    "SPOOKY"));

            WorldgenHandler.INSTANCE.register(check, GenerationStep.Decoration.UNDERGROUND_ORES, Feature.ORE.configured(
                    new OreConfiguration(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, BlockInit.RUBY_ORE.get()
                            .defaultBlockState(), 5)).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(31))
                    .squared().count(2), new ResourceLocation("pokecube_legends:ruby_ore"));
            WorldgenHandler.INSTANCE.register(check, GenerationStep.Decoration.UNDERGROUND_ORES, Feature.ORE.configured(
                    new OreConfiguration(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, BlockInit.SAPPHIRE_ORE.get()
                            .defaultBlockState(), 5)).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(31))
                    .squared().count(2), new ResourceLocation("pokecube_legends:sapphire_ore"));

            Trees.register();
        }

        @SubscribeEvent
        public static void registerTiles(final RegistryEvent.Register<BlockEntityType<?>> event)
        {
            RaidSpawn.TYPE = BlockEntityType.Builder.of(RaidSpawn::new, BlockInit.RAID_SPAWNER.get()).build(null);
            RingTile.TYPE = BlockEntityType.Builder.of(RingTile::new, BlockInit.PORTAL.get()).build(null);
            event.getRegistry().register(RaidSpawn.TYPE.setRegistryName(BlockInit.RAID_SPAWNER.get().getRegistryName()));
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

        modEventBus.addListener(this::loadComplete);

        PokecubeLegends.BLOCKS.register(modEventBus);
        PokecubeLegends.ITEMS.register(modEventBus);
        PokecubeLegends.BLOCKS_TAB.register(modEventBus);
        PokecubeLegends.DECORATION_TAB.register(modEventBus);
        PokecubeLegends.NO_TAB.register(modEventBus);
        PokecubeLegends.FLUIDS.register(modEventBus);
        PokecubeLegends.ENTITIES.register(modEventBus);
        PokecubeLegends.LEGENDS_SERIALIZERS.register(modEventBus);
        PokecubeLegends.TILES.register(modEventBus);
        PokecubeLegends.CONTAINER.register(modEventBus);

        // Biomes Dictionary
        BiomeDictionary.addTypes(FeaturesInit.BIOME_UB1, Type.MAGICAL, Type.FOREST, Type.MUSHROOM);
        BiomeDictionary.addTypes(FeaturesInit.BIOME_UB2, Type.JUNGLE, Type.FOREST, Type.DENSE);
        BiomeDictionary.addTypes(FeaturesInit.BIOME_UB3, Type.SANDY, Type.WASTELAND, Type.HOT);
        BiomeDictionary.addTypes(FeaturesInit.BIOME_UB4, Type.HILLS, Type.DEAD, Type.SPOOKY);
        BiomeDictionary.addTypes(FeaturesInit.BIOME_UB5, Type.COLD, Type.CONIFEROUS, Type.SNOWY);
        BiomeDictionary.addTypes(FeaturesInit.BIOME_UB6, Type.MAGICAL, Type.FOREST, Type.SPARSE);

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
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        BlockInit.strippableBlocks(event);
        BlockInit.compostables();
        BlockInit.flammables();
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(final AttachCapabilitiesEvent<ItemStack> event)
    {
        UsableItemNatureEffects.registerCapabilities(event);
        UsableItemZMoveEffects.registerCapabilities(event);
        UsableItemGigantShard.registerCapabilities(event);
    }

    public static final CreativeModeTab TAB = new CreativeModeTab("ultratab")
    {

        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(BlockInit.ULTRA_MAGNETIC.get());
        }
    };

    public static final CreativeModeTab DECO_TAB = new CreativeModeTab("decotab")
    {

        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(BlockInit.SKY_BRICK.get());
        }
    };

    public static final CreativeModeTab LEGEND_TAB = new CreativeModeTab("legendtab")
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
    }

    @SubscribeEvent
    public void serverStarting(final FMLServerStartingEvent event)
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
            if (hit.getBlock() == PokecubeItems.DYNABLOCK.get()) event.getPlayer().sendMessage(
                    new TranslatableComponent("msg.notaraidspot.info"), Util.NIL_UUID);
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
