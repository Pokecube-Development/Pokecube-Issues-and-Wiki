package pokecube.api.data.effects.actions;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import pokecube.core.moves.damage.TerrainDamageSource;
import pokecube.core.moves.damage.TerrainDamageSource.TerrainType;

public class DamageAction implements IEffectAction
{
    float amount = 1;
    boolean relative = false;
    String damage_type = "material";

    DamageSource _damage;

    public DamageAction()
    {}

    @Override
    public void applyEffect(LivingEntity mob)
    {
        float damage = relative ? amount * mob.getMaxHealth() : amount;
        mob.hurt(_damage, damage);
    }

    @Override
    public void init()
    {
        _damage = new TerrainDamageSource("material", TerrainType.MATERIAL, null);
    }
}
