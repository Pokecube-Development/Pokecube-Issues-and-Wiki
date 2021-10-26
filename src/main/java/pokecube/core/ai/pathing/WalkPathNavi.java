package pokecube.core.ai.pathing;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;

public class WalkPathNavi extends GroundPathNavigation
{

    public WalkPathNavi(final Mob entitylivingIn, final Level worldIn)
    {
        super(entitylivingIn, worldIn);
    }

    @Override
    protected PathFinder createPathFinder(final int range)
    {
        this.nodeEvaluator = new AmphibiousNodeEvaluator(true);
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, range);
    }

}
