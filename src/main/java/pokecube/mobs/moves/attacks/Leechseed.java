package pokecube.mobs.moves.attacks;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.moves.templates.Move_Ongoing;

public class Leechseed extends Move_Ongoing
{
    @Override
    public void doOngoingEffect(final LivingEntity user, final IOngoingAffected mob, final IOngoingEffect effect)
    {
        final LivingEntity living = mob.getEntity();
        final IPokemob pokemob = PokemobCaps.getPokemobFor(living);
        float factor = 0.0625f;
        if (pokemob != null) factor *= pokemob.getMoveStats().TOXIC_COUNTER + 1;
        final float thisMaxHP = living.getMaxHealth();
        final float damage = this.damageTarget(living, user, Math.max(1, (int) (factor * thisMaxHP)));
        if (user != null && user.isAlive()) user.setHealth(Math.min(user.getHealth() + damage, user.getMaxHealth()));
    }

    @Override
    public int getDuration()
    {
        return -1;
    }

}
