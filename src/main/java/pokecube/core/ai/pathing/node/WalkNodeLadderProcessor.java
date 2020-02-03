package pokecube.core.ai.pathing.node;

import net.minecraft.block.BlockState;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class WalkNodeLadderProcessor extends WalkNodeProcessor
{

    @Override
    public int func_222859_a(final PathPoint[] pathOptions, final PathPoint point)
    {
        int i = super.func_222859_a(pathOptions, point);
        PathPoint pathpoint = this.getPoint(point.x, point.y + 1, point.z, Direction.UP);
        if (pathpoint != null && !pathpoint.visited) pathOptions[i++] = pathpoint;
        pathpoint = this.getPoint(point.x, point.y - 1, point.z, Direction.DOWN);
        if (pathpoint != null && !pathpoint.visited) pathOptions[i++] = pathpoint;
        for (final Direction side : Direction.Plane.HORIZONTAL)
        {
            pathpoint = this.getPoint(point.x + side.getXOffset(), point.y - 1, point.z + side.getZOffset(),
                    Direction.DOWN);
            if (pathpoint != null && !pathpoint.visited) pathOptions[i++] = pathpoint;
        }
        return i;
    }

    private PathPoint getJumpOff(final int x, final int y, final int z)
    {
        for (int i = x; i < x + this.entitySizeX; ++i)
            for (int j = y; j < y + this.entitySizeY; ++j)
                for (int k = z; k < z + this.entitySizeZ; ++k)
                {
                    final PathNodeType type = this.getPathNodeTypeRaw(this.blockaccess, i, j, k);
                    if (type != PathNodeType.OPEN && type != PathNodeType.WALKABLE) return null;
                }
        final PathPoint point = this.openPoint(x, y, z);

        boolean laddar = false;
        for (final Direction dir : Direction.Plane.HORIZONTAL)
            laddar = laddar || this.getLadder(x + dir.getXOffset(), y, z + dir.getZOffset()) != null;
        point.nodeType = PathNodeType.OPEN;
        point.costMalus += laddar ? 1 : 5;
        return point;
    }

    private PathPoint getLadder(final int x, final int y, final int z)
    {
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int i = x; i < x + this.entitySizeX; ++i)
            for (int j = y; j < y + this.entitySizeY; ++j)
                for (int k = z; k < z + this.entitySizeZ; ++k)
                {
                    final BlockState BlockState = this.blockaccess.getBlockState(pos.setPos(i, j, k));

                    if (BlockState.getBlock().isLadder(BlockState, this.blockaccess, pos, this.entity))
                    {
                        final PathPoint point = this.openPoint(x, y, z);
                        point.nodeType = PathNodeType.OPEN;
                        return point;
                    }
                }
        return null;
    }

    private PathPoint getPoint(final int x, final int y, final int z, final Direction direction)
    {
        if (direction == Direction.UP) return this.getLadder(x, y, z);
        else if (direction == Direction.DOWN) return this.getJumpOff(x, y, z);
        return null;
    }
}
