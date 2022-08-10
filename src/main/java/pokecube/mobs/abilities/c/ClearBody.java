package pokecube.mobs.abilities.c;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.database.abilities.Ability;

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
