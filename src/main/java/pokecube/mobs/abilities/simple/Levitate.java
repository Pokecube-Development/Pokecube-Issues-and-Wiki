package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;

@AbilityProvider(name = "levitate")
public class Levitate extends Ability
{
    @Override
    public int beforeDamage(IPokemob mob, MoveApplication move, int damage)
    {
        boolean weAreTarget = mob.getEntity() == move.getTarget() && mob.getAbility() == this;
        if (weAreTarget && move.getMove().getType(move.getUser()) == PokeType.getType("ground") && move.getTarget() == mob)
            return 0;
        return super.beforeDamage(mob, move, damage);
    }

    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if (move.getMove().getType(move.getUser()) == PokeType.getType("ground")) move.canceled = true;
    }
}
