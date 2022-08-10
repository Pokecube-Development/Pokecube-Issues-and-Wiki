package pokecube.mobs.abilities.c;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.moves.Move_Base;
import pokecube.core.database.abilities.Ability;

public class CuteCharm extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob != move.attacked || move.pre || move.attacker == move.attacked) return;
        final Move_Base attack = move.getMove();
        if (attack == null || (attack.getAttackCategory() & IMoveConstants.CATEGORY_CONTACT) == 0) return;
        move.infatuateTarget = move.infatuateTarget || Math.random() > 0.7;
    }
}
