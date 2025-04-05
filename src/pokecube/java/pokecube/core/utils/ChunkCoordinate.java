package pokecube.core.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class ChunkCoordinate
{

    public static GlobalPos getChunkCoordFromWorldCoord(BlockPos pos, final Level world)
    {
        final int i = Mth.floor(pos.getX() >> 4);
        final int j = Mth.floor(pos.getY() >> 4);
        final int k = Mth.floor(pos.getZ() >> 4);
        pos = new BlockPos(i, j, k);
        return GlobalPos.of(world.dimension(), pos);
    }

    public static boolean isWithin(final GlobalPos a, final GlobalPos b, final int tolerance)
    {
        return a.dimension().equals(b.dimension()) && ChunkCoordinate.isWithin(a.pos(), b.pos(), tolerance);
    }

    public static boolean isWithin(final BlockPos a, final BlockPos b, final int tolerance)
    {
        final int dx = Math.abs(a.getX() - b.getX());
        final int dy = Math.abs(a.getY() - b.getY());
        final int dz = Math.abs(a.getZ() - b.getZ());
        return dx <= tolerance && dz <= tolerance && dy <= tolerance;
    }
}
