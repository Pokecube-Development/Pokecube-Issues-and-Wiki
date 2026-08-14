package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.PreApplyTests;

@MoveProvider(name = "stomping-tantrum")
public class StompingTantrum implements PreApplyTests
{
    @Override
    public boolean checkPreApply(MoveApplication t) {
        if (t.getUser().getEntity().getPersistentData().contains("pokecube:lastMoveFailed"))
            t.pwr = 150;
        return PreApplyTests.super.checkPreApply(t);
    }
}
