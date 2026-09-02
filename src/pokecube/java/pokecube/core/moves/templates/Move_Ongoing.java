package pokecube.core.moves.templates;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.core.impl.entity.impl.OngoingMoveEffect;
import pokecube.core.moves.damage.sources.PokecubeDamageSources;
import thut.core.common.ThutCore;

import java.util.Random;
import java.util.function.Function;

public class Move_Ongoing implements Function<Damage, IOngoingEffect>
{

    @Override
    public IOngoingEffect apply(Damage t)
    {
        return this.makeEffect(t.move().getUserEntity());
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
     * I have these attacks affecting the target roughly once per 40 ticks, this duration is how many times it occurs -1
     * can be used for a move that occurs until the mob dies or returns to cube.
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
        return new DamageSource(PokecubeDamageSources.pokemobOngoing(), user);
    }

    public OngoingMoveEffect makeEffect(final LivingEntity user)
    {
        final OngoingMoveEffect effect = new OngoingMoveEffect(user);
        effect.setDuration(this.getDuration());
        effect.move = this;
        return effect;
    }
}
