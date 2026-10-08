package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.damage.effects.StatusEffects;

@AbilityProvider(name = "toxic-chain")
public class ToxicChain extends Ability
{
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        final IPokemob attacker = move.getUser();
        if (move.hit && Math.random() > 0.7) StatusEffects.setStatus(attacker, mob, IMoveConstants.STATUS_PSN2);
    }
}
