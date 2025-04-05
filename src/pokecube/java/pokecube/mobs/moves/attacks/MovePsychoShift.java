package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.moves.MovesUtils;

@MoveProvider(name = "psycho-shift")
public class MovePsychoShift implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        IPokemob attacker = packet.getUser();

        boolean failed = false;
        if (attacker.getStatus() == IMoveConstants.STATUS_NON) failed = true;
        final IPokemob hit = PokemobCaps.getPokemobFor(packet.getTarget());
        if (hit != null && !failed)
        {
            if (hit.getStatus() != IMoveConstants.STATUS_NON) failed = true;
            else if (hit.setStatus(attacker, attacker.getStatus())) attacker.healStatus();
            else failed = true;
        }
        if (failed) MovesUtils.displayEfficiencyMessages(attacker, packet.getTarget(), -2, 0);
    }

}