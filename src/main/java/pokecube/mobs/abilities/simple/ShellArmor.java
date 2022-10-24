package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;

public class ShellArmor extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (areWeTarget(mob, move)) move.crit = -1;
    }
}
