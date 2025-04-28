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
        final IPokemob attacked = PokemobCaps.getPokemobFor(packet.getTarget());
        if (attacked != null)
        {
            final int spdef = attacker.getStat(Stats.SPDEFENSE, true);
            final int def = attacker.getStat(Stats.DEFENSE, true);

            final int spdef2 = attacked.getStat(Stats.SPDEFENSE, true);
            final int def2 = attacked.getStat(Stats.DEFENSE, true);

            final int averageDef = (def + def2) / 2;
            final int averageSpdef = (spdef + spdef2) / 2;

            var defModAtker = PokecubeAttributes.getModifierValue(attacker.getEntity(), Stats.DEFENSE)
                    * PokecubeAttributes.getNatureModifier(attacker.getEntity(), Stats.DEFENSE);
            var defModAtked = PokecubeAttributes.getModifierValue(attacked.getEntity(), Stats.DEFENSE)
                    * PokecubeAttributes.getNatureModifier(attacked.getEntity(), Stats.DEFENSE);

            var spdefModAtker = PokecubeAttributes.getModifierValue(attacker.getEntity(), Stats.SPDEFENSE)
                    * PokecubeAttributes.getNatureModifier(attacker.getEntity(), Stats.SPDEFENSE);
            var spdefModAtked = PokecubeAttributes.getModifierValue(attacked.getEntity(), Stats.SPDEFENSE)
                    * PokecubeAttributes.getNatureModifier(attacked.getEntity(), Stats.SPDEFENSE);

            PokecubeAttributes.setStat(attacker.getEntity(), Stats.DEFENSE, averageDef / defModAtker);
            PokecubeAttributes.setStat(attacked.getEntity(), Stats.DEFENSE, averageDef / defModAtked);

            PokecubeAttributes.setStat(attacker.getEntity(), Stats.SPDEFENSE, averageSpdef / spdefModAtker);
            PokecubeAttributes.setStat(attacked.getEntity(), Stats.SPDEFENSE, averageSpdef / spdefModAtked);
        }
    }
}
