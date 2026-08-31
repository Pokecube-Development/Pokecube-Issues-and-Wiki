package pokecube.api.data.effects.actions;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.damage.sources.TerrainDamageSource;
import pokecube.core.moves.damage.sources.TerrainDamageSource.TerrainType;

import java.util.function.Consumer;

public class DamageAction implements IEffectAction
{
    float amount = 1;
    boolean relative = false;
    String damage_type = "material";

    DamageSource _damage;
    Consumer<LivingEntity> _applier = e->{};

    public DamageAction()
    {}

    @Override
    public void applyEffect(LivingEntity mob)
    {
        _applier.accept(mob);
    }

    @Override
    public void init()
    {
        if ("material".equals(damage_type)) _applier = mob -> {
            float damage = relative ? amount * mob.getMaxHealth() : amount;
            _damage = new TerrainDamageSource(PokemobTerrainEffects.NoEffects.NO_EFFECTS, TerrainType.MATERIAL, null);
            mob.hurt(_damage, damage);
        };
    }
}
