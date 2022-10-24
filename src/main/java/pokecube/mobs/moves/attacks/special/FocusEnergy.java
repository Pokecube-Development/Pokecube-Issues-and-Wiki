package pokecube.mobs.moves.attacks.special;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class FocusEnergy extends Move_Basic
{

    public FocusEnergy()
    {
        super("focus-energy");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        packet.attacker.getMoveStats().SPECIALTYPE = IPokemob.TYPE_CRIT;
    }
}
