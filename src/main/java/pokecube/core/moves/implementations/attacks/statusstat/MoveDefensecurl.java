package pokecube.core.moves.implementations.attacks.statusstat;

import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveDefensecurl extends Move_Basic
{

    public MoveDefensecurl()
    {
        super("defensecurl");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        packet.attacker.getMoveStats().DEFENSECURLCOUNTER = 200;
    }

}
