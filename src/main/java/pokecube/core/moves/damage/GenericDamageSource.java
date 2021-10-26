package pokecube.core.moves.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

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
