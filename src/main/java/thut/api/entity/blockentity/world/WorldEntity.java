package thut.api.entity.blockentity.world;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ticks.LevelTickAccess;
import thut.api.entity.blockentity.IBlockEntity;

public class WorldEntity implements IBlockEntityWorld
{

    final Level              world;
    IBlockEntity             mob;
    public boolean           creating;
    BlockEntityChunkProvider chunks;

    public WorldEntity(final Level world)
    {
        this.world = world;
        this.chunks = new BlockEntityChunkProvider(this);
    }

    @Override
    public Level getWorld()
    {
        return this.world;
    }

    @Override
    public IBlockEntity getBlockEntity()
    {
        return this.mob;
    }

    @Override
    public int getBrightness(final LightLayer type, final BlockPos pos)
    {
        return this.world.getBrightness(type, pos);
    }

    @Override
    public boolean setBlock(final BlockPos pos, final BlockState newState, final int flags)
    {
        final ChunkAccess c = this.getChunk(pos);
        return c.setBlockState(pos, newState, (flags & 64) != 0) != null;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        BlockState state = this.getBlock(pos);
        if (state == null) state = this.world.getBlockState(pos);
        return state;
    }

    @Override
    public BlockEntity getBlockEntity(final BlockPos pos)
    {
        final BlockEntity tile = this.getTile(pos);
        if (tile == null) return this.world.getBlockEntity(pos);
        return tile;
    }

    @Override
    public void setBlockEntity(final IBlockEntity mob)
    {
        IBlockEntityWorld.super.setBlockEntity(mob);
        this.mob = mob;
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        return this.world.getFluidState(pos);
    }

    @Override
    public void levelEvent(final Player player, final int type, final BlockPos pos, final int data)
    {
        this.world.levelEvent(player, type, pos, data);
    }

    @Override
    public List<? extends Player> players()
    {
        return this.world.players();
    }

    @Override
    public ChunkAccess getChunk(final int x, final int z, final ChunkStatus requiredStatus, final boolean nonnull)
    {
        return new EntityChunk(this, new ChunkPos(x, z));
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(final int x, final int y, final int z)
    {
        return this.world.getNoiseBiome(x, y, z);
    }

    @Override
    public Holder<Biome> getBiome(final BlockPos pos)
    {
        return this.world.getBiome(pos);
    }

    @Override
    public Holder<Biome> getNoiseBiome(final int x, final int y, final int z)
    {
        return this.world.getNoiseBiome(x, y, z);
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(final BlockPos pos)
    {
        return this.world.getCurrentDifficultyAt(pos);
    }

    @Override
    public Random getRandom()
    {
        return this.world.getRandom();
    }

    @Override
    public void playSound(final Player player, final BlockPos pos, final SoundEvent soundIn, final SoundSource category,
            final float volume, final float pitch)
    {
        this.world.playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void addParticle(final ParticleOptions particleData, final double x, final double y, final double z,
            final double xSpeed, final double ySpeed, final double zSpeed)
    {
        this.world.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public List<Entity> getEntities(final Entity entityIn, final AABB boundingBox,
            final Predicate<? super Entity> predicate)
    {
        return this.world.getEntities(entityIn, boundingBox, predicate);
    }

    @Override
    public int getHeight(final Types heightmapType, final int x, final int z)
    {
        return this.world.getHeight(heightmapType, x, z);
    }

    @Override
    public int getSkyDarken()
    {
        return this.world.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager()
    {
        return this.world.getBiomeManager();
    }

    @Override
    public boolean isClientSide()
    {
        return this.world.isClientSide();
    }

    @Override
    public int getSeaLevel()
    {
        return this.world.getSeaLevel();
    }

    @Override
    public LevelLightEngine getLightEngine()
    {
        return this.world.getLightEngine();
    }

    @Override
    public WorldBorder getWorldBorder()
    {
        return this.world.getWorldBorder();
    }

    @Override
    public ChunkSource getChunkSource()
    {
        return this.chunks;
    }

    @Override
    public RegistryAccess registryAccess()
    {
        return this.world.registryAccess();
    }

    @Override
    public float getShade(final Direction p_230487_1_, final boolean p_230487_2_)
    {
        return this.world.getShade(p_230487_1_, p_230487_2_);
    }

    @Override
    public LevelData getLevelData()
    {
        return this.world.getLevelData();
    }

    @Override
    public DimensionType dimensionType()
    {
        return this.world.dimensionType();
    }

    @Override
    public boolean isStateAtPosition(final BlockPos p_217375_1_, final Predicate<BlockState> p_217375_2_)
    {
        return false;
    }

    @Override
    public boolean removeBlock(final BlockPos pos, final boolean isMoving)
    {
        return false;
    }

    @Override
    public boolean destroyBlock(final BlockPos p_225521_1_, final boolean p_225521_2_, final Entity p_225521_3_)
    {
        return false;
    }

    @Override
    public boolean setBlock(final BlockPos pos, final BlockState state, final int flags, final int recursionLeft)
    {
        return false;
    }

    @Override
    public boolean destroyBlock(final BlockPos pos, final boolean dropBlock, final Entity entity,
            final int recursionLeft)
    {
        return false;
    }

    @Override
    public MinecraftServer getServer()
    {
        return this.world.getServer();
    }

    @Override
    public void gameEvent(final Entity p_151549_, final GameEvent p_151550_, final BlockPos p_151551_)
    {
        this.world.gameEvent(p_151549_, p_151550_, p_151551_);
    }

    @Override
    public <T extends Entity> List<T> getEntities(final EntityTypeTest<Entity, T> p_151464_, final AABB p_151465_,
            final Predicate<? super T> p_151466_)
    {
        return this.world.getEntities(p_151464_, p_151465_, p_151466_);
    }

    @Override
    public boolean isFluidAtPosition(final BlockPos p_151584_, final Predicate<FluidState> p_151585_)
    {
        return this.world.isFluidAtPosition(p_151584_, p_151585_);
    }

    @Override
    public long nextSubTickCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks()
    {
        return this.world.getBlockTicks();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks()
    {
        return this.world.getFluidTicks();
    }
}