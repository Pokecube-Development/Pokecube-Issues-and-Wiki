package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;

@MoveProvider(name = "hyper-drill")
public class HyperDrill implements PostMoveUse
{

    @Override
    public void applyPostMove(Damage t) {
        MoveApplication packet = t.move();
        MoveEntry move = packet.getMove();
        if (packet.canceled || packet.failed) return;

        if (packet.hit) {
            packet.failed = false;
        }
    }
}
