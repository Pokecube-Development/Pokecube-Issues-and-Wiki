package pokecube.core.ai.pathing.node;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.FlaggedPathPoint;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import pokecube.core.ai.pathing.MultiNodeNavigator;

public class MultiNodeWrapper extends NodeProcessor
{
    final MultiNodeNavigator  navi;
    private final PathPoint[] pathOptionsA = new PathPoint[32];
    private final PathPoint[] pathOptionsB = new PathPoint[32];

    public MultiNodeWrapper(final MultiNodeNavigator multiNodeNavigator)
    {
        this.navi = multiNodeNavigator;
    }

    @Override
    public int func_222859_a(final PathPoint[] pathOptions, final PathPoint point)
    {
        final int a = this.navi.a.func_222859_a(this.pathOptionsA, point);
        final int b = this.navi.b.func_222859_a(this.pathOptionsB, point);
        final int num = Math.min(a + b, 32);
        for (int i = 0; i < a; i++)
        {
            pathOptions[i] = this.pathOptionsA[i];
            if (pathOptions[i] == null) throw new IllegalArgumentException("Error with finding Path Options!");
        }
        for (int i = a; i < num; i++)
        {
            pathOptions[i] = this.pathOptionsB[i - a];
            if (pathOptions[i] == null) throw new IllegalArgumentException("Error with finding Path Options!");
        }
        return num;
    }

    @Override
    public FlaggedPathPoint func_224768_a(final double x, final double y, final double z)
    {
        FlaggedPathPoint a = this.navi.a.func_224768_a(x, y, z);
        if (a == null) a = this.navi.b.func_224768_a(x, y, z);
        return a;
    }

    @Override
    public PathNodeType getPathNodeType(final IBlockReader blockaccessIn, final int x, final int y, final int z)
    {
        final PathNodeType a = this.navi.a.getPathNodeType(blockaccessIn, x, y, z);
        if (a == PathNodeType.WALKABLE) return a;
        final PathNodeType b = this.navi.b.getPathNodeType(blockaccessIn, x, y, z);
        if (b == PathNodeType.WALKABLE) return b;
        if (a == PathNodeType.OPEN) return a;
        if (b == PathNodeType.OPEN) return b;
        return null;
    }

    @Override
    public PathNodeType getPathNodeType(final IBlockReader blockaccessIn, final int x, final int y, final int z,
            final MobEntity MobEntityIn, final int xSize, final int ySize, final int zSize,
            final boolean canBreakDoorsIn, final boolean canEnterDoorsIn)
    {
        final PathNodeType a = this.navi.a.getPathNodeType(blockaccessIn, x, y, z, MobEntityIn, xSize, ySize, zSize,
                canBreakDoorsIn, canEnterDoorsIn);
        if (a == PathNodeType.WALKABLE || a == PathNodeType.OPEN) return a;
        final PathNodeType b = this.navi.b.getPathNodeType(blockaccessIn, x, y, z, MobEntityIn, xSize, ySize, zSize,
                canBreakDoorsIn, canEnterDoorsIn);
        if (b == PathNodeType.WALKABLE || b == PathNodeType.OPEN) return b;
        return a;
    }

    @Override
    public PathPoint getStart()
    {
        final PathPoint a = this.navi.a.getStart();
        return a;
    }

    @Override
    public void init(final IWorldReader sourceIn, final MobEntity mob)
    {
        super.init(sourceIn, mob);
        this.navi.a.init(sourceIn, mob);
        this.navi.b.init(sourceIn, mob);
    }

    @Override
    public void postProcess()
    {
        super.postProcess();
        this.navi.a.postProcess();
        this.navi.b.postProcess();
    }

}
