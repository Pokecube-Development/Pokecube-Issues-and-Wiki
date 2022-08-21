package pokecube.mobs.abilities.n;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.utils.PokeType;

public class Normalize extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (!move.pre) return;
        if (move.attackType != PokeType.getType("normal") && mob == move.attacker)
        {
            move.attackType = PokeType.getType("normal");
            move.PWR *= 1.2;
        }
    }
}
