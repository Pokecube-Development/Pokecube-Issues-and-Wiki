package pokecube.mobs.abilities.simple;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "rough-skin")
public class RoughSkin extends Ability
{
    @Override
    public int beforeDamage(IPokemob mob, MoveApplication move, int damage)
    {
        if (move.getMove().isContact(move.getUser()))
        {
            final LivingEntity entity = move.getUser().getEntity();
            final float maxHp = entity.getMaxHealth();
            // TODO message about recoil
            entity.hurt(DamageSource.MAGIC, 0.125f * maxHp);
        }
        return damage;
    }
}
