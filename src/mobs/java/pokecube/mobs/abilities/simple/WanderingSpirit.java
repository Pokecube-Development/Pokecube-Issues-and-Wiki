package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.database.tags.Tags;

@AbilityProvider(name = "wandering-spirit")
public class WanderingSpirit extends Ability
{
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        final MoveEntry attack = move.getMove();
        if (!areWeTarget(mob, move)) return;
        IPokemob user = move.getUser();
        if (move.hit && attack.isContact(user) && Math.random() > 0.7)
        {
            if (user.getAbility() == null) return;
            final String name = user.getAbility().getName();

            // Don't apply to this one
            if (Tags.ABILITY.isIn("no_wandering_spirit", name)) return;

            // These will ensure that the ability is only temporarily set,
            // rather than permanently.
            if (!user.inCombat()) user.resetCombatTime();
            if (!user.inCombat()) user.resetCombatTime();

            // Swap the abilities
            user.setAbility(mob.getAbility());
            user.setAbility(this);
        }
    }
}
