package pokecube.mobs.abilities.s;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.database.abilities.Ability;

public class Sturdy extends Ability
{
    @Override
    public int beforeDamage(IPokemob mob, MovePacket move, int damage)
    {
        if (mob == move.attacked)
        {
            final LivingEntity target = mob.getEntity();
            final float hp = target.getHealth();
            final float maxHp = target.getMaxHealth();
            if (hp == maxHp && damage >= hp) return (int) maxHp - 1;
        }
        return damage;
    }
}
