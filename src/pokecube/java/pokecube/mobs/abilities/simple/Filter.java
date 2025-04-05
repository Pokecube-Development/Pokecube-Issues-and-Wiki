package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.Tools;

@AbilityProvider(name = "filter")
public class Filter extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if (Tools.getAttackEfficiency(move.type, mob.getType1(), mob.getType2()) > 1) move.superEffectMult = 0.75f;
    }
}
