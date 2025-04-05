package pokecube.mobs.moves.attacks;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.templates.Move_Ongoing;

public class Perishsong extends Move_Ongoing
{
    @Override
    public void doOngoingEffect(final LivingEntity user, final IOngoingAffected mob, final IOngoingEffect effect)
    {
        if (effect.getDuration() == 0) this.damageTarget(mob.getEntity(), user, Integer.MAX_VALUE);
        else
        {
            // TODO perish counter here.
        }
    }

    @Override
    protected DamageSource getOngoingDamage(final LivingEntity user)
    {
        // TODO: Check if correct
        final DamageSource source = super.getOngoingDamage(user);
        source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS);
        source.is(DamageTypeTags.BYPASSES_ARMOR);
        return source;
    }

    @Override
    public int getDuration()
    {
        return 3;
    }
}
