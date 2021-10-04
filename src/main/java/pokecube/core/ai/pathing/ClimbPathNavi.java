package pokecube.core.ai.pathing;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.Level;

public class ClimbPathNavi extends WallClimberNavigation
{

    public ClimbPathNavi(final Mob entityLivingIn, final Level worldIn)
    {
        super(entityLivingIn, worldIn);
    }

}
