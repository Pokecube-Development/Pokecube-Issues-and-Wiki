package pokecube.core.database.abilities.s;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class Static extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        final Move_Base attack = move.getMove();
        final IPokemob attacker = move.attacker;
        if (attacker == mob || move.pre || attacker == move.attacked) return;
        if (move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7)
            move.attacker.setStatus(IMoveConstants.STATUS_PAR);
    }
}
