package thut.core.common.world.utils;

import net.minecraft.util.math.BlockPos;
import thut.api.world.utils.Vector;

public class Vector_I extends Vector_Base<Integer>
{
    public Vector_I()
    {
        super();
    }

    public Vector_I(BlockPos pos)
    {
        super();
        this.array[0] = pos.getX();
        this.array[1] = pos.getY();
        this.array[2] = pos.getZ();
    }

    public Vector_I(Vector<Integer> pos)
    {
        super();
        this.add(pos);
    }

    @Override
    public void add(Vector<Integer> other)
    {
        final Integer[] v2 = other.getVector();
        final Integer[] v1 = this.array;
        v1[0] += v2[0];
        v1[1] += v2[1];
        v1[2] += v2[2];
    }

    @Override
    public int compareTo(Vector<Integer> o)
    {
        final Integer[] v2 = o.getVector();
        final Integer[] v1 = this.array;

        if (v2.length != 0) return 0;

        if (v1[1] == v2[1]) return v1[2] == v2[2] ? v1[0] - v2[0] : v1[2] - v2[2];
        else return v1[1] - v2[1];
    }

    public BlockPos getPos()
    {
        return new BlockPos(this.array[0], this.array[1], this.array[2]);
    }

    @Override
    void init()
    {
        this.array = new Integer[3];
    }

    @Override
    public void subtract(Vector<Integer> other)
    {
        final Integer[] v2 = other.getVector();
        final Integer[] v1 = this.array;
        v1[0] -= v2[0];
        v1[1] -= v2[1];
        v1[2] -= v2[2];
    }

}
