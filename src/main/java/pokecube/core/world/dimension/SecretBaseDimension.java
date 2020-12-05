package pokecube.core.world.dimension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Blockreader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.entity.ThutTeleporter;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public class SecretBaseDimension
{
    public static final DeferredRegister<SurfaceBuilder<?>> REG = DeferredRegister.create(
            ForgeRegistries.SURFACE_BUILDERS, PokecubeCore.MODID);

    public static void onConstruct(final IEventBus bus)
    {
        Registry.register(Registry.CHUNK_GENERATOR_CODEC, "pokecube:secret_base", SecretChunkGenerator.CODEC);
        MinecraftForge.EVENT_BUS.register(SecretBaseDimension.class);
    }

    public static void sendToBase(final ServerPlayerEntity player, final UUID baseOwner)
    {
        final RegistryKey<World> targetDim = SecretBaseDimension.WORLD_KEY;
        final BlockPos pos = SecretBaseDimension.getSecretBaseLoc(baseOwner, player.getServer(), targetDim);
        final Vector3 v = Vector3.getNewVector().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(GlobalPos.getPosition(targetDim, pos), v), true);
        player.sendMessage(new TranslationTextComponent("pokecube.secretbase.enter"), Util.DUMMY_UUID);
    }

    public static void sendToExit(final ServerPlayerEntity player, final UUID baseOwner)
    {
        final RegistryKey<World> targetDim = World.OVERWORLD;
        final BlockPos pos = SecretBaseDimension.getSecretBaseLoc(baseOwner, player.getServer(), targetDim);
        final Vector3 v = Vector3.getNewVector().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(GlobalPos.getPosition(targetDim, pos), v), true);
        player.sendMessage(new TranslationTextComponent("pokecube.secretbase.exit"), Util.DUMMY_UUID);
    }

    public static void setSecretBasePoint(final ServerPlayerEntity player, final BlockPos pos,
            final RegistryKey<World> dim)
    {
        final CompoundNBT tag = PokecubePlayerDataHandler.getCustomDataTag(player);
        final CompoundNBT exit = new CompoundNBT();
        exit.putInt("x", pos.getX());
        exit.putInt("y", pos.getY());
        exit.putInt("z", pos.getZ());
        if (dim == SecretBaseDimension.WORLD_KEY) tag.put("secret_base_internal", exit);
        else
        {
            if (tag.contains("secret_base_exit"))
            {
                final CompoundNBT exito = tag.getCompound("secret_base_exit");
                final Vector4 old = new Vector4(exito.getInt("x"), exito.getInt("y"), exito.getInt("z"), 0);
                PokecubeSerializer.getInstance().bases.removeIf(c -> old.withinDistance(0.25f, old));
            }
            tag.put("secret_base_exit", exit);
            PokecubeSerializer.getInstance().bases.add(GlobalPos.getPosition(dim, pos));
        }
    }

    public static ChunkPos getFromIndex(final int index)
    {
        final int scale = 10;
        int x = index % (16 << scale) - (16 << scale) / 2;
        int z = index / (16 << scale) - (16 << scale) / 2;
        x *= 16;
        z *= 16;
        return new ChunkPos(x, z);
    }

    public static BlockPos getSecretBaseLoc(final UUID player, final MinecraftServer server,
            final RegistryKey<World> dim)
    {
        final CompoundNBT tag = PokecubePlayerDataHandler.getCustomDataTag(player.toString());
        if (dim == SecretBaseDimension.WORLD_KEY)
        {
            if (tag.contains("secret_base_internal"))
            {
                final CompoundNBT exit = tag.getCompound("secret_base_internal");
                return new BlockPos(exit.getInt("x"), exit.getInt("y"), exit.getInt("z"));
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
            return new BlockPos((chunk.x << 4) + 8, 64, (chunk.z << 4) + 8);
        }
        else if (!tag.contains("secret_base_exit")) return server.getWorld(dim).getSpawnPoint();
        else
        {
            final CompoundNBT exit = tag.getCompound("secret_base_exit");
            return new BlockPos(exit.getInt("x"), exit.getInt("y"), exit.getInt("z"));
        }
    }

    public static class SecretChunkGenerator extends ChunkGenerator
    {
        public static final Codec<SecretChunkGenerator> CODEC = RegistryLookupCodec.getLookUpCodec(Registry.BIOME_KEY)
                .xmap(SecretChunkGenerator::new, SecretChunkGenerator::getRegistry).stable().codec();

        private final Registry<Biome> registry;

        BlockState[] states = new BlockState[256];

        public SecretChunkGenerator(final Registry<Biome> registry)
        {
            super(new SingleBiomeProvider(registry.getOrThrow(SecretBaseDimension.BIOME_KEY)),
                    new DimensionStructuresSettings(false));
            this.registry = registry;
            Arrays.fill(this.states, Blocks.AIR.getDefaultState());
        }

        public Registry<Biome> getRegistry()
        {
            return this.registry;
        }

        @Override
        protected Codec<? extends ChunkGenerator> func_230347_a_()
        {
            return SecretChunkGenerator.CODEC;
        }

        @Override
        public ChunkGenerator func_230349_a_(final long p_230349_1_)
        {
            return this;
        }

        @Override
        public void generateSurface(final WorldGenRegion p_225551_1_, final IChunk p_225551_2_)
        {

        }

        @Override
        public void func_230352_b_(final IWorld world, final StructureManager manager, final IChunk chunk)
        {
            final ChunkPos pos = chunk.getPos();
            final double h = chunk.getHeight();
            final boolean stone = pos.x % 16 == 0 && pos.z % 16 == 0;
            // if(!stone) return;
            final BlockPos.Mutable blockpos$mutableblockpos = new BlockPos.Mutable();
            BlockState state = Blocks.STONE.getDefaultState();
            final Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
            final Heightmap heightmap1 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
            for (int i = 0; i < 16; i++)
                for (int j = 0; j < h; j++)
                    for (int k = 0; k < 16; k++)
                    {
                        state = j < 64 && j > 57 && k > 3 && k < 12 && i > 3 && i < 12 ? Blocks.STONE.getDefaultState()
                                : Blocks.AIR.getDefaultState();
                        if (!stone || j == 0 || j == h - 1) state = Blocks.BARRIER.getDefaultState();
                        chunk.setBlockState(blockpos$mutableblockpos.setPos(i, j, k), state, false);
                        if (j < 64)
                        {
                            heightmap.update(i, j, k, state);
                            heightmap1.update(i, j, k, state);
                        }
                    }
        }

        @Override
        public int getHeight(final int x, final int z, final Type heightmapType)
        {
            return 64;
        }

        @Override
        public IBlockReader func_230348_a_(final int x, final int z)
        {
            return new Blockreader(this.states);
        }

    }

    public static final String ID = PokecubeCore.MODID + ":secret_base";

    private static final ResourceLocation IDLOC = new ResourceLocation(SecretBaseDimension.ID);

    public static final RegistryKey<World> WORLD_KEY = RegistryKey.getOrCreateKey(Registry.WORLD_KEY,
            SecretBaseDimension.IDLOC);
    public static final RegistryKey<Biome> BIOME_KEY = RegistryKey.getOrCreateKey(Registry.BIOME_KEY,
            SecretBaseDimension.IDLOC);

    @SubscribeEvent
    @OnlyIn(value = Dist.CLIENT)
    public static void clientTick(final ClientTickEvent event)
    {
        final World world = PokecubeCore.proxy.getWorld();
        if (world == null) return;
        if (world.getWorldBorder().getSize() != 2999984 && world.getDimensionKey().compareTo(SecretBaseDimension.WORLD_KEY) == 0) world
                .getWorldBorder().setSize(2999984);
    }

    @SubscribeEvent
    public static void worldTick(final WorldTickEvent event)
    {
        final World world = event.world;
        if (world.getWorldBorder().getSize() != 2999984 && world.getDimensionKey().compareTo(SecretBaseDimension.WORLD_KEY) == 0) world
                .getWorldBorder().setSize(2999984);
    }

    @SubscribeEvent
    public static void worldLoad(final WorldEvent.Load event)
    {
        final World world = (World) event.getWorld();
        if (world.getWorldBorder().getSize() != 2999984 && world.getDimensionKey().compareTo(SecretBaseDimension.WORLD_KEY) == 0) world
                .getWorldBorder().setSize(2999984);
    }

    public static List<GlobalPos> getNearestBases(final GlobalPos here, final int baseRadarRange)
    {
        final List<GlobalPos> bases = Lists.newArrayList();
        for (final GlobalPos v : PokecubeSerializer.getInstance().bases)
            if (v.getPos().withinDistance(here.getPos(), baseRadarRange)) bases.add(v);
        return bases;
    }

}
