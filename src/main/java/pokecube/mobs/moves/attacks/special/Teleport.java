package pokecube.mobs.moves.attacks.special;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.moves.templates.Move_Basic;

public class Teleport extends Move_Basic
{
    public Teleport()
    {
        super("teleport");
    }

    @Override
    public void postAttack(final MovePacket packet)
    {
        final IPokemob attacker = packet.attacker;
        BrainUtils.deagro(attacker.getEntity());
        super.postAttack(packet);
    }
}
