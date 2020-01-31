package pokecube.core.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import thut.api.maths.Vector3;

public class ChunkCoordinate extends BlockPos
{

    public static ChunkCoordinate getChunkCoordFromWorldCoord(final BlockPos pos, final int dimension)
    {
        return ChunkCoordinate.getChunkCoordFromWorldCoord(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public static ChunkCoordinate getChunkCoordFromWorldCoord(final int x, final int y, final int z, final int dim)
    {
        final int i = MathHelper.floor(x >> 4);
        final int j = MathHelper.floor(y >> 4);
        final int k = MathHelper.floor(z >> 4);
        return new ChunkCoordinate(i, j, k, dim);
    }

    public int dim;

    public ChunkCoordinate(final BlockPos pos, final int dimension)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public ChunkCoordinate(final int p_i1354_1_, final int p_i1354_2_, final int p_i1354_3_, final int dim)
    {
        super(p_i1354_1_, p_i1354_2_, p_i1354_3_);
        this.dim = dim;
    }

    public ChunkCoordinate(final Vector3 v, final int dim)
    {
        super(v.intX(), v.intY(), v.intZ());
        this.dim = dim;
    }

    @Override
    public boolean equals(final Object test)
    {
        if (!(test instanceof ChunkCoordinate)) return false;
        final ChunkCoordinate BlockPos = (ChunkCoordinate) test;
        return this.getX() == BlockPos.getX() && this.getY() == BlockPos.getY() && this.getZ() == BlockPos.getZ()
                && this.dim == BlockPos.dim;
    }

    @Override
    public int hashCode()
    {
        return this.getX() + this.getZ() << 8 + this.getY() << 16 + this.dim << 24;
    }

    public void writeToBuffer(final ByteBuf buffer)
    {
        buffer.writeInt(this.getX());
        buffer.writeInt(this.getY());
        buffer.writeInt(this.getZ());
        buffer.writeInt(this.dim);
    }
}
