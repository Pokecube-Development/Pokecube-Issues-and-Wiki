package pokecube.mobs.moves.attacks.special;

import java.util.Random;

import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;

public class Acupressure extends Move_Basic
{

    public Acupressure()
    {
        super("acupressure");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        final Random r = new Random(packet.attacked.getLevel().random.nextLong());
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
