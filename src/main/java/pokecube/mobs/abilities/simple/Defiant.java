package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.MovesUtils;

@AbilityProvider(name = "defiant")
public class Defiant extends Ability
{
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if (move.applied_stat_effects == null) return;
        for (int i = 0; i < move.applied_stat_effects.diffs().length; i++)
        {
            if (move.applied_stat_effects.diffs()[i] < 0)
            {
                MovesUtils.handleStats2(mob, mob.getOwner(), IMoveConstants.ATTACK, IMoveConstants.SHARP);
            }
        }
    }
}
