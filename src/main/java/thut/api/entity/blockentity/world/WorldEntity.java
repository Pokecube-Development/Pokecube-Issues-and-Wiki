package thut.api.entity.blockentity.world;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ITickList;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.IWorldInfo;
import thut.api.entity.blockentity.IBlockEntity;

public class WorldEntity implements IBlockEntityWorld
{

    final World    world;
    IBlockEntity   mob;
    public boolean creating;
    BlockEntityChunkProvider chunks;

    public WorldEntity(final World world)
    {
        this.world = world;
        this.chunks = new BlockEntityChunkProvider(this);
    }

    @Override
    public World getWorld()
    {
        return this.world;
    }

    @Override
    public IBlockEntity getBlockEntity()
    {
        return this.mob;
    }

    @Override
    public int getLightFor(final LightType type, final BlockPos pos)
    {
        return this.world.getLightFor(type, pos);
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState newState, final int flags)
    {
        final IChunk c = this.getChunk(pos);
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
    public TileEntity getTileEntity(final BlockPos pos)
    {
        final TileEntity tile = this.getTile(pos);
        if (tile == null) return this.world.getTileEntity(pos);
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
    public ITickList<Block> getPendingBlockTicks()
    {
        return this.world.getPendingBlockTicks();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks()
    {
        return this.world.getPendingFluidTicks();
    }

    @Override
    public void playEvent(final PlayerEntity player, final int type, final BlockPos pos, final int data)
    {
        this.world.playEvent(player, type, pos, data);
    }

    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        return this.world.getPlayers();
    }

    @Override
    public IChunk getChunk(final int x, final int z, final ChunkStatus requiredStatus, final boolean nonnull)
    {
        return new EntityChunk(this, new ChunkPos(x, z));
    }

    @Override
    public Biome getNoiseBiomeRaw(final int x, final int y, final int z)
    {
        return this.world.getNoiseBiome(x, y, z);
    }

    @Override
    public Biome getBiome(final BlockPos pos)
    {
        return this.world.getBiome(pos);
    }

    @Override
    public Biome getNoiseBiome(final int x, final int y, final int z)
    {
        return this.world.getNoiseBiome(x, y, z);
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(final BlockPos pos)
    {
        return this.world.getDifficultyForLocation(pos);
    }

    @Override
    public Random getRandom()
    {
        return this.world.getRandom();
    }

    @Override
    public void playSound(final PlayerEntity player, final BlockPos pos, final SoundEvent soundIn,
            final SoundCategory category, final float volume, final float pitch)
    {
        this.world.playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void addParticle(final IParticleData particleData, final double x, final double y, final double z,
            final double xSpeed, final double ySpeed, final double zSpeed)
    {
        this.world.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(final Entity entityIn, final AxisAlignedBB boundingBox,
            final Predicate<? super Entity> predicate)
    {
        return this.world.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(final Class<? extends T> clazz, final AxisAlignedBB aabb,
            final Predicate<? super T> filter)
    {
        return this.world.getEntitiesWithinAABB(clazz, aabb, filter);
    }

    @Override
    public int getHeight(final Type heightmapType, final int x, final int z)
    {
        return this.world.getHeight(heightmapType, x, z);
    }

    @Override
    public int getSkylightSubtracted()
    {
        return this.world.getSkylightSubtracted();
    }

    @Override
    public BiomeManager getBiomeManager()
    {
        return this.world.getBiomeManager();
    }

    @Override
    public boolean isRemote()
    {
        // TODO Auto-generated method stub
        return this.world.isRemote();
    }

    @Override
    public int getSeaLevel()
    {
        return this.world.getSeaLevel();
    }

    @Override
    public WorldLightManager getLightManager()
    {
        return this.world.getLightManager();
    }

    @Override
    public WorldBorder getWorldBorder()
    {
        return this.world.getWorldBorder();
    }

    @Override
    public AbstractChunkProvider getChunkProvider()
    {
        return this.chunks;
    }

    @Override
    public DynamicRegistries func_241828_r()
    {
        return this.world.func_241828_r();
    }

    @Override
    public float func_230487_a_(final Direction p_230487_1_, final boolean p_230487_2_)
    {
        return this.world.func_230487_a_(p_230487_1_, p_230487_2_);
    }

    @Override
    public IWorldInfo getWorldInfo()
    {
        return this.world.getWorldInfo();
    }

    @Override
    public DimensionType getDimensionType()
    {
        return this.world.getDimensionType();
    }

    @Override
    public boolean hasBlockState(final BlockPos p_217375_1_, final Predicate<BlockState> p_217375_2_)
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
    public boolean setBlockState(final BlockPos pos, final BlockState state, final int flags, final int recursionLeft)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean destroyBlock(final BlockPos pos, final boolean dropBlock, final Entity entity,
            final int recursionLeft)
    {
        // TODO Auto-generated method stub
        return false;
    }
}