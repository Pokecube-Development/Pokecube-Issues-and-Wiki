package thut.bot.entity.ai.modules.map;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thut.core.common.ThutCore;

public class Edge extends Part
{
    public Node node1;
    public Node node2;

    private BlockPos end1;
    private BlockPos end2;

    public int digInd = 0;

    boolean areSame(final Edge other)
    {
        final BlockPos here1 = this.node1.getCenter().atY(0);
        final BlockPos here2 = this.node2.getCenter().atY(0);
        final BlockPos there1 = other.node1.getCenter().atY(0);
        final BlockPos there2 = other.node2.getCenter().atY(0);
        return (here1.equals(there1) && here2.equals(there2)) ||
               (here1.equals(there2) && here2.equals(there1));
    }

    public boolean withinDistance(final BlockPos pos, final double size)
    {
        double maxY;
        double minY;
        if (size > 2)
        {
            maxY = Math.max(this.node1.getOutBounds().maxY, this.node2.getOutBounds().maxY);
            minY = Math.min(this.node1.getOutBounds().minY, this.node2.getOutBounds().minY);
        }
        else
        {
            maxY = Math.max(this.node1.getInBounds().maxY, this.node2.getInBounds().maxY);
            minY = Math.min(this.node1.getInBounds().minY, this.node2.getInBounds().minY);
        }
        if (pos.getY() > maxY || pos.getY() < minY) return false;
        final Vec3 e1 = new Vec3(this.getEnd1().getX(), this.getEnd1().getY(), this.getEnd1().getZ());
        final Vec3 e2 = new Vec3(this.getEnd2().getX(), this.getEnd2().getY(), this.getEnd2().getZ());
        final Vec3 e3 = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        Vec3 n = e2.subtract(e1).normalize();
        final Vec3 diff = e1.subtract(e3);
        n = n.cross(diff);
        return n.length() < size;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof Edge)) return false;
        return this.areSame((Edge) obj);
    }

    @Override
    public int hashCode()
    {
        return this.node1.getCenter().hashCode() ^ this.node2.getCenter().hashCode();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag edgeNbt = super.serializeNBT();
        edgeNbt.putUUID("n1", this.node1.id);
        edgeNbt.put("e1", NbtUtils.writeBlockPos(this.getEnd1()));
        edgeNbt.putUUID("n2", this.node2.id);
        edgeNbt.put("e2", NbtUtils.writeBlockPos(this.getEnd2()));
        edgeNbt.putInt("d", this.digInd);
        return edgeNbt;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        super.deserializeNBT(nbt);

        final UUID p1 = (nbt.getUUID("n1"));
        final UUID p2 = (nbt.getUUID("n2"));
        this.digInd = nbt.getInt("d");

        if (this.getTree() != null)
        {
            this.node1 = (Node) this.getTree().allParts.get(p1);
            this.node2 = (Node) this.getTree().allParts.get(p2);

            this.node1.edges.add(this);
            this.node2.edges.add(this);
        }
        this.setEnds(NbtUtils.readBlockPos(nbt.getCompound("e1")), NbtUtils.readBlockPos(nbt.getCompound("e2")));
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
        return this.withinDistance(pos, 3) && !this.isInside(pos);
    }

    public BlockPos getEnd1()
    {
        return this.end1;
    }

    public BlockPos getEnd2()
    {
        return this.end2;
    }

    public void setEnds(final BlockPos end1, final BlockPos end2)
    {
        this.end1 = end1;
        this.end2 = end2;

        if (end1.closerThan(end2, 1))
        {
            ThutCore.LOGGER.error("ERROR EDGE NOT VALID");
            new IllegalArgumentException().printStackTrace();
        }

        final double minX = Math.min(end1.getX(), end2.getX());
        final double maxX = Math.max(end1.getX(), end2.getX());

        final double minZ = Math.min(end1.getZ(), end2.getZ());
        final double maxZ = Math.max(end1.getZ(), end2.getZ());

        final double maxY = Math.max(this.node1.getInBounds().maxY, this.node2.getInBounds().maxY);
        final double minY = Math.min(this.node1.getInBounds().minY, this.node2.getInBounds().minY);

        final AABB min = new AABB(minX - 2, minY - 1, minZ - 2, maxX + 2, maxY + 1, maxZ + 2);
        final AABB max = new AABB(minX - 3, minY - 2, minZ - 3, maxX + 3, maxY + 2, maxZ + 3);
        this.setInBounds(min);
        this.setOutBounds(max);
    }

    @Override
    public void setDigDone(final long time)
    {
        super.setDigDone(time);
        this.node1.started = true;
        this.node2.started = true;
    }

    @Override
    public String toString()
    {
        return this.node1 + "<->" + this.node2;
    }
}
