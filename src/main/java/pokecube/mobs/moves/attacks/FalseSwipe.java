package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.LoadedMove.PreProcessor;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.moves.utils.MoveApplication;

@MoveProvider(name = "false-swipe")
public class FalseSwipe implements PreProcessor
{
    @Override
    public void preProcess(MoveApplication t)
    {
        t.noFaint = true;
    }
}
