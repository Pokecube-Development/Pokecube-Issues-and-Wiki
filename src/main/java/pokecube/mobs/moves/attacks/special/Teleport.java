package pokecube.mobs.moves.attacks.special;

import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class Teleport extends Move_Basic
{
    public Teleport()
    {
        super("teleport");
    }

    @Override
    public void attack(final MovePacket packet)
    {
        final IPokemob attacker = packet.attacker;
        BrainUtils.deagro(attacker.getEntity());
        super.attack(packet);
    }
}
