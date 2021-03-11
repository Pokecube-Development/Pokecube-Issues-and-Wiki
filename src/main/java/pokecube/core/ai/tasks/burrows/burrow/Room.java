package pokecube.core.ai.tasks.burrows.burrow;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class Room extends Part
{
    private float direction;
    private float size;

    private BlockPos centre;

    public Vector3d mid;

    private Vector3d entrance;
    private Vector3d exit;

    public Room()
    {
        this(0, 1);
    }

    public Room(final float direction, final float size)
    {
        this.direction = direction;
        this.size = size;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        this.setCenter(NBTUtil.readBlockPos(nbt), nbt.getFloat("size"), nbt.getFloat("dir"));
        super.deserializeNBT(nbt);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = super.serializeNBT();
        nbt.putInt("X", this.getCenter().getX());
        nbt.putInt("Y", this.getCenter().getY());
        nbt.putInt("Z", this.getCenter().getZ());
        nbt.putFloat("dir", this.direction);
        nbt.putFloat("size", this.size);
        return nbt;
    }

    public boolean withinDistance(final BlockPos pos, final double size)
    {
        final double minY = this.centre.getY();
        final double maxY = minY + 7;
        if (pos.getY() > maxY || pos.getY() < minY) return false;
        final Vector3d e1 = new Vector3d(this.entrance.x(), this.entrance.y(), this.entrance.z());
        final Vector3d e2 = new Vector3d(this.exit.x(), this.exit.y(), this.exit.z());
        final Vector3d e3 = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
        Vector3d n = e2.subtract(e1).normalize();
        final Vector3d diff = e1.subtract(e3);
        n = n.cross(diff);
        return n.length() < size;
    }

    @Override
    public boolean isInside(final BlockPos pos)
    {
        if (!this.getInBounds().contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) return false;
        final Vector3d x0 = this.mid;
        Vector3d x = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).subtract(x0);
        x = x.multiply(1, this.size / 2, 1);
        // check if in the main room
        final double r = x.length();
        // This is in he main room itself
        if (r <= this.size && x.y >= 0) return true;
        // Otherwise check if we are in the correct direction.
        return this.withinDistance(pos, 1 + this.size / 4);
    }

    @Override
    public boolean isOnShell(final BlockPos pos)
    {
        if (!this.getOutBounds().contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) return false;
        final Vector3d x0 = this.mid;
        Vector3d x = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).subtract(x0);
        x = x.multiply(1, this.size / 2, 1);
        // check if in the main room
        final double r = x.length();
        return r <= this.size + 2 && x.y > -2;
    }

    public BlockPos getCenter()
    {
        return this.centre;
    }

    public void setCenter(final BlockPos centre, final float size, final float direction)
    {
        this.centre = centre;
        this.mid = new Vector3d(centre.getX() + 0.5, centre.getY(), centre.getZ() + 0.5);
        this.direction = direction;
        this.size = size;
        final double dx = Math.sin(Math.toRadians(direction));
        final double dz = Math.cos(Math.toRadians(direction));
        this.entrance = this.mid.add(dx * size, 0, dz * size);
        final float dr = size + 7;
        this.exit = this.entrance.add(dx * dr, dr, dz * dr);
        final AxisAlignedBB min = new AxisAlignedBB(this.mid.add(-dr * 2, -2, -dr * 2), this.mid.add(dr * 2, dr, dr
                * 2));
        this.setInBounds(min);
        this.setOutBounds(min);
    }

    public float getSize()
    {
        return this.size;
    }
}
