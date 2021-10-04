package pokecube.core.ai.pathing.processors;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * This is a mirror of WalkNodeProcessor, except it prioritizes water a bit
 * higher
 */
public class SwimAndWalkNodeProcessor extends WalkNodeEvaluator
{
    private float oldWalkPriority;
    private float oldWaterBorderPriority;

    @Override
    public void prepare(final PathNavigationRegion p_225578_1_, final Mob p_225578_2_)
    {
        super.prepare(p_225578_1_, p_225578_2_);
        // Super called handled storing the water priority
        p_225578_2_.setPathfindingMalus(BlockPathTypes.WATER, 2.0F);
        this.oldWalkPriority = p_225578_2_.getPathfindingMalus(BlockPathTypes.WALKABLE);
        p_225578_2_.setPathfindingMalus(BlockPathTypes.WALKABLE, 6.0F);
        this.oldWaterBorderPriority = p_225578_2_.getPathfindingMalus(BlockPathTypes.WATER_BORDER);
        p_225578_2_.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 4.0F);
    }

    /**
     * This method is called when all nodes have been processed and PathEntity
     * is created.
     * {@link net.minecraft.world.pathfinder.WalkNodeProcessor
     * WalkNodeProcessor} uses this to change its field {@link
     * net.minecraft.world.pathfinder.WalkNodeProcessor#avoidsWater avoidsWater}
     */
    @Override
    public void done()
    {
        this.mob.setPathfindingMalus(BlockPathTypes.WALKABLE, this.oldWalkPriority);
        this.mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, this.oldWaterBorderPriority);
        super.done();
    }

}
