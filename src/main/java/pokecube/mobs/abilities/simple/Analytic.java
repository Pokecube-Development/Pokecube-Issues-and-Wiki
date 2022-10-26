package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "analytic")
public class Analytic extends Ability
{
    // TODO Position modifiers
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;

        // This ability isn't really meaningful in real-time combat, needs a
        // rework.
    }
}
