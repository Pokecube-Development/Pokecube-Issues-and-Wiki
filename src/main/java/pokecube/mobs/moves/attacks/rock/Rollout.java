package pokecube.mobs.moves.attacks.rock;

import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class Rollout extends Move_Basic
{

    public Rollout()
    {
        super("rollout");
    }

    public Rollout(String string)
    {
        super(string);
    }
    
    @Override
    public void onAttack(MovePacket packet)
    {
        super.onAttack(packet);
        if (packet.damageDealt == 0) packet.attacker.getMoveStats().ROLLOUTCOUNTER = 0;
        else packet.attacker.getMoveStats().ROLLOUTCOUNTER++;
    }

}
