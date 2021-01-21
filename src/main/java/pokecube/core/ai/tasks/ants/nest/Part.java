package pokecube.core.ai.tasks.ants.nest;

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

    public AxisAlignedBB inBounds  = null;
    public AxisAlignedBB outBounds = null;

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

}
