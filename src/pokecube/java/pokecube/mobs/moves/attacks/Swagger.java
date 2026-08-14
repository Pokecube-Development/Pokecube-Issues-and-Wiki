package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.damage.effects.StatusEffects;

@MoveProvider(name = "swagger")
public class Swagger implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t) {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;

        IPokemob attacker = packet.getUser();
        final IPokemob target = PokemobCaps.getPokemobFor(packet.getTarget());

        if (target != null)
        {
            packet.stat_effects[IPokemob.Stats.ATTACK.ordinal()] = 2;

            MovesUtils.displayStatsMessage(attacker, packet.getTarget(), 0, 1, (byte)2);
            StatusEffects.setStatus(packet.getTarget(), attacker.getEntity(), StatusEffects.CONFUSE, attacker.getEntity().getRandom().nextInt(2, 6), 1);
            MovesUtils.displayStatusMessages(attacker, target.getEntity(), IMoveConstants.CHANGE_CONFUSED, false);
        }
    }
}
