package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;

@MoveProvider(name = "power-split")
public class Powersplit implements PostMoveUse
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
            final int spatk = attacker.getStat(Stats.SPATTACK, true);
            final int atk = attacker.getStat(Stats.ATTACK, true);

            final int spatk2 = attacked.getStat(Stats.SPATTACK, true);
            final int atk2 = attacked.getStat(Stats.ATTACK, true);

            final int averageAtk = (atk + atk2) / 2;
            final int averageSpatk = (spatk + spatk2) / 2;

            var atkModAtker = PokecubeAttributes.getModifierValue(attackerE, Stats.ATTACK)
                    * PokecubeAttributes.getNatureModifier(attackerE, Stats.ATTACK);
            var atkModAtked = PokecubeAttributes.getModifierValue(attackedE, Stats.ATTACK)
                    * PokecubeAttributes.getNatureModifier(attackedE, Stats.ATTACK);

            var spatkModAtker = PokecubeAttributes.getModifierValue(attackerE, Stats.SPATTACK)
                    * PokecubeAttributes.getNatureModifier(attackerE, Stats.SPATTACK);
            var spatkModAtked = PokecubeAttributes.getModifierValue(attackedE, Stats.SPATTACK)
                    * PokecubeAttributes.getNatureModifier(attackedE, Stats.SPATTACK);

            PokecubeAttributes.setStat(attackerE, Stats.ATTACK, averageAtk / atkModAtker);
            PokecubeAttributes.setStat(attackedE, Stats.ATTACK, averageAtk / atkModAtked);

            PokecubeAttributes.setStat(attackerE, Stats.SPATTACK, averageSpatk / spatkModAtker);
            PokecubeAttributes.setStat(attackedE, Stats.SPATTACK, averageSpatk / spatkModAtked);
        }
    }
}
