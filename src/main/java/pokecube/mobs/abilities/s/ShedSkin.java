package pokecube.mobs.abilities.s;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.IMoveConstants;

public class ShedSkin extends Ability
{
    @Override
    public void onUpdate(IPokemob mob)
    {
        if (mob.getStatus() != IMoveConstants.STATUS_NON)
        {
            final LivingEntity poke = mob.getEntity();
            if (poke.tickCount % 20 == 0 && Math.random() < 0.3) mob.healStatus();
        }
    }
}
