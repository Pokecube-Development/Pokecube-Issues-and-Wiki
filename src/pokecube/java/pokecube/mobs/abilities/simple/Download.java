package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.moves.MovesUtils;

@AbilityProvider(name = "download")
public class Download extends Ability
{

    @Override
    public void onAgress(IPokemob mob, LivingEntity target)
    {
        final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
        if (targetMob != null)
        {
            if (targetMob.getStat(IPokemob.Stats.SPDEFENSE, true) > targetMob.getStat(IPokemob.Stats.DEFENSE, true))
                MovesUtils.handleStats2(targetMob, mob.getOwner(), IMoveConstants.ATTACK, IMoveConstants.RAISE);
            else // If Def <= spDef, raise special attack.
                MovesUtils.handleStats2(targetMob, mob.getOwner(), IMoveConstants.SPATACK, IMoveConstants.RAISE);
        }
    }
}
