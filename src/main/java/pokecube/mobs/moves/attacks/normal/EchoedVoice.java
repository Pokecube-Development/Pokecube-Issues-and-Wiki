package pokecube.mobs.moves.attacks.normal;

import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class EchoedVoice extends Move_Basic
{

    public EchoedVoice()
    {
        super("echoed-voice");
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        super.onAttack(packet);
        packet.attacker.getMoveStats().FURYCUTTERCOUNTER++;
    }
}