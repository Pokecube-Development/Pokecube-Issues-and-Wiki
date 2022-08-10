package pokecube.mobs.abilities.e;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.moves.Move_Base;
import pokecube.core.database.abilities.Ability;
import pokecube.core.utils.PokeType;
import thut.core.common.ThutCore;

public class EffectSpore extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        final Move_Base attack = move.getMove();

        final IPokemob attacker = move.attacker;
        if (attacker == mob || move.pre || attacker == move.attacked || attacker.isType(PokeType.getType("grass")))
            return;
        if (move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7)
        {
            final int num = ThutCore.newRandom().nextInt(30);
            if (num < 9) move.attacker.setStatus(IMoveConstants.STATUS_PSN);
            if (num < 19) move.attacker.setStatus(IMoveConstants.STATUS_PAR);
            else move.attacker.setStatus(IMoveConstants.STATUS_SLP);
        }
    }
}
