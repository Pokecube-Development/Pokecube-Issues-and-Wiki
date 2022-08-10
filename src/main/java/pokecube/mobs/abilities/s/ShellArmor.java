package pokecube.mobs.abilities.s;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.database.abilities.Ability;

public class ShellArmor extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (move.pre && mob == move.attacked) move.criticalLevel = -1;
    }
}
