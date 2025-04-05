package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.moves.MovesUtils;

@AbilityProvider(name = "dauntless-shield")
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
