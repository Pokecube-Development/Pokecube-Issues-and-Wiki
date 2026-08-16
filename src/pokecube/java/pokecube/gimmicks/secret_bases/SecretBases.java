package pokecube.gimmicks.secret_bases;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.init.CoreCreativeTabs;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.gimmicks.secret_bases.blocks.BaseBlock;
import pokecube.gimmicks.secret_bases.blocks.BaseTile;
import pokecube.gimmicks.secret_bases.command.SecretBase;
import pokecube.gimmicks.secret_bases.dimension.SecretBaseDimension;
import thut.api.attachments.Ownable;
import thut.lib.RegHelper;

import java.util.function.Supplier;

@Mod(value = PokecubeCore.MODID)
@EventBusSubscriber(modid = PokecubeCore.MODID)
public class SecretBases
{
    public static final DeferredRegister.Blocks BLOCKS;
    public static final DeferredRegister.Items ITEMS;
    public static final DeferredRegister<BlockEntityType<?>> TILES;
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNKGEN;

    public static final Supplier<BlockEntityType<?>> BASE_TYPE;
    public static final DeferredBlock<Block> SECRET_BASE;
    public static final Supplier<MapCodec<SecretBaseDimension.SecretChunkGenerator>> SECRET_BASEGEN;
    static
    {
        // Setup the DeferredRegisters
        BLOCKS = DeferredRegister.createBlocks(PokecubeCore.MODID);
        ITEMS = DeferredRegister.createItems(PokecubeCore.MODID);
        TILES = DeferredRegister.create(RegHelper.BLOCK_ENTITY_TYPE_REGISTRY, PokecubeCore.MODID);
        CHUNKGEN = DeferredRegister.create(BuiltInRegistries.CHUNK_GENERATOR, PokecubeCore.MODID);

        // Register the block
        SECRET_BASE = BLOCKS.register("secret_base",
                        () -> new BaseBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                                .requiresCorrectToolForDrops().strength(2000).sound(SoundType.STONE)
                                .instrument(NoteBlockInstrument.BASEDRUM)));
        // Then register the item
        ITEMS.register(SECRET_BASE.getId().getPath(), () -> new BlockItem(SECRET_BASE.get(), new Item.Properties()));

        BASE_TYPE = TILES.register("secret_base",
                () -> BlockEntityType.Builder.of(BaseTile::new, SECRET_BASE.get()).build(null));
        SECRET_BASEGEN = CHUNKGEN.register("secret_base",
                () -> SecretBaseDimension.SecretChunkGenerator.CODEC);
    }

    public SecretBases(IEventBus bus)
    {
        // Register the DeferredRegisters
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        CHUNKGEN.register(bus);
        PokecubeItems.DEFAULT_OWNABLE_TE.add(SecretBase.class);
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event)
    {
        // Register as an ownable block
        Ownable.TILES.add(BASE_TYPE.get());

        PacketPokedex.RADAR_SUPPLIERS.put("_bases_", player -> {
            final ServerLevel level = player.serverLevel().getLevel();
            final BlockPos pos = player.blockPosition();
            final GlobalPos here = GlobalPos.of(level.dimension(), pos);
            return SecretBaseDimension.getNearestBases(here, PokecubeCore.getConfig().baseRadarRange);
        });
    }

    @SubscribeEvent
    public static void onCommandRegister(final RegisterCommandsEvent event)
    {
        SecretBase.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTab().equals(CoreCreativeTabs.BLOCKS_ITEMS_TAB.get()))
        {
            CoreCreativeTabs.addAfter(event, PokecubeItems.DEEPSLATE_FOSSIL_ORE, SECRET_BASE);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            CoreCreativeTabs.addAfter(event, Items.LODESTONE, SECRET_BASE);
        }
    }
}
