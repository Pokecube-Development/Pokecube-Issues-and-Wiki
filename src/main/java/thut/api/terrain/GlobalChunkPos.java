package thut.api.terrain;

import java.util.Objects;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;

public class GlobalChunkPos
{
    private final DimensionType dimension;
    private final ChunkPos      pos;

    private final int hash;

    public GlobalChunkPos(final DimensionType dimension, final ChunkPos pos)
    {
        this.dimension = dimension;
        this.pos = pos;
        this.hash = (dimension.getId() + pos.z * 511) * 511 + pos.x;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) return true;
        else if (obj != null && this.getClass() == obj.getClass())
        {
            final GlobalChunkPos globalpos = (GlobalChunkPos) obj;
            return Objects.equals(this.dimension, globalpos.dimension) && Objects.equals(this.pos, globalpos.pos);
        }
        else return false;
    }

    @Override
    public int hashCode()
    {
        return this.hash;
    }
}
