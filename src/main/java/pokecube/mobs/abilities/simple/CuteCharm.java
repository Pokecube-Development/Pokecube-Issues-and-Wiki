package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;

public class CuteCharm extends Ability
{
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if (areWeUser(mob, move)) return;
        final MoveEntry attack = move.getMove();
        if (attack == null || (attack.getAttackCategory(move.getUser()) & IMoveConstants.CATEGORY_CONTACT) == 0) return;
        move.infatuate = move.infatuate || Math.random() > 0.7;
    }
}
