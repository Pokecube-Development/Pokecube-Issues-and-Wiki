package pokecube.mobs.moves.attacks.special;

import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.moves.templates.Move_Basic;

public class Rest extends Move_Basic
{

    public Rest()
    {
        super("rest");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        packet.attacker.healStatus();
        packet.attacker.healChanges();
        packet.attacker.setStatus(IMoveConstants.STATUS_SLP, 2);
    }
}
