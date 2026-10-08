package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.MovesUtils;

@AbilityProvider(name = "thermal-exchange")
public class ThermalExchange extends Ability
{
    private boolean isCorrectType(PokeType type)
    {
        return type == PokeType.getType("fire");
    }

    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if (this.isCorrectType(move.type))
            MovesUtils.handleStats2(mob, mob.getEntity(), IMoveConstants.ATTACK, IMoveConstants.RAISE);
    }
}
