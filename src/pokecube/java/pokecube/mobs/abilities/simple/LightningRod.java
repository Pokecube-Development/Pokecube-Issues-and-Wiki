package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.MovesUtils;

@AbilityProvider(name = "lightning-rod")
public class LightningRod extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (mob.getEntity() == move.getTarget() && move.type == PokeType.getType("electric"))
        {
            move.canceled = true;
            byte boost = IMoveConstants.SPATACK;
            MovesUtils.handleStats2(mob, mob.getOwner(), boost, IMoveConstants.RAISE);
        }
    }

}
