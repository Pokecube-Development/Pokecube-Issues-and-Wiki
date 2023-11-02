package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants.AttackCategory;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.utils.EntityTools;

@AbilityProvider(name = "battery")
public class Battery extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (areWeTarget(mob, move)) return;
        final LivingEntity target = EntityTools.getCoreLiving(move.getTarget());
        if (target == null) return;
        final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
        if (targetMob == null) return;
        if (move.getMove().getCategory(move.getUser()) == AttackCategory.SPECIAL)
            move.pwr *= 1.3;
    }
}
