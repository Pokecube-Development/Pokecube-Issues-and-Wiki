package thut.bot.entity.map;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.interfaces.PokecubeMod;

public abstract class Part implements INBTSerializable<CompoundTag>
{

    // Persistant value to track if we have started being mined.
    public boolean started    = false;
    public long    dig_done   = 0;
    public long    build_done = 0;

    private AABB inBounds  = null;
    private AABB outBounds = null;

    private final List<BlockPos> digBounds   = Lists.newArrayList();
    private final List<BlockPos> buildBounds = Lists.newArrayList();

    Object2LongOpenHashMap<BlockPos> digBlocks   = new Object2LongOpenHashMap<>();
    Object2LongOpenHashMap<BlockPos> buildBlocks = new Object2LongOpenHashMap<>();

    // If present, when loading this map will be used to sync the nodes
    // on the edges.
    private Tree _tree = null;

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("s", this.started);
        nbt.putLong("dd", this.dig_done);
        nbt.putLong("bd", this.build_done);
        return nbt;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.started = nbt.getBoolean("s");
        if (!PokecubeMod.debug)
        {
            this.dig_done = nbt.getLong("dd");
            this.build_done = nbt.getLong("bd");
        }
    }

    public boolean shouldDig(final long worldTime)
    {
        return this.started && this.dig_done < worldTime;
    }

    public boolean shouldBuild(final long worldTime)
    {
        return this.started && this.build_done < worldTime;
    }

    public abstract boolean isInside(BlockPos pos);

    public abstract boolean isOnShell(BlockPos pos);

    public Tree getTree()
    {
        return this._tree;
    }

    public void setTree(final Tree _tree)
    {
        this._tree = _tree;
    }

    public List<BlockPos> getDigBounds()
    {
        return this.digBounds;
    }

    public List<BlockPos> getBuildBounds()
    {
        Collections.shuffle(this.buildBounds);
        return this.buildBounds;
    }

    public AABB getOutBounds()
    {
        return this.outBounds;
    }

    public void setOutBounds(final AABB outBounds)
    {
        this.outBounds = outBounds;
        this.buildBounds.clear();
        this.getBuildBlocks().clear();
//        BlockPos.betweenClosedStream(this.getOutBounds()).forEach(p ->
//        {
//            final BlockPos p2 = p.immutable();
//            this.buildBounds.add(p2);
//            if (this.isOnShell(p2)) this.getBuildBlocks().put(p2, 0);
//        });
    }

    public AABB getInBounds()
    {
        return this.inBounds;
    }

    public boolean shouldCheckDig(final BlockPos pos, final long time)
    {
        return this.shouldDig(time) && this.getDigBlocks().getOrDefault(pos, time) < time;
    }

    public void markDug(final BlockPos pos, final long time)
    {
        this.getDigBlocks().put(pos, time);
    }

    public Object2LongOpenHashMap<BlockPos> getDigBlocks()
    {
        return this.digBlocks;
    }

    public boolean shouldCheckBuild(final BlockPos pos, final long time)
    {
        return this.shouldBuild(time) && this.getBuildBlocks().getOrDefault(pos, time) < time;
    }

    public void markBuilt(final BlockPos pos, final long time)
    {
        this.getBuildBlocks().put(pos, time);
    }

    public Object2LongOpenHashMap<BlockPos> getBuildBlocks()
    {
        return this.buildBlocks;
    }

    public void setInBounds(final AABB inBounds)
    {
        this.inBounds = inBounds;
        this.digBounds.clear();
        this.getDigBlocks().clear();
//        BlockPos.betweenClosedStream(this.getInBounds()).forEach(p ->
//        {
//            final BlockPos p2 = p.immutable();
//            this.digBounds.add(p2);
//            if (this.isInside(p2)) this.getDigBlocks().put(p2, 0);
//        });
    }

    public void setDigDone(final long time)
    {
        this.dig_done = time;
    }
}
