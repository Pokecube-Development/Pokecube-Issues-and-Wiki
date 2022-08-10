package pokecube.mobs.abilities.g;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.utils.PokeType;

public class Galvanize extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (!move.pre) return;
        if (move.attackType == PokeType.getType("normal") && mob == move.attacker)
        {
            move.attackType = PokeType.getType("electric");
            move.PWR *= 1.2;
        }
    }
}
