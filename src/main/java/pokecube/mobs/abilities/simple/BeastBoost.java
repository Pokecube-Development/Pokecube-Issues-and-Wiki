package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.MovesUtils;

@AbilityProvider(name = "beast-boost")
public class BeastBoost extends Ability
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
            int stat = mob.getStat(Stats.ATTACK, true);
            int tmp;
            if ((tmp = mob.getStat(Stats.SPATTACK, true)) > stat)
            {
                stat = tmp;
                boost = IMoveConstants.SPATACK;
            }
            if ((tmp = mob.getStat(Stats.DEFENSE, true)) > stat)
            {
                stat = tmp;
                boost = IMoveConstants.DEFENSE;
            }
            if ((tmp = mob.getStat(Stats.SPDEFENSE, true)) > stat)
            {
                stat = tmp;
                boost = IMoveConstants.SPDEFENSE;
            }
            if ((tmp = mob.getStat(Stats.VIT, true)) > stat)
            {
                stat = tmp;
                boost = IMoveConstants.VIT;
            }
            MovesUtils.handleStats2(mob, mob.getOwner(), boost, IMoveConstants.RAISE);
        }
    }
}
