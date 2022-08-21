package pokecube.mobs.abilities.w;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.utils.PokeType;

public class WaterAbsorb extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacked && move.pre && move.attackType == PokeType.getType("water"))
        {
            move.canceled = true;
            final LivingEntity entity = mob.getEntity();
            final float hp = entity.getHealth();
            final float maxHp = entity.getMaxHealth();
            entity.setHealth(Math.min(hp + 0.25f * maxHp, maxHp));
        }
    }

}
