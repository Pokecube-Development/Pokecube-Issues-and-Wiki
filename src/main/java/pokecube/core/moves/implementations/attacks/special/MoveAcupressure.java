package pokecube.core.moves.implementations.attacks.special;

import java.util.Random;

import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;

public class MoveAcupressure extends Move_Basic
{

    public MoveAcupressure()
    {
        super("acupressure");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        final Random r = new Random(packet.attacked.getEntityWorld().rand.nextLong());
        int rand = r.nextInt(7);
        for (int i = 0; i < 8; i++)
        {
            final int stat = rand;
            if (MovesUtils.handleStats2(packet.attacker, packet.attacked, 1 << stat, IMoveConstants.SHARP)) return;
            rand = (rand + 1) % 7;
        }
        MovesUtils.displayEfficiencyMessages(packet.attacker, packet.attacked, -2, 0);
    }
}
