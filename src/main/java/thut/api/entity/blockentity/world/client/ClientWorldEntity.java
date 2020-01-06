package thut.api.entity.blockentity.world.client;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.entity.blockentity.IBlockEntity;

public class ClientWorldEntity extends World implements IBlockEntityWorld<World>
{
    public static ClientWorldEntity instance;

    final World                     world;
    IBlockEntity                    mob;
    public boolean                  creating;

    public ClientWorldEntity(final World world)
    {
        super(world.getWorldInfo(), world.getDimension().getType(),
                (worldIn, dimensionIn) -> new BlockEntityChunkProvider((ClientWorldEntity) worldIn),
                world.getProfiler(), world.isRemote);
        this.world = world;
    }

    @Override
    public Biome getBiome(final BlockPos pos)
    {
        return this.world.getBiome(pos);
    }

    @Override
    public IBlockEntity getBlockEntity()
    {
        return this.mob;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        final BlockState state = this.getBlock(pos);
        if (state == null) return this.world.getBlockState(pos);
        return state;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getCombinedLight(final BlockPos pos, final int minLight)
    {
        return this.world.getCombinedLight(pos, minLight);
    }

    @Override
    public Entity getEntityByID(final int id)
    {
        return this.world.getEntityByID(id);
    }

    // @Override
    // public MapData getMapData(final String p_217406_1_)
    // {
    // TODO
    // return this.world.getMapData(p_217406_1_);
    // }

    @Override
    public int getNextMapId()
    {
        return this.world.getNextMapId();
    }

    @Override
    public ITickList<Block> getPendingBlockTicks()
    {
        return null;
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks()
    {
        return null;
    }

    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        return this.world.getPlayers();
    }

    @Override
    public RecipeManager getRecipeManager()
    {
        return this.world.getRecipeManager();
    }

    @Override
    public Scoreboard getScoreboard()
    {
        return this.world.getScoreboard();
    }

    @Override
    public int getStrongPower(final BlockPos pos, final Direction direction)
    {
        return this.world.getStrongPower(pos, direction);
    }

    @Override
    public NetworkTagManager getTags()
    {
        return this.world.getTags();
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        final TileEntity tile = this.getTile(pos);
        if (tile == null) return this.world.getTileEntity(pos);
        return tile;
    }

    @Override
    public WorldType getWorldType()
    {
        return this.world.getWorldType();
    }

    @Override
    public World getWrapped()
    {
        return this.world;
    }

    @Override
    public boolean isAirBlock(final BlockPos pos)
    {
        final BlockState state = this.getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }

    @Override
    public void notifyBlockUpdate(final BlockPos pos, final BlockState oldState, final BlockState newState,
            final int flags)
    {
        this.world.notifyBlockUpdate(pos, oldState, newState, flags);
    }

    @Override
    public void playEvent(final PlayerEntity p_217378_1_, final int p_217378_2_, final BlockPos p_217378_3_,
            final int p_217378_4_)
    {
        this.world.playEvent(p_217378_1_, p_217378_2_, p_217378_3_, p_217378_4_);
    }

    @Override
    public void playMovingSound(final PlayerEntity p_217384_1_, final Entity p_217384_2_, final SoundEvent p_217384_3_,
            final SoundCategory p_217384_4_, final float p_217384_5_, final float p_217384_6_)
    {
        this.world.playMovingSound(p_217384_1_, p_217384_2_, p_217384_3_, p_217384_4_, p_217384_5_, p_217384_6_);
    }

    @Override
    public void playSound(final PlayerEntity player, final double x, final double y, final double z,
            final SoundEvent soundIn, final SoundCategory category, final float volume, final float pitch)
    {
        this.world.playSound(player, x, y, z, soundIn, category, volume, pitch);
    }

    // @Override
    // public void registerMapData(final MapData p_217399_1_)
    // {//TODO
    // this.world.registerMapData(p_217399_1_);
    // }

    @Override
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress)
    {
        this.world.sendBlockBreakProgress(breakerId, pos, progress);
    }

    @Override
    public void setBlockEntity(final IBlockEntity mob)
    {
        IBlockEntityWorld.super.setBlockEntity(mob);
        this.mob = mob;
    }

    /** Sets the block state at a given location. Flag 1 will cause a block
     * update. Flag 2 will send the change to clients (you almost always want
     * this). Flag 4 prevents the block from being re-rendered, if this is a
     * client world. Flags can be added together. */
    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState newState, final int flags)
    {
        if (this.setBlock(pos, newState)) return true;
        else return this.world.setBlockState(pos, newState, flags);
    }

    @Override
    public void setTileEntity(final BlockPos pos, @Nullable final TileEntity tileEntityIn)
    {
        if (this.setTile(pos, tileEntityIn)) return;
        this.getWrapped().setTileEntity(pos, tileEntityIn);
    }

    @Override
    public MapData func_217406_a(String p_217406_1_)
    {
        // TODO Auto-generated method stub
        return this.world.func_217406_a(p_217406_1_);
    }

    @Override
    public void func_217399_a(MapData p_217399_1_)
    {
        this.world.func_217399_a(p_217399_1_);
    }

}