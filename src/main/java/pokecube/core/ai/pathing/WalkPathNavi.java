package pokecube.core.ai.pathing;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.world.World;

public class WalkPathNavi extends GroundPathNavigator
{

    public WalkPathNavi(final MobEntity entitylivingIn, final World worldIn)
    {
        super(entitylivingIn, worldIn);
    }

}
