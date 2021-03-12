package pokecube.mobs.moves.attacks.ongoing;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.templates.Move_Ongoing;

public class Perishsong extends Move_Ongoing
{

    public Perishsong()
    {
        super("perishsong");
    }

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
        return super.getOngoingDamage(user).bypassMagic().bypassArmor();
    }

    @Override
    public int getDuration()
    {
        return 3;
    }

    @Override
    public boolean onSource()
    {
        return true;
    }

}
