package pokecube.mobs.abilities.o;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;

public class Oblivious extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        mob.getMoveStats().infatuateTarget = null;
        move.infatuateTarget = false;
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        mob.getMoveStats().infatuateTarget = null;
    }

}
