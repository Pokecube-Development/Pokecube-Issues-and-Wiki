package pokecube.core.ai.tasks.ants.nest;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class Edge extends Part
{
    public Node node1;
    public Node node2;

    public BlockPos end1;
    public BlockPos end2;

    // Fraction of the way dug from end1 to end2.
    public double digged = 0;

    boolean areSame(final Edge other)
    {
        return this.end1.equals(other.end1) && this.end2.equals(other.end2);
    }

    public boolean withinDistance(final BlockPos pos, final double size)
    {
        double maxY;
        double minY;
        if (size > 2)
        {
            maxY = Math.max(this.node1.outBounds.maxY, this.node2.outBounds.maxY);
            minY = Math.min(this.node1.outBounds.minY, this.node2.outBounds.minY);
        }
        else
        {
            maxY = Math.max(this.node1.inBounds.maxY, this.node2.inBounds.maxY);
            minY = Math.min(this.node1.inBounds.minY, this.node2.inBounds.minY);
        }
        if (pos.getY() > maxY || pos.getY() < minY) return false;
        final Vector3d e1 = new Vector3d(this.end1.getX(), this.end1.getY(), this.end1.getZ());
        final Vector3d e2 = new Vector3d(this.end2.getX(), this.end2.getY(), this.end2.getZ());
        final Vector3d e3 = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
        Vector3d n = e2.subtract(e1).normalize();
        final Vector3d diff = e1.subtract(e3);
        n = n.crossProduct(diff);
        return n.length() < size;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof Edge)) return false;
        return this.areSame((Edge) obj);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT edgeNbt = super.serializeNBT();
        edgeNbt.put("n1", NBTUtil.writeBlockPos(this.node1.getCenter()));
        edgeNbt.put("e1", NBTUtil.writeBlockPos(this.end1));
        edgeNbt.put("n2", NBTUtil.writeBlockPos(this.node2.getCenter()));
        edgeNbt.put("e2", NBTUtil.writeBlockPos(this.end2));
        return edgeNbt;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        super.deserializeNBT(nbt);
        this.end1 = NBTUtil.readBlockPos(nbt.getCompound("e1"));
        this.end2 = NBTUtil.readBlockPos(nbt.getCompound("e2"));

        final BlockPos p1 = NBTUtil.readBlockPos(nbt.getCompound("n1"));
        final BlockPos p2 = NBTUtil.readBlockPos(nbt.getCompound("n2"));

        // The following 4 lines ensure that the nodes are the
        // correctly loaded ones.
        if (this.getTree() != null)
        {
            this.node1 = this.getTree().map.getOrDefault(p1, new Node(p1));
            this.node2 = this.getTree().map.getOrDefault(p2, new Node(p2));
        }
        else
        {
            this.node1 = new Node(p1);
            this.node2 = new Node(p2);
        }
    }

    @Override
    public boolean isInside(final BlockPos pos)
    {
        if (this.node1.selfInside(pos)) return true;
        if (this.node2.selfInside(pos)) return true;
        return this.withinDistance(pos, 1.5);
    }

    @Override
    public boolean isOnShell(final BlockPos pos)
    {
        return this.withinDistance(pos, 4) && !this.isInside(pos);
    }
}
