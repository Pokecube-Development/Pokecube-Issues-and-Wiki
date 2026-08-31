package pokecube.core.ai.pathing.processors;

import java.util.Arrays;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import thut.core.common.ThutCore;

/**
 * This is a mirror of WalkNodeProcessor, except it prioritizes water a bit
 * higher
 */
public class SwimAndWalkNodeProcessor extends NodeEvaluator
{
    private final SwimNodeEvaluator swimmer;
    private final WalkNodeEvaluator walker;

    private float oldWalkPriority;
    private float oldWaterBorderPriority;

    Node[] nodes_swim = new Node[32];
    Node[] nodes_walk = new Node[32];

    MutableBlockPos pos = new MutableBlockPos();

    public SwimAndWalkNodeProcessor()
    {
        swimmer = new SwimNodeEvaluator(true);
        walker = new WalkNodeEvaluator();

        swimmer.setCanFloat(true);
    }

    @Override
    public void prepare(final PathNavigationRegion level, final Mob mob)
    {
        super.prepare(level, mob);
        swimmer.prepare(level, mob);
        walker.prepare(level, mob);
        // Super called handled storing the water priority
        mob.setPathfindingMalus(PathType.WATER, 2.0F);
        this.oldWalkPriority = mob.getPathfindingMalus(PathType.WALKABLE);
        mob.setPathfindingMalus(PathType.WALKABLE, 6.0F);
        this.oldWaterBorderPriority = mob.getPathfindingMalus(PathType.WATER_BORDER);
        mob.setPathfindingMalus(PathType.WATER_BORDER, 4.0F);
    }

    @Override
    public void done()
    {
        this.mob.setPathfindingMalus(PathType.WALKABLE, this.oldWalkPriority);
        this.mob.setPathfindingMalus(PathType.WATER_BORDER, this.oldWaterBorderPriority);
        super.done();
        swimmer.done();
        walker.done();
    }

    @Override
    public Node getStart()
    {
        var swim = swimmer.getStart();
        var walk = walker.getStart();
        var water = this.mob.isInWater();
        return water ? swim == null ? walk : swim : walk == null ? swim : walk;
    }

    @Override
    public Target getTarget(double x, double y, double z)
    {
        pos.set(x, y, z);
        var swim = swimmer.getTarget(x, y, z);
        var walk = walker.getTarget(x, y, z);
        var water = !currentContext.level().getFluidState(pos).isEmpty();
        return water ? swim == null ? walk : swim : walk == null ? swim : walk;
    }

    @Override
    public int getNeighbors(Node[] toFill, Node node)
    {
        Arrays.fill(nodes_swim, null);
        Arrays.fill(nodes_walk, null);
        pos.set(node.x, node.y, node.z);
        var water = !currentContext.level().getFluidState(pos).isEmpty();
        int is = swimmer.getNeighbors(nodes_swim, node);
        int iw = walker.getNeighbors(nodes_walk, node);
        int k = 0;

        var node1 = water ? nodes_swim : nodes_walk;
        var node2 = water ? nodes_walk : nodes_swim;

        int i1 = water ? is : iw;
        int i2 = water ? iw : is;

        for (int i = 0; i < i1; i++)
        {
            if (node1[i] != null) toFill[k++] = node1[i];
        }
        for (int i = 0; i < i2; i++)
        {
            if (node2[i] != null) toFill[k++] = node2[i];
            if (k > 30)
            {
                ThutCore.logInfo("Uh Oh, out of room for nodes");
                break;
            }
        }
        return k;
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext level, int x, int y, int z, Mob mob)
    {
        var walk = walker.getPathTypeOfMob(level, x, y, z, mob);
        var swim = swimmer.getPathTypeOfMob(level, x, y, z, mob);
        var water = swim != PathType.BLOCKED;
        return water ? swim == null ? walk : swim : walk == null ? swim : walk;
    }

    @Override
    public PathType getPathType(PathfindingContext level, int x, int y, int z)
    {
        var walk = walker.getPathType(level, x, y, z);
        var swim = swimmer.getPathType(level, x, y, z);
        var water = swim != PathType.BLOCKED;
        return water ? swim == null ? walk : swim : walk == null ? swim : walk;
    }

}