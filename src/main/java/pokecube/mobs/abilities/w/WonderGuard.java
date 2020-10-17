package pokecube.mobs.abilities.w;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.utils.PokeType;

public class WonderGuard extends Ability
{
   /* @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {

        final Move_Base attack = move.getMove();

        final IPokemob attacker = move.attacker;

        if (attacker == mob || !move.pre || attacker == move.attacked) return;

        final float eff = PokeType.getAttackEfficiency(attack.getType(move.attacker), mob.getType1(), mob.getType2());
        if (eff <= 1 && attack.getPWR(attacker, mob.getEntity()) > 0) move.canceled = true;
    }*/
}
