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

        final int def = attacker.getStat(Stats.DEFENSE, true);
        final int atk = attacker.getStat(Stats.ATTACK, true);

        var defModAtker = PokecubeAttributes.getModifierValue(attacker.getEntity(), Stats.DEFENSE)
                * PokecubeAttributes.getNatureModifier(attacker.getEntity(), Stats.DEFENSE);
        var atkModAtker = PokecubeAttributes.getModifierValue(attacker.getEntity(), Stats.ATTACK)
                * PokecubeAttributes.getNatureModifier(attacker.getEntity(), Stats.ATTACK);

        PokecubeAttributes.setStat(attacker.getEntity(), Stats.DEFENSE, atk / defModAtker);
        PokecubeAttributes.setStat(attacker.getEntity(), Stats.ATTACK, def / atkModAtker);
    }
}
