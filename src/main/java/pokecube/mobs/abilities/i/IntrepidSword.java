package pokecube.mobs.abilities.i;

import net.minecraft.entity.LivingEntity;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;

public class IntrepidSword extends Ability
{
	@Override
    public void onAgress(IPokemob mob, LivingEntity target)
    {
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob != null) MovesUtils.handleStats2(mob, mob.getEntity(), IMoveConstants.ATTACK,
                IMoveConstants.RAISE);
    }
}
