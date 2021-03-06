package pokecube.legends;

import java.util.Random;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
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
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
import pokecube.legends.init.EntityInit;
import pokecube.legends.init.FeaturesInit;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.MoveRegister;
import pokecube.legends.init.PokecubeDim;
import pokecube.legends.init.function.UsableItemGigantShard;
import pokecube.legends.init.function.UsableItemNatureEffects;
import pokecube.legends.init.function.UsableItemZMoveEffects;
import pokecube.legends.recipes.LegendsDistorticRecipeManager;
import pokecube.legends.recipes.LegendsLootingRecipeManager;
import pokecube.legends.tileentity.RaidSpawn;
import pokecube.legends.tileentity.RingTile;
import pokecube.legends.worldgen.trees.Trees;
import thut.api.terrain.BiomeDatabase;

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
    public static final DeferredRegister<Item>          ITEMS          = DeferredRegister.create(ForgeRegistries.ITEMS,
            Reference.ID);
	public static final DeferredRegister<Fluid>  		FLUIDS         = DeferredRegister.create(ForgeRegistries.FLUIDS,
            Reference.ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES       = DeferredRegister.create(ForgeRegistries.ENTITIES,
    		Reference.ID);

    //Recipes
    public static final DeferredRegister<IRecipeSerializer<?>> LEGENDS_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS,
    		Reference.ID);

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
            final Predicate<RegistryKey<Biome>> check = k -> PokecubeLegends.config.generateOres && (BiomeDatabase
                    .contains(k, "FOREST") || BiomeDatabase.contains(k, "OCEAN") || BiomeDatabase.contains(k, "HILLS")
                    || BiomeDatabase.contains(k, "PLAINS") || BiomeDatabase.contains(k, "SWAMP") || BiomeDatabase
                            .contains(k, "MOUNTAIN") || BiomeDatabase.contains(k, "SNOWY") || BiomeDatabase.contains(k,
                                    "SPOOKY"));

            WorldgenHandler.INSTANCE.register(check, GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE
                    .configured(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, BlockInit.RUBY_ORE
                            .get().defaultBlockState(), 5)).range(32).squared().count(2),
                    new ResourceLocation("pokecube_legends:ruby_ore"));
            WorldgenHandler.INSTANCE.register(check, GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE
                    .configured(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                            BlockInit.SAPPHIRE_ORE.get().defaultBlockState(), 5)).range(32).squared().count(2),
                    new ResourceLocation("pokecube_legends:sapphire_ore"));

            Trees.register();
        }

        @SubscribeEvent
        public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event)
        {
            RaidSpawn.TYPE = TileEntityType.Builder.of(RaidSpawn::new, BlockInit.RAID_SPAWN.get()).build(null);
            RingTile.TYPE = TileEntityType.Builder.of(RingTile::new, BlockInit.BLOCK_PORTALWARP.get()).build(null);
            event.getRegistry().register(RaidSpawn.TYPE.setRegistryName(BlockInit.RAID_SPAWN.get().getRegistryName()));
            event.getRegistry().register(RingTile.TYPE.setRegistryName(BlockInit.BLOCK_PORTALWARP.get()
                    .getRegistryName()));
        }

        @SubscribeEvent
        public static void onEntityAttributes(final EntityAttributeCreationEvent event)
        {
            final AttributeModifierMap.MutableAttribute attribs = LivingEntity.createLivingAttributes();
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

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::loadComplete);

        PokecubeLegends.BLOCKS.register(modEventBus);
        PokecubeLegends.ITEMS.register(modEventBus);
        PokecubeLegends.BLOCKS_TAB.register(modEventBus);
        PokecubeLegends.DECORATION_TAB.register(modEventBus);
		PokecubeLegends.FLUIDS.register(modEventBus);
        PokecubeLegends.ENTITIES.register(modEventBus);
        PokecubeLegends.LEGENDS_SERIALIZERS.register(modEventBus);

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

    public static final ItemGroup TAB = new ItemGroup("ultratab")
    {

        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(BlockInit.ULTRA_MAGNETIC.get());
        }
    };

    public static final ItemGroup DECO_TAB = new ItemGroup("decotab")
    {

        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(BlockInit.SKY_BRICK.get());
        }
    };

    public static final ItemGroup LEGEND_TAB = new ItemGroup("legendtab")
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
        if (hit.getBlock() != BlockInit.RAID_SPAWN.get())
        {
            if (hit.getBlock() == PokecubeItems.DYNABLOCK.get()) event.getPlayer().sendMessage(
                    new TranslationTextComponent("msg.notaraidspot.info"), Util.NIL_UUID);
            return;
        }
        final boolean active = hit.getValue(RaidSpawnBlock.ACTIVE).active();
        if (active) return;
        else
        {
            final State state = new Random().nextInt(20) == 0 ? State.RARE : State.NORMAL;
            event.getWorld().setBlockAndUpdate(event.getPos(), hit.setValue(RaidSpawnBlock.ACTIVE, state));
            event.setUseItem(Result.ALLOW);
            if (!event.getPlayer().isCreative()) event.getItemStack().grow(-1);
        }
    }
}
