package pokecube.mobs.abilities.b;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;

public class BigPecks extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacked && move.attackedStatModification[1] < 0) move.attackedStatModProb = 0;
    }
}
