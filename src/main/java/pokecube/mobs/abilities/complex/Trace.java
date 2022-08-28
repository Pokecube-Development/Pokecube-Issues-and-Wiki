package pokecube.mobs.abilities.complex;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.ai.brain.BrainUtils;

public class Trace extends Ability
{
    Ability traced;

    @Override
    public void onAgress(final IPokemob mob, final LivingEntity target)
    {
        final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
        if (this.traced != null) this.traced.onAgress(mob, target);
        else if (targetMob != null)
        {
            final Ability ability = targetMob.getAbility();
            if (ability != null) this.traced = AbilityManager.makeAbility(ability.toString(), mob);
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

    @Override
    public boolean singleton()
    {
        return false;
    }

}
