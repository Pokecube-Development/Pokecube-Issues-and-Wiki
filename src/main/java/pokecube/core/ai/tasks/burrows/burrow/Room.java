package pokecube.core.ai.tasks.burrows.burrow;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Room extends Part
{
    private float direction;
    private float size;

    private BlockPos centre;

    public Vec3 mid;

    private Vec3 entrance;
    private Vec3 exit;

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
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.setCenter(NbtUtils.readBlockPos(nbt), nbt.getFloat("size"), nbt.getFloat("dir"));
        super.deserializeNBT(nbt);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag nbt = super.serializeNBT();
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
        final Vec3 e1 = new Vec3(this.entrance.x(), this.entrance.y(), this.entrance.z());
        final Vec3 e2 = new Vec3(this.exit.x(), this.exit.y(), this.exit.z());
        final Vec3 e3 = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        Vec3 n = e2.subtract(e1).normalize();
        final Vec3 diff = e1.subtract(e3);
        n = n.cross(diff);
        return n.length() < size;
    }

    @Override
    public boolean isInside(final BlockPos pos)
    {
        if (!this.getInBounds().contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) return false;
        final Vec3 x0 = this.mid;
        Vec3 x = new Vec3(pos.getX(), pos.getY(), pos.getZ()).subtract(x0);
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
        final Vec3 x0 = this.mid;
        Vec3 x = new Vec3(pos.getX(), pos.getY(), pos.getZ()).subtract(x0);
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
        this.mid = new Vec3(centre.getX() + 0.5, centre.getY(), centre.getZ() + 0.5);
        this.direction = direction;
        this.size = size;
        final double dx = Math.sin(Math.toRadians(direction));
        final double dz = Math.cos(Math.toRadians(direction));
        this.entrance = this.mid.add(dx * size, 0, dz * size);
        final float dr = size + 7;
        this.exit = this.entrance.add(dx * dr, dr, dz * dr);
        final AABB min = new AABB(this.mid.add(-dr * 2, -2, -dr * 2), this.mid.add(dr * 2, dr, dr
                * 2));
        this.setInBounds(min);
        this.setOutBounds(min);
    }

    public float getSize()
    {
        return this.size;
    }
}
