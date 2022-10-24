package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.moves.utils.MoveApplication;

public class Guts extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        move.stat_multipliers[Stats.ATTACK.ordinal()] = 1.5f;
    }
}
