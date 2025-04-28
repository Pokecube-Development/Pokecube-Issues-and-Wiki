package pokecube.core.moves.damage.sources;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class GenericDamageSource extends DamageSource
{

    private GenericDamageSource(final Holder<DamageType> damageTypeHolder, final Entity damageSourceEntityIn)
    {
        super(damageTypeHolder, damageSourceEntityIn);
    }

    public static DamageSource causeMobDamage(final LivingEntity mob)
    {
        return new GenericDamageSource(PokecubeDamageSources.pokemobGeneric(), mob);
    }
}
