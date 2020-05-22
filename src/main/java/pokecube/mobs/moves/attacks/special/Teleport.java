package pokecube.mobs.moves.attacks.special;

import pokecube.core.ai.tasks.combat.FindTargetsTask;
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
    public void postAttack(final MovePacket packet)
    {
        final IPokemob attacker = packet.attacker;
        FindTargetsTask.deagro(attacker.getEntity());
        super.postAttack(packet);
    }
}
