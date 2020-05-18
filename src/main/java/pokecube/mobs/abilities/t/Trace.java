package pokecube.mobs.abilities.t;

import net.minecraft.entity.LivingEntity;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class Trace extends Ability
{
    Ability traced;

    @Override
    public void onAgress(final IPokemob mob, final LivingEntity target)
    {
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (this.traced != null) this.traced.onAgress(mob, target);
        else if (targetMob != null)
        {
            final Ability ability = targetMob.getAbility();
            if (ability != null) this.traced = AbilityManager.makeAbility(ability.getClass(), mob);
        }
    }

    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        if (this.traced != null) this.traced.onMoveUse(mob, move);
    }

    @Override
    public void onUpdate(final IPokemob mob)
    {
        if (this.traced != null && !BrainUtils.hasAttackTarget(mob.getEntity()))
        {
            this.traced.destroy();
            this.traced = null;
        }
        else if (this.traced != null) this.traced.onUpdate(mob);
    }

}
