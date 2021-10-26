package pokecube.core.ai.pathing;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.ClimberPathNavigator;
import net.minecraft.world.World;

public class ClimbPathNavi extends ClimberPathNavigator
{

    public ClimbPathNavi(final MobEntity entityLivingIn, final World worldIn)
    {
        super(entityLivingIn, worldIn);
    }

}
