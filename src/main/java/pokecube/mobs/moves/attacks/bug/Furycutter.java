package pokecube.mobs.moves.attacks.bug;

import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class Furycutter extends Move_Basic
{

    public Furycutter()
    {
        super("fury-cutter");
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        super.onAttack(packet);
        if (packet.damageDealt == 0) packet.attacker.getMoveStats().FURYCUTTERCOUNTER = 0;
        else packet.attacker.getMoveStats().FURYCUTTERCOUNTER++;
    }
}
