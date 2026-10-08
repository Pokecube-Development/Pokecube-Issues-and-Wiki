package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;

@AbilityProvider(name = "rivalry")
public class Rivalry extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        final IPokemob target = PokemobCaps.getPokemobFor(move.getTarget());
        if (target != null)
        {
            final byte mobGender = mob.getSexe();
            final byte targetGender = target.getSexe();
            if (mobGender == IPokemob.SEXLEGENDARY || targetGender == IPokemob.SEXLEGENDARY
                    || mobGender == IPokemob.NOSEXE || targetGender == IPokemob.NOSEXE)
                return;

            if (mobGender == targetGender) move.pwr *= 1.25;
            else move.pwr *= 0.75;
        }
    }
}
