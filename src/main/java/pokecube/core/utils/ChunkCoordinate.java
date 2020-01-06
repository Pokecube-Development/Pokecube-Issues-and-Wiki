package pokecube.core.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import thut.api.maths.Vector3;

public class ChunkCoordinate extends BlockPos
{

    public static ChunkCoordinate getChunkCoordFromWorldCoord(BlockPos pos, int dimension)
    {
        return ChunkCoordinate.getChunkCoordFromWorldCoord(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public static ChunkCoordinate getChunkCoordFromWorldCoord(int x, int y, int z, int dim)
    {
        final int i = MathHelper.floor(x / 16.0D);
        final int j = MathHelper.floor(y / 16.0D);
        final int k = MathHelper.floor(z / 16.0D);
        return new ChunkCoordinate(i, j, k, dim);
    }

    public int dim;

    public ChunkCoordinate(BlockPos pos, int dimension)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public ChunkCoordinate(int p_i1354_1_, int p_i1354_2_, int p_i1354_3_, int dim)
    {
        super(p_i1354_1_, p_i1354_2_, p_i1354_3_);
        this.dim = dim;
    }

    public ChunkCoordinate(Vector3 v, int dim)
    {
        super(v.intX(), v.intY(), v.intZ());
        this.dim = dim;
    }

    @Override
    public boolean equals(Object p_equals_1_)
    {
        if (!(p_equals_1_ instanceof ChunkCoordinate)) return false;
        final ChunkCoordinate BlockPos = (ChunkCoordinate) p_equals_1_;
        return this.getX() == BlockPos.getX() && this.getY() == BlockPos.getY() && this.getZ() == BlockPos.getZ()
                && this.dim == BlockPos.dim;
    }

    @Override
    public int hashCode()
    {
        return this.getX() + this.getZ() << 8 + this.getY() << 16 + this.dim << 24;
    }

    public void writeToBuffer(ByteBuf buffer)
    {
        buffer.writeInt(this.getX());
        buffer.writeInt(this.getY());
        buffer.writeInt(this.getZ());
        buffer.writeInt(this.dim);
    }
}
