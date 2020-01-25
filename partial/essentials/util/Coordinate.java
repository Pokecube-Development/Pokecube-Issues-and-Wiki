package thut.essentials.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class Coordinate implements Comparable<Coordinate>
{

    public static Coordinate getChunkCoordFromWorldCoord(BlockPos pos, int dimension)
    {
        return getChunkCoordFromWorldCoord(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public static Coordinate getChunkCoordFromWorldCoord(int x, int y, int z, int dim)
    {
        int i = MathHelper.floor(x / 16.0D);
        int j = MathHelper.floor(y / 16.0D);
        int k = MathHelper.floor(z / 16.0D);
        return new Coordinate(i, j, k, dim);
    }

    public int x;
    public int y;
    public int z;
    public int dim;

    public Coordinate(BlockPos pos, int dimension)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public Coordinate(int x, int y, int z, int dim)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Coordinate)) { return false; }
        Coordinate BlockPos = (Coordinate) obj;
        return x == BlockPos.x && y == BlockPos.y && this.z == BlockPos.z && this.dim == BlockPos.dim;
    }

    @Override
    public int hashCode()
    {
        return x + z << 8 + y << 16 + this.dim << 24;
    }

    @Override
    public int compareTo(Coordinate p_compareTo_1_)
    {
        return y == p_compareTo_1_.y
                ? (this.z == p_compareTo_1_.z ? x - p_compareTo_1_.x
                        : this.dim == p_compareTo_1_.dim ? this.z - p_compareTo_1_.z : this.dim - p_compareTo_1_.dim)
                : this.y - p_compareTo_1_.y;
    }

    @Override
    public String toString()
    {
        return "CCxyzw: " + x + " " + y + " " + z + " " + dim;
    }
}
