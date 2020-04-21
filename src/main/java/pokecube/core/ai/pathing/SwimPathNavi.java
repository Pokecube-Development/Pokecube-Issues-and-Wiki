package pokecube.core.ai.pathing;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.SwimmerPathNavigator;
import net.minecraft.world.World;

public class SwimPathNavi extends SwimmerPathNavigator
{

    public SwimPathNavi(final MobEntity entitylivingIn, final World worldIn)
    {
        super(entitylivingIn, worldIn);
    }

}
