package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;

public class UnseenFist extends Ability
{
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        final MoveEntry attack = move.getMove();
        if (move.hit && attack.getAttackCategory(move.getUser()) == IMoveConstants.CATEGORY_CONTACT)
        {
            move.failed = false;
        }
    }
}
