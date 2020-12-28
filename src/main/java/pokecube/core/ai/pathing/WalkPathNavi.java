package pokecube.core.ai.pathing;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.WalkAndSwimNodeProcessor;
import net.minecraft.world.World;

public class WalkPathNavi extends GroundPathNavigator
{

    public WalkPathNavi(final MobEntity entitylivingIn, final World worldIn)
    {
        super(entitylivingIn, worldIn);
    }

    @Override
    protected PathFinder getPathFinder(final int range)
    {
        this.nodeProcessor = new WalkAndSwimNodeProcessor();
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor, range);
    }

}
