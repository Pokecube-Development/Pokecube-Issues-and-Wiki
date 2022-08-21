package pokecube.mobs.abilities.a;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;

public class Analytic extends Ability
{
    // TODO Position modifiers
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move) {
        if (move.attacker == mob || move.pre) return;

        move.PWR += (move.PWR/100)*30;
    }
}
