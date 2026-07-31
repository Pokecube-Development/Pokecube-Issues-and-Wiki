package pokecube.world.dimension;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.entity.teleporting.TeleDest;
import thut.api.entity.teleporting.ThutTeleporter;
import thut.api.maths.Vector3;
import thut.lib.RegHelper;
import thut.lib.TComponent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SecretBaseDimension
{
    public static void onConstruct(final IEventBus bus)
    {
        DimensionRegister.CHUNKGEN.register(bus);
    }

    public static void sendToBase(final ServerPlayer player, final UUID baseOwner)
    {
        final GlobalPos pos = SecretBaseDimension.getSecretBaseLoc(baseOwner, player.getServer(), true);
        final Vector3 v = new Vector3().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(pos, v), true);
        thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokecube.secretbase.enter"));
    }

    public static void sendToExit(final ServerPlayer player, final UUID baseOwner)
    {
        final GlobalPos pos = SecretBaseDimension.getSecretBaseLoc(baseOwner, player.getServer(), false);
        final Vector3 v = new Vector3().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(pos, v), true);
        thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokecube.secretbase.exit"));
    }

    public static void setSecretBasePoint(final ServerPlayer player, final GlobalPos gpos, final boolean inBase)
    {
        final CompoundTag tag = PokecubePlayerDataHandler.getCustomDataTag(player);
        final BlockPos pos = gpos.pos();

        if (inBase)
        {
            final CompoundTag exit = new CompoundTag();
            exit.putInt("x", pos.getX());
            exit.putInt("y", pos.getY());
            exit.putInt("z", pos.getZ());
            tag.put("secret_base_internal", exit);
        }
        else
        {
            final Tag exit = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, gpos).getOrThrow();
            if (tag.contains("secret_base_exit"))
            {
                final CompoundTag exito = tag.getCompound("secret_base_exit");
                GlobalPos old = null;
                try
                {
                    old = GlobalPos.CODEC.decode(NbtOps.INSTANCE, exito).result().get().getFirst();
                }
                catch (final Exception e)
                {
                    old = GlobalPos.of(Level.OVERWORLD,
                            new BlockPos(exito.getInt("x"), exito.getInt("y"), exito.getInt("z")));
                }
                final GlobalPos orig = old;
                PokecubeSerializer.getInstance().bases.removeIf(
                        c -> orig.dimension().location().equals(c.dimension().location()) && orig.pos()
                                .equals(c.pos()));
            }
            tag.put("secret_base_exit", exit);
            PokecubeSerializer.getInstance().bases.add(gpos);
        }
    }

    public static ChunkPos getFromIndex(final int index)
    {
        final int scale = 16 << 10;
        final int shift = scale / 2;
        int x = index % scale - shift;
        int z = index / scale - shift;
        x *= 16;
        z *= 16;
        return new ChunkPos(x, z);
    }

    public static int fromChunkPos(final ChunkPos pos)
    {
        // First convert to secret base coords
        int x = pos.x / 16;
        int z = pos.z / 16;

        final int dx = pos.x % 16;
        final int dz = pos.z % 16;

        // and ensure we wrap correctly
        if (dx > 0) if (dx < 8) x += 1;
        if (dx < 0) if (dx < -7) x -= 1;

        if (dz > 0) if (dz < 8) z += 1;
        if (dz < 0) if (dz < -7) z -= 1;

        final int scale = 16 << 10;
        final int shift = scale / 2;
        x += shift;
        z += shift;
        return x + z * scale;
    }

    public static GlobalPos getSecretBaseLoc(final UUID player, final MinecraftServer server, final boolean inBase)
    {
        final CompoundTag tag = PokecubePlayerDataHandler.getCustomDataTag(server.registryAccess(), player.toString());
        if (inBase)
        {
            if (tag.contains("secret_base_internal"))
            {
                final CompoundTag exit = tag.getCompound("secret_base_internal");
                return GlobalPos.of(SecretBaseDimension.WORLD_KEY,
                        new BlockPos(exit.getInt("x"), exit.getInt("y"), exit.getInt("z")));
            }
            int index;
            if (!tag.contains("secret_base_index"))
            {
                index = PokecubeSerializer.getInstance().customData.getInt("next_base_index");
                tag.putInt("secret_base_index", index);
                PokecubeSerializer.getInstance().customData.putInt("next_base_index", index + 1);
            }
            else index = tag.getInt("secret_base_index");
            final ChunkPos chunk = SecretBaseDimension.getFromIndex(index);
            return GlobalPos.of(SecretBaseDimension.WORLD_KEY,
                    new BlockPos((chunk.x << 4) + 8, 64, (chunk.z << 4) + 8));
        }
        else if (!tag.contains("secret_base_exit"))
            return GlobalPos.of(Level.OVERWORLD, server.getLevel(Level.OVERWORLD).getSharedSpawnPos());
        else
        {
            final CompoundTag exit = tag.getCompound("secret_base_exit");
            try
            {
                return GlobalPos.CODEC.decode(NbtOps.INSTANCE, exit).result().get().getFirst();
            }
            catch (final Exception e)
            {
                return GlobalPos.of(Level.OVERWORLD,
                        new BlockPos(exit.getInt("x"), exit.getInt("y"), exit.getInt("z")));
            }
        }
    }

    public static class SecretChunkGenerator extends ChunkGenerator
    {
        public static final MapCodec<SecretChunkGenerator> CODEC = RecordCodecBuilder.mapCodec((builder) -> {
            return builder.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(m -> m.biomeSource))
                    .apply(builder, SecretChunkGenerator::new);
        });

        BlockState[] states = new BlockState[256];

        public SecretChunkGenerator(BiomeSource biomes)
        {
            super(biomes);
        }

        @Override
        protected MapCodec<? extends ChunkGenerator> codec()
        {
            return CODEC;
        }

        @Override
        public int getBaseHeight(final int x, final int z, final Types heightmapType,
                final LevelHeightAccessor p_156156_, RandomState p_223211_)
        {
            return 64;
        }

        @Override
        public NoiseColumn getBaseColumn(final int x, final int z, final LevelHeightAccessor p_156152_,
                RandomState p_223211_)
        {
            return new NoiseColumn(0, this.states);
        }

        @Override
        public void applyCarvers(WorldGenRegion p_187691_, long p_187692_, RandomState p_223211_,
                BiomeManager p_187693_, StructureManager p_187694_, ChunkAccess p_187695_, Carving p_187696_)
        {

        }

        @Override
        public void buildSurface(WorldGenRegion p_187697_, StructureManager p_187698_, RandomState p_223211_,
                ChunkAccess p_187699_)
        {

        }

        @Override
        public void spawnOriginalMobs(WorldGenRegion p_62167_)
        {

        }

        @Override
        public int getGenDepth()
        {
            return 384;
        }

        @Override
        public void addDebugScreenInfo(List<String> info, RandomState rng, BlockPos pos)
        {
            // TODO include owner of secret base?
        }

        @Override
        public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                StructureManager structureManager, ChunkAccess chunk)
        {
            final ChunkPos pos = chunk.getPos();
            final boolean stone = pos.x % 16 == 0 && pos.z % 16 == 0;
            final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            BlockState state = Blocks.STONE.defaultBlockState();
            final Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
            final Heightmap heightmap1 = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
            for (int i = 0; i < 16; i++)
                for (int k = 0; k < 16; k++)
                {
                    chunk.setBlockState(blockpos$mutableblockpos.set(i, 0, k), Blocks.BARRIER.defaultBlockState(),
                            false);
                    if (stone) for (int j = 57; j < 64; j++)
                    {
                        state = j < 64 && j > 57 && k > 3 && k < 12 && i > 3 && i < 12
                                ? Blocks.STONE.defaultBlockState()
                                : Blocks.AIR.defaultBlockState();
                        chunk.setBlockState(blockpos$mutableblockpos.set(i, j, k), state, false);
                        if (j < 64)
                        {
                            heightmap.update(i, j, k, state);
                            heightmap1.update(i, j, k, state);
                        }
                    }
                }
            return CompletableFuture.completedFuture(chunk);
        }

        @Override
        public int getSeaLevel()
        {
            return 63;
        }

        @Override
        public int getMinY()
        {
            return 0;
        }
    }

    public static final String ID = PokecubeCore.MODID + ":secret_base";

    private static final ResourceLocation IDLOC = ResourceLocation.parse(SecretBaseDimension.ID);

    public static final ResourceKey<Level> WORLD_KEY = ResourceKey.create(RegHelper.DIMENSION_REGISTRY,
            SecretBaseDimension.IDLOC);
    public static final ResourceKey<Biome> BIOME_KEY = ResourceKey.create(RegHelper.BIOME_REGISTRY,
            SecretBaseDimension.IDLOC);

    public static final double WORLDSIZE = 2 * 2999984;

    @EventBusSubscriber(modid = PokecubeCore.MODID)
    public static class DimensionRegister
    {
        public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNKGEN = DeferredRegister.create(
                BuiltInRegistries.CHUNK_GENERATOR, PokecubeCore.MODID);
        public static final Supplier<MapCodec<SecretChunkGenerator>> SECRET_BASE = CHUNKGEN.register("secret_base",
                () -> SecretChunkGenerator.CODEC);

        @SubscribeEvent
        public static void dummy(FMLLoadCompleteEvent event)
        {

        }
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientEventHandler
    {

        @SubscribeEvent
        public static void onClientTick(final ClientTickEvent.Pre event)
        {
            final Level world = PokecubeCore.proxy.getWorld();
            var A = DimensionRegister.SECRET_BASE;
            if (world == null) return;
            if (world.getWorldBorder().getSize() != WORLDSIZE
                    && world.dimension().compareTo(SecretBaseDimension.WORLD_KEY) == 0)
                world.getWorldBorder().setSize(WORLDSIZE);
        }
    }

    @EventBusSubscriber
    public static class EventHandler
    {

        @SubscribeEvent
        public static void onWorldTick(final LevelTickEvent.Pre event)
        {
            final Level world = event.getLevel();
            if (world.getWorldBorder().getSize() != WORLDSIZE
                    && world.dimension().compareTo(SecretBaseDimension.WORLD_KEY) == 0)
                world.getWorldBorder().setSize(WORLDSIZE);
        }

        @SubscribeEvent
        public static void onWorldLoad(final LevelEvent.Load event)
        {
            final Level world = (Level) event.getLevel();
            if (world.getWorldBorder().getSize() != WORLDSIZE
                    && world.dimension().compareTo(SecretBaseDimension.WORLD_KEY) == 0)
                world.getWorldBorder().setSize(WORLDSIZE);
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
            final AABB chunkBox = SecretBaseDimension.getBaseBox(nearestBase);

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
    }

    private static AABB getBaseBox(final ChunkPos nearestBase)
    {
        final BlockPos pos1 = nearestBase.getWorldPosition();
        final BlockPos pos2 = pos1.offset(16, 255, 16);
        final AABB chunkBox = AABB.encapsulatingFullBlocks(pos1, pos2);

        // int index = fromChunkPos(nearestBase);
        //
        //

        return chunkBox;
    }

    public static List<GlobalPos> getNearestBases(final GlobalPos here, final int baseRadarRange)
    {
        final List<GlobalPos> bases = Lists.newArrayList();
        for (final GlobalPos v : PokecubeSerializer.getInstance().bases)
            if (v.pos().closerThan(here.pos(), baseRadarRange)) bases.add(v);
        return bases;
    }

}
