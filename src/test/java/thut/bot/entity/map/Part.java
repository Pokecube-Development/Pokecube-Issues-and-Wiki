package thut.bot.entity.map;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.interfaces.PokecubeMod;

public abstract class Part implements INBTSerializable<CompoundTag>
{
    public UUID id = UUID.randomUUID();

    // Persistant value to track if we have started being mined.
    public boolean started = false;

    // Used to track when last done things.
    public long dig_done   = 0;
    public long build_done = 0;

    private AABB inBounds  = null;
    private AABB outBounds = null;

    private final List<BlockPos> buildBounds = Lists.newArrayList();

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
        nbt.putUUID("id", this.id);

        final ListTag build = new ListTag();
        for (final BlockPos p : this.buildBounds)
            build.add(NbtUtils.writeBlockPos(p));
        nbt.put("bb", build);
        nbt.putString("ids", this.id.toString());

        return nbt;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.started = nbt.getBoolean("s");
        this.id = nbt.getUUID("id");
        if (!PokecubeMod.debug)
        {
            this.dig_done = nbt.getLong("dd");
            this.build_done = nbt.getLong("bd");
        }
        final ListTag build = nbt.getList("bb", 10);
        for (int i = 0; i < build.size(); i++)
            this.buildBounds.add(NbtUtils.readBlockPos(build.getCompound(i)));
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
        _tree.allParts.put(this.id, this);
    }

    public List<BlockPos> getBuildBounds()
    {
        return this.buildBounds;
    }

    public AABB getOutBounds()
    {
        return this.outBounds;
    }

    public void setOutBounds(final AABB outBounds)
    {
        this.outBounds = outBounds;
    }

    public AABB getInBounds()
    {
        return this.inBounds;
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
    }

    public void setDigDone(final long time)
    {
        this.dig_done = time;
    }
}
