package pokecube.core.database.abilities.c;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

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
