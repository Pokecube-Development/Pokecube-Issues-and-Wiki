package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.utils.PokeType;

public class SteelySpirit extends Ability
{
	@Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (!move.pre) return;
        if (mob == move.attacker && move.attackType == PokeType.getType("steel"))
        		move.PWR *= 1.5;
    }
}
