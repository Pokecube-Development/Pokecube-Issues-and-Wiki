package pokecube.core.ai.pathing;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FlyPathNavi extends FlyingPathNavigator
{

    public FlyPathNavi(final MobEntity entityIn, final World worldIn)
    {
        super(entityIn, worldIn);
    }

    @Override
    protected void pathFollow()
    {
        final Vec3d vec3d = this.getEntityPosition();
        for (int i = this.currentPath.getCurrentPathIndex(); i < this.currentPath.getCurrentPathLength(); i++)
        {
            final Vec3d vec3d1 = this.currentPath.getVectorFromIndex(this.entity, i);
            if (this.isDirectPathBetweenPoints(vec3d, vec3d1, 1, 1, 1)) if (i - 1 > this.currentPath
                    .getCurrentPathIndex()) this.currentPath.setCurrentPathIndex(i - 1);
        }
        super.pathFollow();
    }
}
