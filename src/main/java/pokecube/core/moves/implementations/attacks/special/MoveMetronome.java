package pokecube.core.moves.implementations.attacks.special;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import net.minecraft.entity.Entity;
import pokecube.core.PokecubeCore;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;
import thut.api.maths.Vector3;

public class MoveMetronome extends Move_Basic
{
    public MoveMetronome()
    {
        super("metronome");
    }

    @Override
    public void ActualMoveUse(Entity user, Entity target, Vector3 start, Vector3 end)
    {
        Move_Base toUse = null;
        final ArrayList<MoveEntry> moves = new ArrayList<>(MoveEntry.values());
        Collections.shuffle(moves);
        final Iterator<MoveEntry> iter = moves.iterator();
        while (toUse == null && iter.hasNext())
        {
            final MoveEntry move = iter.next();
            toUse = MovesUtils.getMoveFromName(move.name);
        }
        if (toUse != null) toUse.ActualMoveUse(user, target, start, end);
        else PokecubeCore.LOGGER.warn("Failed to find a move for metronome to use by " + user + " on " + target);
    }
}
