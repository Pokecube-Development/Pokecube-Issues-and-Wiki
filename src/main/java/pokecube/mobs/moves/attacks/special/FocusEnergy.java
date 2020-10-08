package pokecube.mobs.moves.attacks.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class FocusEnergy extends Move_Basic
{

    public FocusEnergy()
    {
        super("focusenergy");
    }

    @Override
    public void attack(MovePacket packet)
    {
        super.attack(packet);
        if (packet.canceled || packet.failed) return;
        packet.attacker.getMoveStats().SPECIALTYPE = IPokemob.TYPE_CRIT;
    }
}
