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
        final IPokemob attacked = PokemobCaps.getPokemobFor(packet.getTarget());
        if (attacked != null)
        {
            final int spatk = attacker.getStat(Stats.SPATTACK, true);
            final int atk = attacker.getStat(Stats.ATTACK, true);

            final int spatk2 = attacked.getStat(Stats.SPATTACK, true);
            final int atk2 = attacked.getStat(Stats.ATTACK, true);

            final int averageAtk = (atk + atk2) / 2;
            final int averageSpatk = (spatk + spatk2) / 2;

            var atkModAtker = PokecubeAttributes.getModifierValue(attacker.getEntity(), Stats.ATTACK)
                    * PokecubeAttributes.getNatureModifier(attacker.getEntity(), Stats.ATTACK);
            var atkModAtked = PokecubeAttributes.getModifierValue(attacked.getEntity(), Stats.ATTACK)
                    * PokecubeAttributes.getNatureModifier(attacked.getEntity(), Stats.ATTACK);

            var spatkModAtker = PokecubeAttributes.getModifierValue(attacker.getEntity(), Stats.SPATTACK)
                    * PokecubeAttributes.getNatureModifier(attacker.getEntity(), Stats.SPATTACK);
            var spatkModAtked = PokecubeAttributes.getModifierValue(attacked.getEntity(), Stats.SPATTACK)
                    * PokecubeAttributes.getNatureModifier(attacked.getEntity(), Stats.SPATTACK);

            PokecubeAttributes.setStat(attacker.getEntity(), Stats.ATTACK, averageAtk / atkModAtker);
            PokecubeAttributes.setStat(attacked.getEntity(), Stats.ATTACK, averageAtk / atkModAtked);

            PokecubeAttributes.setStat(attacker.getEntity(), Stats.SPATTACK, averageSpatk / spatkModAtker);
            PokecubeAttributes.setStat(attacked.getEntity(), Stats.SPATTACK, averageSpatk / spatkModAtked);
        }
    }
}
