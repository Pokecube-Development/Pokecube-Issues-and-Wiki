package pokecube.mobs.abilities.w;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.moves.Move_Base;
import pokecube.core.database.tags.Tags;

public class WanderingSpirit extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        final Move_Base attack = move.getMove();
        final IPokemob attacker = move.attacker;

        if (move.pre || attacker == move.attacked) return;
        if (move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7)
        {
            if (attacker.getAbility() == null) return;
            final String name = attacker.getAbility().getName();

            // Don't apply to this one
            if (Tags.ABILITY.isIn("no_wandering_spirit", name)) return;

            // These will ensure that the ability is only temporarily set,
            // rather than permanently.
            if (!mob.inCombat()) mob.resetCombatTime();
            if (!attacker.inCombat()) attacker.resetCombatTime();

            // Swap the abilities
            mob.setAbility(attacker.getAbility());
            attacker.setAbility(this);
        }
    }
}
