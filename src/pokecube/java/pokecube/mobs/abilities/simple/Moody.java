package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.MovesUtils;

@AbilityProvider(name = "moody")
public class Moody extends Ability
{
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        var r = mob.getEntity().getRandom();
        int randRaise = r.nextInt(7);
        int randDrop = r.nextInt(7);

        while (randDrop == randRaise) randDrop = r.nextInt(7);


        MovesUtils.handleStats2(mob, mob.getOwner(), randRaise, IMoveConstants.SHARP);
        MovesUtils.handleStats2(mob, mob.getOwner(), randDrop, IMoveConstants.FALL);
    }
}