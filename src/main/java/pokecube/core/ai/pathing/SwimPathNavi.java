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
    protected PathFinder getPathFinder(final int range)
    {
        this.nodeProcessor = new SwimAndWalkNodeProcessor();
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor, range);
    }

    @Override
    public boolean canEntityStandOnPos(final BlockPos pos)
    {
        final boolean superStand = super.canEntityStandOnPos(pos);
        if (superStand) return true;
        final BlockState block = this.world.getBlockState(pos);
        final FluidState fluid = this.world.getFluidState(pos);
        final boolean lavaImmune = this.entity.getPathPriority(PathNodeType.LAVA) > 0;
        if (!lavaImmune && fluid.isTagged(FluidTags.LAVA)) return false;
        return !block.isOpaqueCube(this.world, pos) && !fluid.isEmpty();
    }
}
