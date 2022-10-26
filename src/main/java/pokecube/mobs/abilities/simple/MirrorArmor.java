package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "mirror-armor")
public class MirrorArmor extends Ability
{
	@Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
		// TODO decide on how to do this now.
    }
}
