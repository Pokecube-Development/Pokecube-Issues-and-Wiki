package pokecube.api.data.effects.actions;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.damage.sources.TerrainDamageSource;
import pokecube.core.moves.damage.sources.TerrainDamageSource.TerrainType;

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
        _damage = new TerrainDamageSource(PokemobTerrainEffects.NoEffects.NO_EFFECTS, TerrainType.MATERIAL, null);
        mob.hurt(_damage, damage);
    }

}
