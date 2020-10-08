package pokecube.mobs.moves.attacks.water;

import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;

public class Splash extends Move_Basic
{

    public Splash()
    {
        super("splash");
    }

    @Override
    public void attack(final MovePacket packet)
    {
        super.attack(packet);
        packet.denied = true;
        MovesUtils.sendPairedMessages(packet.attacked, packet.attacker, "pokemob.move.doesnt.affect");
    }

}
