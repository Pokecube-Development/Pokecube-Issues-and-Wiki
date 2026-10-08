package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.LoadedMove.PreProcessor;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;

import java.util.Collections;
import java.util.List;

@MoveProvider(name = "metronome")
public class Metronome implements PreProcessor
{
    @Override
    public void preProcess(MoveApplication t)
    {
        final List<MoveEntry> moves = MoveEntry.values();
        Collections.shuffle(moves);
        MoveEntry toUse = moves.getFirst();
        t.setMove(toUse);
    }
}
