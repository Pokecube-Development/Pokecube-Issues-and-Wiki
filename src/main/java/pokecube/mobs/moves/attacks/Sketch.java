package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.database.tags.Tags;
import pokecube.core.moves.MovesUtils;
import thut.lib.TComponent;

@MoveProvider(name = "sketch")
public class Sketch implements PostMoveUse
{

    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        IPokemob attacker = packet.getUser();
        if (attacker.getTransformedTo() != null) return;
        final String lastHitBy = attacker.getEntity().getPersistentData().getString("lastMoveHitBy");
        final MoveEntry toSketch = MovesUtils.getMove(lastHitBy);
        if (Tags.MOVE.isIn("no-sketch", lastHitBy) || toSketch == null) return;
        for (int i = 0; i < attacker.getMovesCount(); i++)
            if (attacker.getMove(i) != null && attacker.getMove(i).equals(packet.getName()))
        {
            attacker.setMove(i, toSketch.name);
            attacker.displayMessageToOwner(TComponent.translatable("pokemob.move.sketched", attacker.getDisplayName(),
                    MovesUtils.getMoveName(lastHitBy, attacker)));
            return;
        }
    }
}
