package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.PreApplyTests;
import pokecube.core.moves.MovesUtils;

@MoveProvider(name = "splash")
public class Splash implements PreApplyTests
{
    @Override
    public boolean checkPreApply(MoveApplication t)
    {
        MovesUtils.sendPairedMessages(t.getTarget(), t.getUser(), "pokemob.move.doesnt.affect");
        return false;
    }
}
