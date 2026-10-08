package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.MovesUtils;

@AbilityProvider(name = "lightning-rod")
public class LightningRod extends Ability
{
    @Override
    public int beforeDamage(IPokemob mob, MoveApplication move, int damage)
    {
        boolean weAreTarget = move.getUserEntity() == move.getTarget() && mob.getAbility() == this;
        if (weAreTarget && move.getMove().getType(move.getUser()) == PokeType.getType("electric"))
            return 0;
        return super.beforeDamage(mob, move, damage);
    }

    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if (move.getMove().getType(move.getUser()) == PokeType.getType("electric")) {
            move.canceled = true;
            byte boost = IMoveConstants.SPATACK;
            MovesUtils.handleStats2(mob, mob.getEntity(), boost, IMoveConstants.RAISE);
        }

    }

}
