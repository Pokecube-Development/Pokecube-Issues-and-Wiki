package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.moves.Move_Base;

public class UnseenFist extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        final Move_Base attack = move.getMove();

        if (move.pre) return;
        if (move.hit && attack.getAttackCategory(move.attacker) == IMoveConstants.CATEGORY_CONTACT)
        {
            move.failed = false;
        }
    }
}
