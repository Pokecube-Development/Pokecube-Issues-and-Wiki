package pokecube.core.ai.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import pokecube.core.ai.pathing.processors.SwimAndWalkNodeProcessor;

public class SwimPathNavi extends WalkPathNavi
{

    public SwimPathNavi(final Mob entitylivingIn, final Level worldIn)
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
        final boolean lavaImmune = this.mob.getPathfindingMalus(BlockPathTypes.LAVA) > 0;
        if (!lavaImmune && fluid.is(FluidTags.LAVA)) return false;
        return !block.isSolidRender(this.level, pos) && !fluid.isEmpty();
    }
}
