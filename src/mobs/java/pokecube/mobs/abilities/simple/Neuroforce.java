package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Accuracy;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;

@AbilityProvider(name = "neuroforce")
public class Neuroforce extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        IPokemob targetPokemob = PokemobCaps.getPokemobFor(move.getTarget());
        PokeType type = move.type;

        float efficiency = 1;

        if (targetPokemob != null)
        {
            // Efficiency scales based on typing for pokemobs
            efficiency = Tools.getAttackEfficiency(type, targetPokemob.getType1(), targetPokemob.getType2());
        }

        // Accuracy can then also factor in the target's stats.
        var moveAcc = move.accuracy.applyAccuracy(new Accuracy(move, efficiency));
        efficiency = moveAcc.efficiency();
        if (efficiency > 1)
        {
            move.pwr *= 1.25;
        }
    }
}
