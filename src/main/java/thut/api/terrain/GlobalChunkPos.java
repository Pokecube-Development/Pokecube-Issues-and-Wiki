package thut.api.terrain;

import java.util.Objects;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class GlobalChunkPos
{
    private final RegistryKey<World> world;
    private final ChunkPos      pos;

    private final int hash;

    public GlobalChunkPos(final RegistryKey<World> world, final ChunkPos pos)
    {
        this.world = world;
        this.pos = pos;
        this.hash = world.hashCode() | pos.z * 511 * 511 + pos.x;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) return true;
        else if (obj != null && this.getClass() == obj.getClass())
        {
            final GlobalChunkPos globalpos = (GlobalChunkPos) obj;
            return Objects.equals(this.world, globalpos.world) && Objects.equals(this.pos, globalpos.pos);
        }
        else return false;
    }

    @Override
    public int hashCode()
    {
        return this.hash;
    }
}
