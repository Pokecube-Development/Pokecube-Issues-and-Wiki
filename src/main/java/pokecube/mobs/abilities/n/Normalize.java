package pokecube.mobs.abilities.n;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.utils.PokeType;

public class Normalize extends Ability
{
    /*@Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (!move.pre) return;
        if (move.attackType != PokeType.getType("normal") && mob == move.attacker)
        {
            move.attackType = PokeType.getType("normal");
            move.PWR *= 1.2;
        }
    }*/
}
