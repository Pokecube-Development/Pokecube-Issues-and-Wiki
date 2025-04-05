package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants.AttackCategory;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "ice-scales")
public class IceScales extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if (move.hit && move.getMove().getCategory(move.getUser()) == AttackCategory.SPECIAL) move.pwr = move.pwr / 2;
    }
}
