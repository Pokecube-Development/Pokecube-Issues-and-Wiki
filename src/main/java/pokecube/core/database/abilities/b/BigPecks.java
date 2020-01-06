package pokecube.core.database.abilities.b;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class BigPecks extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacked && move.attackedStatModification[1] < 0) move.attackedStatModProb = 0;
    }
}
