package pokecube.mobs.abilities.w;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.Move_Base;
import pokecube.api.utils.PokeType;

public class WonderGuard extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {

        final Move_Base attack = move.getMove();

        final IPokemob attacker = move.attacker;

        if (attacker == mob || !move.pre || attacker == move.attacked) return;

        final float eff = PokeType.getAttackEfficiency(attack.getType(move.attacker), mob.getType1(), mob.getType2());
        if (eff <= 1 && attack.getPWR(attacker, mob.getEntity()) > 0) move.canceled = true;
    }
}
