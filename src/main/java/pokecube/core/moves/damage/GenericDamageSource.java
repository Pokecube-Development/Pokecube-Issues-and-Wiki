package pokecube.core.moves.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class GenericDamageSource extends EntityDamageSource implements IPokedamage
{

    public GenericDamageSource(final String damageTypeIn, final Entity damageSourceEntityIn)
    {
        super(damageTypeIn, damageSourceEntityIn);
    }

    public static DamageSource causeMobDamage(final LivingEntity mob)
    {
        return new GenericDamageSource("mob", mob);
    }
}
