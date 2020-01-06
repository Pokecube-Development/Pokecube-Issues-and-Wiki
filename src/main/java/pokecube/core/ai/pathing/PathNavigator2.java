package pokecube.core.ai.pathing;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.world.World;

public abstract class PathNavigator2 extends PathNavigator
{

    public PathNavigator2(final MobEntity entityIn, final World worldIn)
    {
        super(entityIn, worldIn);
    }

    @Override
    /**
     * This override is to make the method public for our subclasses.
     */
    public PathFinder getPathFinder(final int p_179679_1_)
    {
        return null;
    }

}
