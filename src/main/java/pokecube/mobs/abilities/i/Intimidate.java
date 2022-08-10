package pokecube.mobs.abilities.i;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.database.abilities.Ability;
import pokecube.core.impl.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;

public class Intimidate extends Ability
{

    @Override
    public void onAgress(IPokemob mob, LivingEntity target)
    {
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob != null) MovesUtils.handleStats2(targetMob, mob.getOwner(), IMoveConstants.ATTACK,
                IMoveConstants.FALL);
    }
}
