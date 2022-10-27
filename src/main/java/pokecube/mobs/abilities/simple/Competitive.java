package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "competitive")
public class Competitive extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;

        boolean effect = false;
        for (final int element : move.stat_effects) if (element < 0)
        {
            effect = true;
            break;
        }
        if (effect) move.stat_effects[3] = IMoveConstants.RAISE;
    }
}
