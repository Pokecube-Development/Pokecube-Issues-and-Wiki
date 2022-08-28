package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;

public class Competitive extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (!move.pre)
        {

        }
        else if (mob == move.attacked)
        {
            boolean effect = false;
            for (final int element : move.attackedStatModification)
                if (element < 0)
                {
                    effect = true;
                    break;
                }
            if (effect)
            {
                move.attackedStatModification = move.attackedStatModification.clone();
                move.attackedStatModification[3] = IMoveConstants.RAISE;
            }
        }
        else if (mob == move.attacker)
        {
            boolean effect = false;
            for (final int element : move.attackerStatModification)
                if (element < 0)
                {
                    effect = true;
                    break;
                }
            if (effect)
            {
                move.attackerStatModification = move.attackerStatModification.clone();
                move.attackerStatModification[3] = IMoveConstants.RAISE;
            }
        }
    }
}
