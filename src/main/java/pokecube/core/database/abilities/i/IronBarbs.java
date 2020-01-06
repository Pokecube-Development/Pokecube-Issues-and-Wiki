package pokecube.core.database.abilities.i;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class IronBarbs extends Ability
{
    @Override
    public int beforeDamage(IPokemob mob, MovePacket move, int damage)
    {
        if ((move.getMove().getAttackCategory() & IMoveConstants.CATEGORY_CONTACT) > 0)
        {
            final LivingEntity entity = move.attacker.getEntity();
            final float maxHp = entity.getMaxHealth();
            // TODO message about recoil
            entity.attackEntityFrom(DamageSource.MAGIC, 0.125f * maxHp);
        }
        return damage;
    }
}
