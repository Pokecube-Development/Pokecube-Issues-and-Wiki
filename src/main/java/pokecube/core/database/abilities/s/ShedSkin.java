package pokecube.core.database.abilities.s;

import net.minecraft.entity.LivingEntity;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;

public class ShedSkin extends Ability
{
    @Override
    public void onUpdate(IPokemob mob)
    {
        if (mob.getStatus() != IMoveConstants.STATUS_NON)
        {
            final LivingEntity poke = mob.getEntity();
            if (poke.ticksExisted % 20 == 0 && Math.random() < 0.3) mob.healStatus();
        }
    }
}
