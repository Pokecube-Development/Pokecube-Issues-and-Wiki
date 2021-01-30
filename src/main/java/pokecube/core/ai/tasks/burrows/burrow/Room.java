package pokecube.core.ai.tasks.burrows.burrow;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class Room extends Part
{
    private float direction;
    private float size;

    private BlockPos centre;

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

    @Override
    public boolean isInside(final BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isOnShell(final BlockPos pos)
    {
        return false;
    }

    public BlockPos getCenter()
    {
        return this.centre;
    }

    public void setCenter(final BlockPos centre, final float size, final float direction)
    {
        this.centre = centre;
        this.size = size;
        this.direction = direction;
    }

    public float getSize()
    {
        return this.size;
    }
}
