package pokecube.core.database.abilities.c;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class ClearBody extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (move.pre && mob == move.attacked && mob != move.attacker)
        {
            move.attackerStatModification = move.attackerStatModification.clone();
            for (int i = 0; i < move.attackedStatModification.length; i++)
                if (move.attackedStatModification[i] < 0) move.attackedStatModification[i] = 0;
        }
    }
}
