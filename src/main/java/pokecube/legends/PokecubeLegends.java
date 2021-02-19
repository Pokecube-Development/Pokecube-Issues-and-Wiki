package pokecube.legends;

import java.util.Random;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
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
import pokecube.adventures.utils.DBLoader;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.events.onload.RegisterPokecubes;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.interfaces.IPokecube.DefaultPokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock.State;
import pokecube.legends.handlers.ForgeEventHandlers;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.Config;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.MoveRegister;
import pokecube.legends.init.PokecubeDim;
import pokecube.legends.init.function.UsableItemGigantShard;
import pokecube.legends.init.function.UsableItemNatureEffects;
import pokecube.legends.init.function.UsableItemZMoveEffects;
import pokecube.legends.tileentity.RaidSpawn;
import pokecube.legends.tileentity.RingTile;
import pokecube.legends.worldgen.trees.Trees;
import thut.api.terrain.BiomeDatabase;

@Mod(value = Reference.ID)
public class PokecubeLegends
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Block> BLOCKS     = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Block> BLOCKS_TAB = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Block> DECORATION_TAB = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Item>  ITEMS      = DeferredRegister.create(ForgeRegistries.ITEMS,
            Reference.ID);

    public static ResourceLocation FUELTAG       = new ResourceLocation(Reference.ID, "fuel");

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

            // Register the fossil stone spawning.
            if (PokecubeLegends.config.generateOres)
            {
                final Predicate<RegistryKey<Biome>> check = k -> BiomeDatabase.contains(k, "FOREST") || BiomeDatabase
                        .contains(k, "OCEAN") || BiomeDatabase.contains(k, "HILLS") || BiomeDatabase.contains(k,
                                "PLAINS") || BiomeDatabase.contains(k, "SWAMP") || BiomeDatabase.contains(k, "MOUNTAIN")
                        || BiomeDatabase.contains(k, "SNOWY") || BiomeDatabase.contains(k, "SPOOKY");
                WorldgenHandler.get(Reference.ID).register(check, GenerationStage.Decoration.UNDERGROUND_ORES,
                        Feature.ORE.withConfiguration(new OreFeatureConfig(
                                OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, BlockInit.RUBY_ORE.get()
                                        .getDefaultState(), 5)).range(32).square().func_242731_b(2));

                WorldgenHandler.get(Reference.ID).register(check, GenerationStage.Decoration.UNDERGROUND_ORES,
                        Feature.ORE.withConfiguration(new OreFeatureConfig(
                                OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, BlockInit.SAPPHIRE_ORE.get()
                                        .getDefaultState(), 5)).range(32).square().func_242731_b(2));
            }
            Trees.register();
        }

        @SubscribeEvent
        public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event)
        {
            RaidSpawn.TYPE = TileEntityType.Builder.create(RaidSpawn::new, BlockInit.RAID_SPAWN.get()).build(null);
            RingTile.TYPE = TileEntityType.Builder.create(RingTile::new, BlockInit.BLOCK_PORTALWARP.get()).build(null);
            event.getRegistry().register(RaidSpawn.TYPE.setRegistryName(BlockInit.RAID_SPAWN.get().getRegistryName()));
            event.getRegistry().register(RingTile.TYPE.setRegistryName(BlockInit.BLOCK_PORTALWARP.get()
                    .getRegistryName()));
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

        DBLoader.trainerDatabases.add(new ResourceLocation(Reference.ID, "database/trainer/trainers.json"));
        DBLoader.tradeDatabases.add(new ResourceLocation(Reference.ID, "database/trainer/trades.json"));

        WorldgenHandler.spawnPresets.add(new ResourceLocation(Reference.ID, "database/trainer/spawn_presets.json"));

        modEventBus.addListener(this::loadComplete);

        new WorldgenHandler(Reference.ID, modEventBus);

        PokecubeLegends.BLOCKS.register(modEventBus);
        PokecubeLegends.ITEMS.register(modEventBus);
        PokecubeLegends.BLOCKS_TAB.register(modEventBus);
        PokecubeLegends.DECORATION_TAB.register(modEventBus);

        BlockInit.init();
        ItemInit.init();
        MoveRegister.init();
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        BlockInit.strippableBlocks(event);
        BlockInit.compostables();
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
        public ItemStack createIcon()
        {
            return new ItemStack(BlockInit.ULTRA_MAGNETIC.get());
        }
    };

    public static final ItemGroup DECO_TAB = new ItemGroup("decotab")
    {

        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(BlockInit.SKY_BRICK.get());
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
        PokecubeLegends.config.onUpdated();
    }

    @SubscribeEvent
    public void reactivate_raid(final RightClickBlock event)
    {
        if (event.getWorld().isRemote) return;
        if (event.getItemStack().getItem() != ItemInit.WISHING_PIECE.get()) return;
        final BlockState hit = event.getWorld().getBlockState(event.getPos());
        if (hit.getBlock() != BlockInit.RAID_SPAWN.get())
        {
            if (hit.getBlock() == PokecubeItems.DYNABLOCK.get()) event.getPlayer().sendMessage(
                    new TranslationTextComponent("msg.notaraidspot.info"), Util.DUMMY_UUID);
            return;
        }
        final boolean active = hit.get(RaidSpawnBlock.ACTIVE).active();
        if (active) return;
        else
        {
            final State state = new Random().nextInt(20) == 0 ? State.RARE : State.NORMAL;
            event.getWorld().setBlockState(event.getPos(), hit.with(RaidSpawnBlock.ACTIVE, state));
            event.setUseItem(Result.ALLOW);
            event.getItemStack().grow(-1);
        }
    }
}
