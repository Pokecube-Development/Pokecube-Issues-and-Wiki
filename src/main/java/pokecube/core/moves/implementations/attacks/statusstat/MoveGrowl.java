package pokecube.core.moves.implementations.attacks.statusstat;

import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveGrowl extends Move_Basic
{

    public MoveGrowl()
    {
        super("growl");
    }

    @Override
    public void preAttack(MovePacket packet)
    {
        super.preAttack(packet);
        this.soundUser = packet.attacker.getSound();
    }
}
