package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;

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
