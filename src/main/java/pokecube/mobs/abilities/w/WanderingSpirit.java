package pokecube.mobs.abilities.w;

import pokecube.core.database.abilities.Ability;
import pokecube.core.database.tags.Tags;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

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
