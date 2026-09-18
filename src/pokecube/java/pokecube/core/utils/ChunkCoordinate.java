package pokecube.core.utils;

import net.minecraft.core.BlockPos;

public class ChunkCoordinate
{
    public static boolean isWithin(final BlockPos a, final BlockPos b, final int tolerance)
    {
        final int dx = Math.abs(a.getX() - b.getX());
        final int dy = Math.abs(a.getY() - b.getY());
        final int dz = Math.abs(a.getZ() - b.getZ());
        return dx <= tolerance && dz <= tolerance && dy <= tolerance;
    }
}
