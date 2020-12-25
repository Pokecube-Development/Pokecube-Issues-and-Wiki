package pokecube.compat.minecraft;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class FakeWorld extends World
{
    private static final DimensionType dimtype;

    static
    {
        dimtype = ObfuscationReflectionHelper.getPrivateValue(DimensionType.class, null, "field_236004_h_");
    }

    public static final FakeWorld INSTANCE = new FakeWorld();

    private final Scoreboard scores = new Scoreboard();

    private FakeWorld()
    {
        super(null, World.OVERWORLD, FakeWorld.dimtype, () -> EmptyProfiler.INSTANCE, true, true, 0);
    }

    @Override
    public ITickList<Block> getPendingBlockTicks()
    {
        return EmptyTickList.get();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks()
    {
        return EmptyTickList.get();
    }

    @Override
    public AbstractChunkProvider getChunkProvider()
    {
        return null;
    }

    @Override
    public void playEvent(final PlayerEntity player, final int type, final BlockPos pos, final int data)
    {

    }

    @Override
    public DynamicRegistries func_241828_r()
    {
        return null;
    }

    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        return Collections.emptyList();
    }

    @Override
    public Biome getNoiseBiomeRaw(final int x, final int y, final int z)
    {
        return null;
    }

    @Override
    public float func_230487_a_(final Direction p_230487_1_, final boolean p_230487_2_)
    {
        return 0;
    }

    @Override
    public void notifyBlockUpdate(final BlockPos pos, final BlockState oldState, final BlockState newState,
            final int flags)
    {

    }

    @Override
    public void playSound(final PlayerEntity player, final double x, final double y, final double z,
            final SoundEvent soundIn, final SoundCategory category, final float volume, final float pitch)
    {

    }

    @Override
    public void playMovingSound(final PlayerEntity playerIn, final Entity entityIn, final SoundEvent eventIn,
            final SoundCategory categoryIn, final float volume, final float pitch)
    {

    }

    @Override
    public Entity getEntityByID(final int id)
    {
        return null;
    }

    @Override
    public MapData getMapData(final String mapName)
    {
        return null;
    }

    @Override
    public void registerMapData(final MapData mapDataIn)
    {

    }

    @Override
    public int getNextMapId()
    {
        return 0;
    }

    @Override
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress)
    {

    }

    @Override
    public long getGameTime()
    {
        return 0;
    }

    @Override
    public long getDayTime()
    {
        return 0;
    }

    @Override
    public Scoreboard getScoreboard()
    {
        return this.scores;
    }

    @Override
    public RecipeManager getRecipeManager()
    {
        return null;
    }

    @Override
    public ITagCollectionSupplier getTags()
    {
        return null;
    }

}
