package pokecube.mobs.abilities.r;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;

public class Rattled extends Ability
{
    private boolean isCorrectType(PokeType type)
    {
        return type == PokeType.getType("dark") || type == PokeType.getType("bug") || type == PokeType.getType("ghost");
    }

   /* @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacked && !move.pre && this.isCorrectType(move.attackType)) MovesUtils.handleStats2(mob, mob
                .getEntity(), IMoveConstants.VIT, IMoveConstants.RAISE);
    }*/
}
