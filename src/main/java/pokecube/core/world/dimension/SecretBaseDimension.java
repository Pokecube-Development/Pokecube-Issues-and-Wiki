package pokecube.core.world.dimension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.entity.ThutTeleporter;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.api.maths.Vector3;

public class SecretBaseDimension
{

    public static void onConstruct(final IEventBus bus)
    {
        Registry.register(Registry.CHUNK_GENERATOR, "pokecube:secret_base", SecretChunkGenerator.CODEC);
        MinecraftForge.EVENT_BUS.register(SecretBaseDimension.class);
    }

    public static void sendToBase(final ServerPlayer player, final UUID baseOwner)
    {
        final GlobalPos pos = SecretBaseDimension.getSecretBaseLoc(baseOwner, player.getServer(), true);
        final Vector3 v = new Vector3().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(pos, v), true);
        player.sendMessage(new TranslatableComponent("pokecube.secretbase.enter"), Util.NIL_UUID);
    }

    public static void sendToExit(final ServerPlayer player, final UUID baseOwner)
    {
        final GlobalPos pos = SecretBaseDimension.getSecretBaseLoc(baseOwner, player.getServer(), false);
        final Vector3 v = new Vector3().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(pos, v), true);
        player.sendMessage(new TranslatableComponent("pokecube.secretbase.exit"), Util.NIL_UUID);
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
            final Tag exit = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, gpos).get().left().get();
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
                PokecubeSerializer.getInstance().bases
                        .removeIf(c -> orig.dimension().location().equals(c.dimension().location())
                                && orig.pos().equals(c.pos()));
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
        final CompoundTag tag = PokecubePlayerDataHandler.getCustomDataTag(player.toString());
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
        public static final Codec<SecretChunkGenerator> CODEC = RegistryLookupCodec.create(Registry.BIOME_REGISTRY)
                .xmap(SecretChunkGenerator::new, SecretChunkGenerator::getRegistry).stable().codec();

        private final Registry<Biome> registry;

        BlockState[] states = new BlockState[256];

        public SecretChunkGenerator(final Registry<Biome> registry)
        {
            super(new FixedBiomeSource(registry.getOrThrow(SecretBaseDimension.BIOME_KEY)),
                    new StructureSettings(false));
            this.registry = registry;
            Arrays.fill(this.states, Blocks.AIR.defaultBlockState());
        }

        public Registry<Biome> getRegistry()
        {
            return this.registry;
        }

        @Override
        protected Codec<? extends ChunkGenerator> codec()
        {
            return SecretChunkGenerator.CODEC;
        }

        @Override
        public ChunkGenerator withSeed(final long p_230349_1_)
        {
            return this;
        }

        @Override
        public int getBaseHeight(final int x, final int z, final Types heightmapType,
                final LevelHeightAccessor p_156156_)
        {
            return 64;
        }

        @Override
        public NoiseColumn getBaseColumn(final int x, final int z, final LevelHeightAccessor p_156152_)
        {
            return new NoiseColumn(0, this.states);
        }

        @Override
        public Sampler climateSampler()
        {
            return (temperature, humidity, continentalness, erosion, depth, weirdness, spawnTarget) -> {
                return Climate.target(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            };
        }

        @Override
        public void applyCarvers(WorldGenRegion p_187691_, long p_187692_, BiomeManager p_187693_,
                StructureFeatureManager p_187694_, ChunkAccess p_187695_, Carving p_187696_)
        {

        }

        @Override
        public void buildSurface(WorldGenRegion p_187697_, StructureFeatureManager p_187698_, ChunkAccess p_187699_)
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
        public CompletableFuture<ChunkAccess> fillFromNoise(Executor p_187748_, Blender p_187749_,
                StructureFeatureManager p_187750_, ChunkAccess chunk)
        {

            final ChunkPos pos = chunk.getPos();
            final boolean stone = pos.x % 16 == 0 && pos.z % 16 == 0;
            final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            BlockState state = Blocks.STONE.defaultBlockState();
            final Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
            final Heightmap heightmap1 = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
            for (int i = 0; i < 16; i++) for (int k = 0; k < 16; k++)
            {
                chunk.setBlockState(blockpos$mutableblockpos.set(i, 0, k), Blocks.BARRIER.defaultBlockState(), false);
                if (stone) for (int j = 57; j < 64; j++)
                {
                    state = j < 64 && j > 57 && k > 3 && k < 12 && i > 3 && i < 12 ? Blocks.STONE.defaultBlockState()
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

        @Override
        public void addDebugScreenInfo(List<String> p_208054_, BlockPos p_208055_)
        {
            // TODO Auto-generated method stub
            
        }

    }

    public static final String ID = PokecubeCore.MODID + ":secret_base";

    private static final ResourceLocation IDLOC = new ResourceLocation(SecretBaseDimension.ID);

    public static final ResourceKey<Level> WORLD_KEY = ResourceKey.create(Registry.DIMENSION_REGISTRY,
            SecretBaseDimension.IDLOC);
    public static final ResourceKey<Biome> BIOME_KEY = ResourceKey.create(Registry.BIOME_REGISTRY,
            SecretBaseDimension.IDLOC);

    public static final double WORLDSIZE = 2 * 2999984;

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientEventHandler
    {

        @SubscribeEvent
        public static void onClientTick(final ClientTickEvent event)
        {
            final Level world = PokecubeCore.proxy.getWorld();
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
        public static void onWorldTick(final WorldTickEvent event)
        {
            final Level world = event.world;
            if (world.getWorldBorder().getSize() != WORLDSIZE
                    && world.dimension().compareTo(SecretBaseDimension.WORLD_KEY) == 0)
                world.getWorldBorder().setSize(WORLDSIZE);
        }

        @SubscribeEvent
        public static void onWorldLoad(final WorldEvent.Load event)
        {
            final Level world = (Level) event.getWorld();
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

            final SectionPos newPos = event.getNewPos();

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

            if (nx <= chunkBox.minX) nx = chunkBox.maxX - 1;
            if (nx >= chunkBox.maxX) nx = chunkBox.minX + 1;
            if (nz <= chunkBox.minZ) nz = chunkBox.maxZ - 1;
            if (nz >= chunkBox.maxZ) nz = chunkBox.minZ + 1;

            final BlockPos pos = new BlockPos(nx, mob.getY(), nz);

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
        final AABB chunkBox = new AABB(pos1, pos2);

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
