package pokecube.core.moves.templates;

import java.util.Random;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.interfaces.entity.impl.OngoingMoveEffect;
import pokecube.core.moves.damage.GenericDamageSource;
import thut.core.common.ThutCore;

public class Move_Ongoing extends Move_Basic
{

    public Move_Ongoing(final String name)
    {
        super(name);
    }

    protected float damageTarget(final LivingEntity mob, final LivingEntity user, final float damage)
    {
        final DamageSource source = this.getOngoingDamage(user);
        mob.hurt(source, damage);
        return damage;
    }

    public void doOngoingEffect(final LivingEntity user, final IOngoingAffected mob, final IOngoingEffect effect)
    {
        final float thisMaxHP = mob.getEntity().getMaxHealth();
        final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
        mob.getEntity().hurt(this.getOngoingDamage(user), damage);
    }

    /**
     * I have these attacks affecting the target roughly once per 40 ticks,
     * this duration is how many times it occurs -1 can be used for a move that
     * occurs until the mob dies or returns to cube.
     *
     * @return the number of times this can affect the target
     */

    public int getDuration()
    {
        final Random r = ThutCore.newRandom();
        return 4 + r.nextInt(2);
    }

    protected DamageSource getOngoingDamage(final LivingEntity user)
    {
        final DamageSource source = GenericDamageSource.causeMobDamage(user);
        if (CapabilityPokemob.getPokemobFor(user) != null)
        {
            source.bypassMagic();
            source.bypassArmor();
        }
        return source;
    }

    public OngoingMoveEffect makeEffect(final LivingEntity user)
    {
        final OngoingMoveEffect effect = new OngoingMoveEffect(user);
        effect.setDuration(this.getDuration());
        effect.move = this;
        return effect;
    }

    /**
     * Does this apply an ongoing move to the attacker
     *
     * @return
     */
    public boolean onSource()
    {
        return false;
    }

    /**
     * Is and ongoing move applied to the source
     *
     * @return
     */
    public boolean onTarget()
    {
        return true;
    }
}
