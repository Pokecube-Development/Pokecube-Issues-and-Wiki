package thut.api.level.terrain;

import java.util.Objects;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class GlobalChunkPos
{
    private final ResourceKey<Level> world;
    private final ChunkPos pos;

    private final int hash;

    public GlobalChunkPos(final ResourceKey<Level> world, final ChunkPos pos)
    {
        this.world = world;
        this.pos = pos;
        this.hash = world.hashCode() | pos.z * 511 * 511 + pos.x;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) return true;
        else if (obj instanceof GlobalChunkPos pos)
            return Objects.equals(this.world, pos.world) && Objects.equals(this.pos, pos.pos);
        else return false;
    }

    @Override
    public int hashCode()
    {
        return this.hash;
    }
}
