package pokecube.core.ai.tasks.ants.nest;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class Part implements INBTSerializable<CompoundNBT>
{

    // Persistant value to track if we have started being mined.
    public boolean started    = false;
    public long    dig_done   = 0;
    public long    build_done = 0;

    private AxisAlignedBB inBounds  = null;
    private AxisAlignedBB outBounds = null;

    private final List<BlockPos> digBounds   = Lists.newArrayList();
    private final List<BlockPos> buildBounds = Lists.newArrayList();

    // If present, when loading this map will be used to sync the nodes
    // on the edges.
    private Tree _tree = null;

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("s", this.started);
        nbt.putLong("dd", this.dig_done);
        nbt.putLong("bd", this.build_done);
        return nbt;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        this.started = nbt.getBoolean("s");
        this.dig_done = nbt.getLong("dd");
        this.build_done = nbt.getLong("bd");
    }

    public boolean shouldDig(final long worldTime)
    {
        return this.started && this.dig_done < worldTime;
    }

    public boolean shouldBuild(final long worldTime)
    {
        return this.started && this.build_done < worldTime && !this.shouldDig(worldTime);
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

    public AxisAlignedBB getOutBounds()
    {
        return this.outBounds;
    }

    public void setOutBounds(final AxisAlignedBB outBounds)
    {
        this.outBounds = outBounds;
        this.buildBounds.clear();
        BlockPos.getAllInBox(this.getOutBounds()).forEach(p -> this.buildBounds.add(p.toImmutable()));
    }

    public AxisAlignedBB getInBounds()
    {
        return this.inBounds;
    }

    public void setInBounds(final AxisAlignedBB inBounds)
    {
        this.inBounds = inBounds;
        this.digBounds.clear();
        BlockPos.getAllInBox(this.getInBounds()).forEach(p -> this.digBounds.add(p.toImmutable()));
    }

    public void setDigDone(final long time)
    {
        this.dig_done = time;
    }
}
