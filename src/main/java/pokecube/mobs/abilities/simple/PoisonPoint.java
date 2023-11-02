package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "poison-point")
public class PoisonPoint extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        final MoveEntry attack = move.getMove();
        final IPokemob attacker = move.getUser();
        if (move.hit && attack.isContact(attacker) && Math.random() > 0.7)
        {
            attacker.setStatus(mob, IMoveConstants.STATUS_PSN);
        }
    }
}
