package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.database.tags.Tags;

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
