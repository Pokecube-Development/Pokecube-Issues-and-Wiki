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

        PokecubeAttributes.setStat(attacker.getEntity(), Stats.DEFENSE, atk);
        PokecubeAttributes.setStat(attacker.getEntity(), Stats.ATTACK, def);
    }
}
