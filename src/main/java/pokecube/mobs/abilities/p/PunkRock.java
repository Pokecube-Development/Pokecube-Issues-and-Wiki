package pokecube.mobs.abilities.p;

import pokecube.core.database.abilities.Ability;
import pokecube.core.database.tags.Tags;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class PunkRock extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        if (move.pre && mob == move.attacked && Tags.MOVE.isIn("sound_based", move.attack))
        {
            move.PWR *= 1.3;
            return;
        }
    }
}
