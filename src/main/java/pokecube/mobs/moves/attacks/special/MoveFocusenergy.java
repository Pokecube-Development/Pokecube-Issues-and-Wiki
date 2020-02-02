package pokecube.mobs.moves.attacks.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveFocusenergy extends Move_Basic
{

    public MoveFocusenergy()
    {
        super("focusenergy");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        packet.attacker.getMoveStats().SPECIALTYPE = IPokemob.TYPE_CRIT;
    }
}
