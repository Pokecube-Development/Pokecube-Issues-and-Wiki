package pokecube.mobs.abilities.simple;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;

public class RoughSkin extends Ability
{
    @Override
    public int beforeDamage(IPokemob mob, MoveApplication move, int damage)
    {
        if ((move.getMove().getAttackCategory(move.getUser()) & IMoveConstants.CATEGORY_CONTACT) > 0)
        {
            final LivingEntity entity = move.getUser().getEntity();
            final float maxHp = entity.getMaxHealth();
            // TODO message about recoil
            entity.hurt(DamageSource.MAGIC, 0.125f * maxHp);
        }
        return damage;
    }
}
