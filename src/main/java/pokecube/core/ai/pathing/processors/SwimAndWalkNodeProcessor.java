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
    public void prepare(final PathNavigationRegion level, final Mob mob)
    {
        super.prepare(level, mob);
        // Super called handled storing the water priority
        mob.setPathfindingMalus(BlockPathTypes.WATER, 2.0F);
        this.oldWalkPriority = mob.getPathfindingMalus(BlockPathTypes.WALKABLE);
        mob.setPathfindingMalus(BlockPathTypes.WALKABLE, 6.0F);
        this.oldWaterBorderPriority = mob.getPathfindingMalus(BlockPathTypes.WATER_BORDER);
        mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 4.0F);
    }

    /**
     * This method is called when all nodes have been processed and PathEntity
     * is created. {@link net.minecraft.world.pathfinder.WalkNodeProcessor
     * WalkNodeProcessor} uses this to change its field
     * {@link net.minecraft.world.pathfinder.WalkNodeProcessor#avoidsWater
     * avoidsWater}
     */
    @Override
    public void done()
    {
        this.mob.setPathfindingMalus(BlockPathTypes.WALKABLE, this.oldWalkPriority);
        this.mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, this.oldWaterBorderPriority);
        super.done();
    }

}
