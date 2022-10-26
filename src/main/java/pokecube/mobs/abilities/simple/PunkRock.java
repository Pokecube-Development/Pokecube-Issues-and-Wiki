package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.database.tags.Tags;

@AbilityProvider(name = "punk-rock")
public class PunkRock extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (areWeUser(mob, move)) return;
        if (Tags.MOVE.isIn("punk-rock-affected", move.getName()))
        {
            move.pwr *= 1.3;
            return;
        }
    }
}
