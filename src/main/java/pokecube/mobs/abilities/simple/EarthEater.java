package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;

@AbilityProvider(name = "earth-eater")
public class EarthEater extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (mob.getEntity() == move.getTarget() && move.type == PokeType.getType("ground"))
        {
            move.canceled = true;
            final LivingEntity entity = mob.getEntity();
            final float hp = entity.getHealth();
            final float maxHp = entity.getMaxHealth();
            entity.setHealth(Math.min(hp + 0.25f * maxHp, maxHp));
        }
    }

}
