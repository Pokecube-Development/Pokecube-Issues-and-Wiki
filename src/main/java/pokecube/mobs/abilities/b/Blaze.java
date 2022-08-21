package pokecube.mobs.abilities.b;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.utils.PokeType;

public class Blaze extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (!move.pre) return;
        if (mob == move.attacker && move.attackType == PokeType.getType("fire") && mob.getEntity().getHealth() < mob
                .getEntity().getMaxHealth() / 3) move.PWR *= 1.5;
    }
}
