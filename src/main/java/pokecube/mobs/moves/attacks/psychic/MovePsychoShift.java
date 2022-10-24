package pokecube.mobs.moves.attacks.psychic;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;

public class MovePsychoShift extends Move_Basic
{

    public MovePsychoShift()
    {
        super("psycho-shift");
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        super.onAttack(packet);
        if (!(packet.canceled || packet.failed || packet.denied))
        {
            boolean failed = false;
            if (packet.attacker.getStatus() == IMoveConstants.STATUS_NON) failed = true;
            final IPokemob hit = PokemobCaps.getPokemobFor(packet.attacked);
            if (hit != null && !failed)
            {
                if (hit.getStatus() != IMoveConstants.STATUS_NON) failed = true;
                else if (hit.setStatus(packet.attacker.getStatus())) packet.attacker.healStatus();
                else failed = true;
            }
            if (failed) MovesUtils.displayEfficiencyMessages(packet.attacker, packet.attacked, -2, 0);
        }
    }

}