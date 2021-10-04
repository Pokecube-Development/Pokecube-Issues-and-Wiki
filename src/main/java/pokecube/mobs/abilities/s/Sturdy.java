package pokecube.mobs.abilities.s;

import net.minecraft.world.entity.LivingEntity;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

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
