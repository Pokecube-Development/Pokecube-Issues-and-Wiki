package pokecube.mobs.abilities.d;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.database.abilities.Ability;
import pokecube.core.moves.MovesUtils;

public class DauntlessShield extends Ability
{

	@Override
    public void onAgress(final IPokemob mob, final LivingEntity target)
    {
        final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
        if (targetMob != null) MovesUtils.handleStats2(mob, targetMob.getEntity(),
        		IMoveConstants.DEFENSE, IMoveConstants.RAISE);
    }
}
