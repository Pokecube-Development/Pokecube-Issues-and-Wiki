package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;

@MoveProvider(name = "power-trick")
public class Powertrick implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        IPokemob attacker = packet.getUser();
        var attackerE = packet.getUserEntity();

        final int def = attacker.getStat(Stats.DEFENSE, true);
        final int atk = attacker.getStat(Stats.ATTACK, true);

        var defModAtker =
                PokecubeAttributes.getModifierValue(attackerE, Stats.DEFENSE) * PokecubeAttributes.getNatureModifier(
                        attackerE, Stats.DEFENSE);
        var atkModAtker =
                PokecubeAttributes.getModifierValue(attackerE, Stats.ATTACK) * PokecubeAttributes.getNatureModifier(
                        attackerE, Stats.ATTACK);

        PokecubeAttributes.setStat(attackerE, Stats.DEFENSE, atk / defModAtker);
        PokecubeAttributes.setStat(attackerE, Stats.ATTACK, def / atkModAtker);
    }
}
