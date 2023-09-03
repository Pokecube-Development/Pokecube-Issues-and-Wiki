package pokecube.core.moves.damage;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class GenericDamageSource extends DamageSource implements IPokedamage
{

    public GenericDamageSource(final Holder<DamageType> damageTypeHolder, final Entity damageSourceEntityIn)
    {
        super(damageTypeHolder, damageSourceEntityIn);
    }

    public static DamageSource causeMobDamage(final LivingEntity mob)
    {
        // TODO: Check this
        return new GenericDamageSource(mob.damageSources().generic().typeHolder(), mob);
    }
}
