package pokecube.mobs.abilities.r;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.database.abilities.Ability;
import pokecube.core.impl.capabilities.CapabilityPokemob;

public class Rivalry extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {

        if (!move.pre) return;
        final IPokemob target = CapabilityPokemob.getPokemobFor(move.attacked);
        if (mob == move.attacker && target != null)
        {
            final byte mobGender = mob.getSexe();
            final byte targetGender = target.getSexe();
            if (mobGender == IPokemob.SEXLEGENDARY || targetGender == IPokemob.SEXLEGENDARY
                    || mobGender == IPokemob.NOSEXE || targetGender == IPokemob.NOSEXE) return;

            if (mobGender == targetGender) move.PWR *= 1.25;
            else move.PWR *= 0.75;
        }
    }
}
