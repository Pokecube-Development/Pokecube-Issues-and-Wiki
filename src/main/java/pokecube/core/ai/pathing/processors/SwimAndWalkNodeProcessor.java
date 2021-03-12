package pokecube.core.ai.pathing.processors;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.world.Region;

/**
 * This is a mirror of WalkNodeProcessor, except it prioritizes water a bit
 * higher
 */
public class SwimAndWalkNodeProcessor extends WalkNodeProcessor
{
    private float oldWalkPriority;
    private float oldWaterBorderPriority;

    @Override
    public void prepare(final Region p_225578_1_, final MobEntity p_225578_2_)
    {
        super.prepare(p_225578_1_, p_225578_2_);
        // Super called handled storing the water priority
        p_225578_2_.setPathfindingMalus(PathNodeType.WATER, 2.0F);
        this.oldWalkPriority = p_225578_2_.getPathfindingMalus(PathNodeType.WALKABLE);
        p_225578_2_.setPathfindingMalus(PathNodeType.WALKABLE, 6.0F);
        this.oldWaterBorderPriority = p_225578_2_.getPathfindingMalus(PathNodeType.WATER_BORDER);
        p_225578_2_.setPathfindingMalus(PathNodeType.WATER_BORDER, 4.0F);
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
        this.mob.setPathfindingMalus(PathNodeType.WALKABLE, this.oldWalkPriority);
        this.mob.setPathfindingMalus(PathNodeType.WATER_BORDER, this.oldWaterBorderPriority);
        super.done();
    }

}
