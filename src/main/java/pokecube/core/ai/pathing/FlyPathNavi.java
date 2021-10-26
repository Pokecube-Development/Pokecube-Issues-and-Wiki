package pokecube.core.ai.pathing;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class FlyPathNavi extends FlyingPathNavigator
{

    public FlyPathNavi(final MobEntity entityIn, final World worldIn)
    {
        super(entityIn, worldIn);
    }

    @Override
    protected void followThePath()
    {
        final Vector3d vec3d = this.getTempMobPos();
        for (int i = this.path.getNextNodeIndex(); i < this.path.getNodeCount(); i++)
        {
            final Vector3d vec3d1 = this.path.getEntityPosAtNode(this.mob, i);
            if (this.canMoveDirectly(vec3d, vec3d1, 1, 1, 1)) if (i - 1 > this.path
                    .getNextNodeIndex()) this.path.setNextNodeIndex(i - 1);
        }
        super.followThePath();
    }
}
