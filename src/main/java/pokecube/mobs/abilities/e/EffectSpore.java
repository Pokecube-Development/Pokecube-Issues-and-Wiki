package pokecube.mobs.abilities.e;

import java.util.Random;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.utils.PokeType;

public class EffectSpore extends Ability
{
   /* @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        final Move_Base attack = move.getMove();

        final IPokemob attacker = move.attacker;
        if (attacker == mob || move.pre || attacker == move.attacked || attacker.isType(PokeType.getType("grass")))
            return;
        if (move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7)
        {
            final int num = new Random().nextInt(30);
            if (num < 9) move.attacker.setStatus(IMoveConstants.STATUS_PSN);
            if (num < 19) move.attacker.setStatus(IMoveConstants.STATUS_PAR);
            else move.attacker.setStatus(IMoveConstants.STATUS_SLP);
        }
    }*/
}
