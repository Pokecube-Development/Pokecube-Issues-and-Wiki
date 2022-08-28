package pokecube.mobs.moves.attacks.special;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.PokecubeAPI;
import pokecube.api.moves.Move_Base;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;
import thut.api.maths.Vector3;

public class Metronome extends Move_Basic
{
    public Metronome()
    {
        super("metronome");
    }

    @Override
    public void ActualMoveUse(LivingEntity user, LivingEntity target, Vector3 start, Vector3 end)
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
        else PokecubeAPI.LOGGER.warn("Failed to find a move for metronome to use by " + user + " on " + target);
    }
}
