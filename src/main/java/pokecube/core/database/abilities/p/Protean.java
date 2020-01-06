package pokecube.core.database.abilities.p;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class Protean extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacker && move.pre) mob.setType1(move.attackType);
    }
}
