package pokecube.core.moves.implementations.attacks.statusstat;

import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveSing extends Move_Basic
{

    public MoveSing()
    {
        super("sing");
    }

    @Override
    public void preAttack(MovePacket packet)
    {
        super.preAttack(packet);
        this.soundUser = packet.attacker.getSound();
    }
}
