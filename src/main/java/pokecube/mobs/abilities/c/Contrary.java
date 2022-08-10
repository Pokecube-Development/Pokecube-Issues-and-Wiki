package pokecube.mobs.abilities.c;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.database.abilities.Ability;

public class Contrary extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacked && move.pre)
        {
            move.attackedStatModification = move.attackedStatModification.clone();
            for (int i = 0; i < move.attackedStatModification.length; i++)
                move.attackedStatModification[i] = -move.attackedStatModification[i];
        }
    }
}
