package pokecube.mobs.moves.attacks;

import java.util.Collections;
import java.util.List;

import pokecube.api.data.moves.LoadedMove.PreProcessor;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;

@MoveProvider(name = "metronome")
public class Metronome implements PreProcessor
{
    @Override
    public void preProcess(MoveApplication t)
    {
        MoveEntry toUse = null;
        final List<MoveEntry> moves = MoveEntry.values();
        Collections.shuffle(moves);
        toUse = moves.get(0);
        t.setMove(toUse);
    }
}
