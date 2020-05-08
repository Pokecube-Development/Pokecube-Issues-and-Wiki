package pokecube.mobs.moves.attacks.statusstat;

import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class Sing extends Move_Basic
{

    public Sing()
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
