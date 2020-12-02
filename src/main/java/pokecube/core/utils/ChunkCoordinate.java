package pokecube.core.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ChunkCoordinate
{

    public static GlobalPos getChunkCoordFromWorldCoord(BlockPos pos, final World world)
    {
        final int i = MathHelper.floor(pos.getX() >> 4);
        final int j = MathHelper.floor(pos.getY() >> 4);
        final int k = MathHelper.floor(pos.getZ() >> 4);
        pos = new BlockPos(i, j, k);
        return GlobalPos.getPosition(world.getDimensionKey(), pos);
    }

    public static boolean isWithin(final GlobalPos a, final GlobalPos b, final int tolerance)
    {
        return a.getDimension().equals(b.getDimension()) && ChunkCoordinate.isWithin(a.getPos(), b.getPos(), tolerance);
    }

    public static boolean isWithin(final BlockPos a, final BlockPos b, final int tolerance)
    {
        final int dx = Math.abs(a.getX() - b.getX());
        final int dy = Math.abs(a.getY() - b.getY());
        final int dz = Math.abs(a.getZ() - b.getZ());
        return dx <= tolerance && dz <= tolerance && dy <= tolerance;
    }
}
