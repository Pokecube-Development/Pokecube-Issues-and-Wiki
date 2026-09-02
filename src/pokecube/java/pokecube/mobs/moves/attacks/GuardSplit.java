package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;

@MoveProvider(name = "guard-split")
public class GuardSplit implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        IPokemob attacker = packet.getUser();
        var attackerE = packet.getUserEntity();
        var attackedE = packet.getTarget();
        final IPokemob attacked = PokemobCaps.getPokemobFor(attackedE);
        if (attacked != null)
        {
            final int spdef = attacker.getStat(Stats.SPDEFENSE, true);
            final int def = attacker.getStat(Stats.DEFENSE, true);

            final int spdef2 = attacked.getStat(Stats.SPDEFENSE, true);
            final int def2 = attacked.getStat(Stats.DEFENSE, true);

            final int averageDef = (def + def2) / 2;
            final int averageSpdef = (spdef + spdef2) / 2;

            var defModAtker = PokecubeAttributes.getModifierValue(attackerE, Stats.DEFENSE)
                    * PokecubeAttributes.getNatureModifier(attackerE, Stats.DEFENSE);
            var defModAtked = PokecubeAttributes.getModifierValue(attackedE, Stats.DEFENSE)
                    * PokecubeAttributes.getNatureModifier(attackedE, Stats.DEFENSE);

            var spdefModAtker = PokecubeAttributes.getModifierValue(attackerE, Stats.SPDEFENSE)
                    * PokecubeAttributes.getNatureModifier(attackerE, Stats.SPDEFENSE);
            var spdefModAtked = PokecubeAttributes.getModifierValue(attackedE, Stats.SPDEFENSE)
                    * PokecubeAttributes.getNatureModifier(attackedE, Stats.SPDEFENSE);

            PokecubeAttributes.setStat(attackerE, Stats.DEFENSE, averageDef / defModAtker);
            PokecubeAttributes.setStat(attackedE, Stats.DEFENSE, averageDef / defModAtked);

            PokecubeAttributes.setStat(attackerE, Stats.SPDEFENSE, averageSpdef / spdefModAtker);
            PokecubeAttributes.setStat(attackedE, Stats.SPDEFENSE, averageSpdef / spdefModAtked);
        }
    }
}
