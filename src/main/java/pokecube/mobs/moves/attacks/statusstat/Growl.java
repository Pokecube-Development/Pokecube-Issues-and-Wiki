package pokecube.mobs.moves.attacks.statusstat;

import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class Growl extends Move_Basic
{

    public Growl()
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
