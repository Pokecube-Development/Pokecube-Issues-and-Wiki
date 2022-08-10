package pokecube.mobs.abilities.g;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.moves.MovePacket;

public class Guts extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacker && move.pre) move.statMults[Stats.ATTACK.ordinal()] = 1.5f;
    }
}
