package pokecube.mobs.moves.attacks.statusstat;

import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class Defensecurl extends Move_Basic
{

    public Defensecurl()
    {
        super("defensecurl");
    }

    @Override
    public void attack(MovePacket packet)
    {
        super.attack(packet);
        if (packet.canceled || packet.failed) return;
        packet.attacker.getMoveStats().DEFENSECURLCOUNTER = 200;
    }

}
