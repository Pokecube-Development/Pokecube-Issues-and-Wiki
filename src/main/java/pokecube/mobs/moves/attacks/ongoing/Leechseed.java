package pokecube.mobs.moves.attacks.ongoing;

import net.minecraft.entity.LivingEntity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.templates.Move_Ongoing;

public class Leechseed extends Move_Ongoing
{

    public Leechseed()
    {
        super("leechseed");
    }

    @Override
    public void doOngoingEffect(final LivingEntity user, final IOngoingAffected mob, final IOngoingEffect effect)
    {
        final LivingEntity living = mob.getEntity();
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(living);
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
