package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.database.tags.Tags;

@AbilityProvider(name = "strong-jaw")
public class StrongJaw extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        final String attack = move.getName();
        if (Tags.MOVE.isIn("biting-moves", attack))
        {
            move.pwr *= 1.5;
        }
    }

}
