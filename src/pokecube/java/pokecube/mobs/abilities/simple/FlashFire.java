package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;

@AbilityProvider(name = "flash-fire")
public class FlashFire extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        if (mob.getEntity() == move.getTarget() && move.type == PokeType.getType("fire"))
        {
            move.canceled = true;
            mob.getEntity().getPersistentData().putBoolean("pokecube:fireBoost", true);
        }
        if (mob.getEntity().getPersistentData().contains("pokecube:fireBoost") && move.type == PokeType.getType("fire"))
            move.pwr = (move.pwr * 3) / 2;
    }

}