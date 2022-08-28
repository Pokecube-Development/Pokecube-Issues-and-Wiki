package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.moves.Move_Base;

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
