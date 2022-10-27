package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.Tools;

@AbilityProvider(name = "wonder-guard")
public class WonderGuard extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        final MoveEntry attack = move.getMove();
        final IPokemob attacker = move.getUser();
        if (!areWeTarget(mob, move)) return;
        final float eff = Tools.getAttackEfficiency(attack.getType(attacker), mob.getType1(), mob.getType2());
        if (eff <= 1 && attack.getPWR(attacker, mob.getEntity()) > 0) move.canceled = true;
    }
}
