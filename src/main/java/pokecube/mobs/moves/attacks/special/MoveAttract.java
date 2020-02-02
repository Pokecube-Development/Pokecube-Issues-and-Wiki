package pokecube.mobs.moves.attacks.special;

import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveAttract extends Move_Basic
{

    public MoveAttract()
    {
        super("attract");
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        packet.infatuateTarget = true;
        super.onAttack(packet);
    }
}
