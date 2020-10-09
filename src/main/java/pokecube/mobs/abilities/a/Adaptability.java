package pokecube.mobs.abilities.a;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class Adaptability extends Ability
{
    @Override
    public void preMove(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacker)
            move.stabFactor = 2;
    }
}
