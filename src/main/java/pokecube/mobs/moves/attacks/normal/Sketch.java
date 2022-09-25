package pokecube.mobs.moves.attacks.normal;

import java.util.Set;

import com.google.common.collect.Sets;

import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;
import thut.lib.TComponent;

public class Sketch extends Move_Basic
{
    public static Set<String> unSketchables = Sets.newHashSet();

    static
    {
        Sketch.unSketchables.add("chatter");
    }

    public Sketch()
    {
        super("sketch");
    }

    @Override
    public void postAttack(final MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.attacker.getTransformedTo() != null) return;
        final String lastHitBy = packet.attacker.getEntity().getPersistentData().getString("lastMoveHitBy");
        final Move_Base toSketch = MovesUtils.getMoveFromName(lastHitBy);
        if (Sketch.unSketchables.contains(lastHitBy) || toSketch == null) return;
        for (int i = 0; i < packet.attacker.getMoves().length; i++)
            if (packet.attacker.getMoves()[i] != null && packet.attacker.getMoves()[i].equals(this.name))
        {
            packet.attacker.setMove(i, toSketch.name);
            packet.attacker.displayMessageToOwner(TComponent.translatable("pokemob.move.sketched",
                    packet.attacker.getDisplayName(), MovesUtils.getMoveName(lastHitBy, packet.attacker)));
            return;
        }
    }
}
