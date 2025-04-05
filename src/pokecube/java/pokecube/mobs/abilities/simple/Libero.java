package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "libero")
public class Libero extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (areWeTarget(mob, move)) return;
        final MoveEntry attack = move.getMove();
        if (attack.name.equals("struggle")) return;
        mob.setType1(attack.type);
    }

    @Override
    public void onUpdate(final IPokemob mob)
    {
        if (!mob.inCombat()) mob.setType1(null);
    }

    @Override
    public IPokemob onRecall(final IPokemob mob)
    {
        mob.setType1(null);
        return super.onRecall(mob);
    }
}
