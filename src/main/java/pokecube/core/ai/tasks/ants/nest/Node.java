package pokecube.core.ai.tasks.ants.nest;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.ants.AntTasks.AntRoom;

public class Node extends Part
{
    public AntRoom type = AntRoom.NODE;

    private BlockPos center;

    public Vector3d mid;

    public List<Edge> edges = Lists.newArrayList();

    public int depth = 0;

    public float size = 3;

    // Non persistent set of open blocks
    public Set<BlockPos> dug = Sets.newHashSet();

    public Node()
    {
        this.setCenter(BlockPos.ZERO, 3);
    }

    public Node(final BlockPos center)
    {
        this.setCenter(center, 3);
    }

    boolean selfInside(final BlockPos pos)
    {
        final Vector3d x0 = this.mid;
        Vector3d x = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).subtract(x0);
        x = x.mul(1, this.size / 2, 1);
        final double r = x.length();
        if (this.type == AntRoom.ENTRANCE)
        {
            final int dx = Math.abs(pos.getX() - this.getCenter().getX());
            final int dz = Math.abs(pos.getZ() - this.getCenter().getZ());
            final int dy = pos.getY() - this.getCenter().getY();
            boolean edge = dy >= 0 && dy < 2;
            edge = edge && (dx == 0 && dz > 1 && dz <= 5 || dz == 0 && dx > 1 && dx <= 5);
            if (edge) return true;
        }
        // Some basic limits first
        if (r > this.size || x.y < 0) return false;
        return true;
    }

    @Override
    public boolean isInside(final BlockPos pos)
    {
        if (!this.getInBounds().contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) return false;
        return this.selfInside(pos);
    }

    @Override
    public boolean isOnShell(final BlockPos pos)
    {
        if (!this.getOutBounds().contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) return false;
        // Check main spot first.
        if (this.isInside(pos)) return false;
        final Vector3d x0 = this.mid;
        Vector3d x = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).subtract(x0);
        x = x.mul(1, this.size / 2, 1);
        final double r = x.length();
        return r <= this.size + 2 && x.y > -2;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = super.serializeNBT();
        nbt.putInt("X", this.getCenter().getX());
        nbt.putInt("Y", this.getCenter().getY());
        nbt.putInt("Z", this.getCenter().getZ());
        nbt.putString("room", this.type.name());
        nbt.putInt("d", this.depth);
        nbt.putFloat("sz", this.size);
        final ListNBT edges = new ListNBT();
        this.edges.removeIf(edge ->
        {
            CompoundNBT edgeNbt;
            try
            {
                edgeNbt = edge.serializeNBT();
                edges.add(edgeNbt);
                return false;
            }
            catch (final Exception e)
            {
                e.printStackTrace();
                PokecubeCore.LOGGER.error("Error saving an edge!");
                return true;
            }
        });
        nbt.put("edges", edges);
        return nbt;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        super.deserializeNBT(nbt);
        final BlockPos pos = NBTUtil.readBlockPos(nbt);
        this.setCenter(pos, nbt.getFloat("sz"));
        // This is a "real" node, it will be added to the maps
        if (this.getTree() != null)
        {
            final Node n = this.getTree().map.getOrDefault(pos, this);
            if (n != this)
            {
                // Ensure the new node also has a tree.
                n.setTree(this.getTree());
                n.deserializeNBT(nbt);
                return;
            }
            this.getTree().map.put(pos, n);
        }
        this.type = AntRoom.valueOf(nbt.getString("room"));
        final ListNBT edges = nbt.getList("edges", 10);
        for (int j = 0; j < edges.size(); ++j)
        {
            final CompoundNBT edgeNbt = edges.getCompound(j);
            final Edge e = new Edge();
            try
            {
                e.setTree(this.getTree());
                e.deserializeNBT(edgeNbt);
                if (!(e.getEnd1().equals(e.getEnd2()) || this.edges.contains(e))) this.edges.add(e);
            }
            catch (final Exception e1)
            {
                e1.printStackTrace();
                PokecubeCore.LOGGER.error("Error loading an edge!");
            }
        }
    }

    public BlockPos getCenter()
    {
        return this.center;
    }

    public void setCenter(final BlockPos center, final float size)
    {
        this.center = center;
        this.size = size;
        this.mid = new Vector3d(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);

        final AxisAlignedBB min = new AxisAlignedBB(this.mid.add(-size, 0, -size), this.mid.add(size, 2, size));
        this.setInBounds(this.type == AntRoom.ENTRANCE ? min.grow(3, 0, 3) : min);
        this.setOutBounds(min.grow(2));
    }

    @Override
    public void setDigDone(final long time)
    {
        super.setDigDone(time);
        for (final Edge e : this.edges)
            e.started = true;
    }

    @Override
    public String toString()
    {
        return this.type + " " + (int) (this.size * 100) / 100f + " " + this.center;
    }
}