package pokecube.core.ai.pathing;

import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.core.ai.pathing.processors.SwimAndWalkNodeProcessor;

public class SwimPathNavi extends WalkPathNavi
{

    public SwimPathNavi(final MobEntity entitylivingIn, final World worldIn)
    {
        super(entitylivingIn, worldIn);
    }

    @Override
    protected PathFinder createPathFinder(final int range)
    {
        this.nodeEvaluator = new SwimAndWalkNodeProcessor();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, range);
    }

    @Override
    public boolean isStableDestination(final BlockPos pos)
    {
        final boolean superStand = super.isStableDestination(pos);
        if (superStand) return true;
        final BlockState block = this.level.getBlockState(pos);
        final FluidState fluid = this.level.getFluidState(pos);
        final boolean lavaImmune = this.mob.getPathfindingMalus(PathNodeType.LAVA) > 0;
        if (!lavaImmune && fluid.is(FluidTags.LAVA)) return false;
        return !block.isSolidRender(this.level, pos) && !fluid.isEmpty();
    }
}
