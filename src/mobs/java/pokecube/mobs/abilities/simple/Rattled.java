package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.MovesUtils;

@AbilityProvider(name = "rattled")
public class Rattled extends Ability
{
    private boolean isCorrectType(PokeType type)
    {
        return type == PokeType.getType("dark") || type == PokeType.getType("bug") || type == PokeType.getType("ghost");
    }

    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if (this.isCorrectType(move.type))
            MovesUtils.handleStats2(mob, mob.getEntity(), IMoveConstants.VIT, IMoveConstants.RAISE);
    }
}
