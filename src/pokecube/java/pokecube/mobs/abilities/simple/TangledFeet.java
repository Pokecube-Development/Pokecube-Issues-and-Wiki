package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.damage.effects.StatusEffects;

@AbilityProvider(name = "tangled-feet")
public class TangledFeet extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    { // Increase evasion when confused.
        if (mob.getEntity().hasEffect(StatusEffects.CONFUSE))
            MovesUtils.handleStats2(mob, mob.getOwner(), IMoveConstants.EVASION, IMoveConstants.RAISE);
    }
}
