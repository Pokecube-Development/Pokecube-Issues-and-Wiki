package pokecube.mobs.abilities.p;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.database.abilities.Ability;

public class Protean extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacker && move.pre) mob.setType1(move.attackType);
    }
}
