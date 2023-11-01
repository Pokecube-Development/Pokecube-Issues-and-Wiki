package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.MovesUtils;

@AbilityProvider(name = "moxie")
public class Moxie extends Ability
{
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        final IPokemob targetMob = PokemobCaps.getPokemobFor(move.getTarget());
        if (targetMob == null) return;
        if (!targetMob.inCombat())
        {
            byte boost = IMoveConstants.ATTACK;
            MovesUtils.handleStats2(mob, mob.getOwner(), boost, IMoveConstants.RAISE);
        }
    }
}
