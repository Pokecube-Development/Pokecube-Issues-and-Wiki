package pokecube.core.ai.pathing;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FlyPathNavi extends FlyingPathNavigation
{

    public FlyPathNavi(final Mob entityIn, final Level worldIn)
    {
        super(entityIn, worldIn);
    }

    @Override
    protected void followThePath()
    {
        final Vec3 vec3d = this.getTempMobPos();
        for (int i = this.path.getNextNodeIndex(); i < this.path.getNodeCount(); i++)
        {
            final Vec3 vec3d1 = this.path.getEntityPosAtNode(this.mob, i);
            if (this.canMoveDirectly(vec3d, vec3d1, 1, 1, 1)) if (i - 1 > this.path
                    .getNextNodeIndex()) this.path.setNextNodeIndex(i - 1);
        }
        super.followThePath();
    }
}
