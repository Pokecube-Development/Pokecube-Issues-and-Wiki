package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;

@MoveProvider(name = "protect")
public class Protect implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t) {
        // This wasn't doing anything?
    }
}
