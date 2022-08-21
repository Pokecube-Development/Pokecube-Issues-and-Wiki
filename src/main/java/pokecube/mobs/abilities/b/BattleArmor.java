package pokecube.mobs.abilities.b;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;

public class BattleArmor extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        move.criticalLevel = -1;
    }
}
