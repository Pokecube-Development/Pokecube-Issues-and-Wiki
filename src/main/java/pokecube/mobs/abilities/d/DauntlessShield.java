package pokecube.mobs.abilities.d;

import net.minecraft.entity.LivingEntity;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;

public class DauntlessShield extends Ability
{

	@Override
    public void onAgress(final IPokemob mob, final LivingEntity target)
    {
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob != null) MovesUtils.handleStats2(mob, targetMob.getEntity(),
        		IMoveConstants.DEFENSE, IMoveConstants.RAISE);
    }
}
