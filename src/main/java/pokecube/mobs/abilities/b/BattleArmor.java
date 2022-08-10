package pokecube.mobs.abilities.b;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.database.abilities.Ability;

public class BattleArmor extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        move.criticalLevel = -1;
    }
}
