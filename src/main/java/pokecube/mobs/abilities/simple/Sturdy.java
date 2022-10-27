package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "sturdy")
public class Sturdy extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        final LivingEntity target = mob.getEntity();
        final float hp = target.getHealth();
        final float maxHp = target.getMaxHealth();
        if (hp == maxHp) move.noFaint = true;
    }
}
