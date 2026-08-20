package pokecube.gimmicks.secret_bases;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.init.CoreCreativeTabs;
import pokecube.core.moves.implementations.MovesAdder;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.gimmicks.secret_bases.blocks.BaseBlock;
import pokecube.gimmicks.secret_bases.blocks.BaseTile;
import pokecube.gimmicks.secret_bases.command.SecretBase;
import pokecube.gimmicks.secret_bases.dimension.SecretBaseDimension;
import pokecube.gimmicks.secret_bases.moves.ActionSecretPower;
import thut.api.attachments.Ownable;
import thut.api.entity.teleporting.TeleDest;
import thut.api.entity.teleporting.ThutTeleporter;
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
        MovesAdder.worldActionPackages.add(ActionSecretPower.class.getPackage());
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

    @SubscribeEvent
    public static void onWorldTick(final LevelTickEvent.Pre event)
    {
        final Level world = event.getLevel();
        if (world.getWorldBorder().getSize() != SecretBaseDimension.WORLDSIZE
                && world.dimension().compareTo(SecretBaseDimension.WORLD_KEY) == 0)
            world.getWorldBorder().setSize(SecretBaseDimension.WORLDSIZE);
    }

    @SubscribeEvent
    public static void onWorldLoad(final LevelEvent.Load event)
    {
        final Level world = (Level) event.getLevel();
        if (world.getWorldBorder().getSize() != SecretBaseDimension.WORLDSIZE
                && world.dimension().compareTo(SecretBaseDimension.WORLD_KEY) == 0)
            world.getWorldBorder().setSize(SecretBaseDimension.WORLDSIZE);
    }

    @SubscribeEvent
    public static void onEnterChunk(final EntityEvent.EnteringSection event)
    {
        final Level world = event.getEntity().level;
        // Only wrap in secret bases, only if chunk changes, and only server
        // side.
        if (world.dimension() != SecretBaseDimension.WORLD_KEY || !event.didChunkChange() || world.isClientSide)
            return;

        SectionPos newPos = event.getNewPos();
        SectionPos oldPos = event.getOldPos();

        boolean moveX = oldPos.getX() != newPos.getX();
        boolean moveZ = oldPos.getZ() != newPos.getZ();
        if (!(moveX || moveZ)) return;

        int x = newPos.getX() / 16;
        int z = newPos.getZ() / 16;

        final int dx = newPos.getX() % 16;
        final int dz = newPos.getZ() % 16;

        // Middle of base, don't care
        if (dx == 0 && dz == 0) return;

        if (dx > 0) if (dx < 8) x += 1;
        if (dx < 0) if (dx < -7) x -= 1;

        if (dz > 0) if (dz < 8) z += 1;
        if (dz < 0) if (dz < -7) z -= 1;

        final ChunkPos nearestBase = new ChunkPos(x << 4, z << 4);

        // We need to shunt it back to nearest valid point.
        final AABB chunkBox = getBaseBox(nearestBase);

        final BlockPos mob = event.getEntity().blockPosition();

        double nx = mob.getX();
        double nz = mob.getZ();

        if (moveX)
        {
            if (nx <= chunkBox.minX + 1)
            {
                nx = chunkBox.maxX - 1;
                if (chunkBox.maxX < 0)
                {
                    nx -= 2;
                }
            }
            else if (nx >= chunkBox.maxX - 1)
            {
                nx = chunkBox.minX + 1;
            }
        }
        if (moveZ)
        {
            if (nz <= chunkBox.minZ + 1)
            {
                nz = chunkBox.maxZ - 1;
                if (chunkBox.maxZ < 0)
                {
                    nz -= 2;
                }
            }
            else if (nz >= chunkBox.maxZ - 1)
            {
                nz = chunkBox.minZ + 1;
            }
        }
        final BlockPos pos = new BlockPos((int) nx, mob.getY(), (int) nz);

        final TeleDest dest = new TeleDest().setPos(GlobalPos.of(world.dimension(), pos));
        EventsHandler.Schedule(world, w -> {
            event.getEntity().setDeltaMovement(0, 0, 0);
            ThutTeleporter.transferTo(event.getEntity(), dest);
            return true;
        });
    }

    private static AABB getBaseBox(final ChunkPos nearestBase)
    {
        final BlockPos pos1 = nearestBase.getWorldPosition();
        final BlockPos pos2 = pos1.offset(16, 255, 16);
        return AABB.encapsulatingFullBlocks(pos1, pos2);
    }
}
